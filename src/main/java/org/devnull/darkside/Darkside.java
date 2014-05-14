package org.devnull.darkside;

import org.devnull.darkside.backends.BackendDB;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.devnull.darkside.configs.DarksideConfig;
import org.devnull.statsd_client.Shipper;
import org.devnull.statsd_client.ShipperFactory;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;

public class Darkside extends JsonBase implements Runnable
{
    private static Logger log = Logger.getLogger(Darkside.class);

    private Server server = null;
    private DarksideConfig config = null;

    public static void main(String[] args) throws Exception
    {
        try
        {
            BasicConfigurator.configure();
            Darkside p = new Darkside(args);
            p.run();
        }
        catch (Exception e)
        {
            log.error(e);
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

    /**
     * Reads command line arguments and starts the service.
     * Continues until shutdown() is called on it, useful for unit testing.
     * <p/>
     * Command line args:
     * <p/>
     * -l <log4j.conf>		Path to a log4j properties file, optional
     * -c <darkside.conf>		Path to the json-based configuration file, required
     *
     * @param args The String[] array of the command-line arguments
     * @throws Exception If there is an error reading the config files
     */

    public Darkside(String[] args) throws Exception
    {
        //
        // set up a console logger until we've ready in the log4j config
        //
        BasicConfigurator.configure();
        log = Logger.getLogger(Darkside.class);
        DarksideConfig config = null;

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].trim().equals("-c"))
            {
                String configFile = args[i + 1];
                config = mapper.readValue(new File(configFile), DarksideConfig.class);
                i++;
            }
            else if (args[i].trim().equals("-l"))
            {
                i++;
                setupLogging(args[i]);
            }
            else
            {
                log.info("Unknown command line argument: " + args[i]);
            }
        }

        if (null == config)
        {
            throw new IllegalArgumentException("No configuration file specified on command line");
        }

        this.config = config;
        setup(config);
    }

    private void setup(final DarksideConfig config) throws Exception
    {
        BackendDB db = DBFactory.getBackendDBInstance(config.dbType, mapper.valueToTree(config.dbConfig));

        //
        // place a copy of the db and config pointers into the singleton that makes them accessible
        // to the RestHandler instances created automatically by Jetty/Jersey
        //
        StuffHolder.getInstance().setDB(db);
        StuffHolder.getInstance().setConfig(config);

        server = new Server(new InetSocketAddress(config.listenAddress, config.listenPort));

        //
        // And this is how we tell Jetty about the servlet context that we have created
        //
        ServletHolder servletHolder = new ServletHolder(ServletContainer.class);

        //
        // IMPORTANT: you have to specify the package where your resources are located
        // in order for Jetty to pick them up
        //
        servletHolder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "org.devnull.darkside");


        if (config.enableInternalDebugLogging)
        {
            //
            // comment out to hide debug information
            //
            // servletHolder.setInitParameter("com.sun.jersey.config.feature.Debug", "true");
            // servletHolder.setInitParameter("com.sun.jersey.config.feature.Trace", "true");
            // servletHolder.setInitParameter("com.sun.jersey.spi.container.ContainerRequestFilters",
            //			       "com.sun.jersey.api.container.filter.LoggingFilter");
            // servletHolder.setInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters",
            //			       "com.sun.jersey.api.container.filter.LoggingFilter");
        }

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(servletHolder, "/*");

        if (config.useAuthentication)
        {
            context.setSecurityHandler(getDigestAuthHandler(config.username, config.password));
        }

        server.setHandler(context);

        QueuedThreadPool queuedThreadPool = new QueuedThreadPool(config.workerThreads);
        queuedThreadPool.setName("RestHandlerThread");
        server.setThreadPool(queuedThreadPool);
    }

    public void run()
    {
        try
        {
            //
            // instantiate the StatsdShipper and kick off the statsd shipper thread
            //
            Shipper shipper = ShipperFactory.getInstance(config.statsd_client_type);
            shipper.configure(mapper.writeValueAsString(config.statsd_config));
            Thread statsdShipperThread = new Thread(shipper, "StatsdShipper");
            statsdShipperThread.start();

            server.start();

            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }

            log.debug("stopping and joining web server");

            server.stop();
            server.join();

            log.debug("shutting down database");

            StuffHolder.getInstance().getDB().shutdown();

            log.debug("shutting down statsd shipper");

            shipper.shutdown();
            statsdShipperThread.interrupt();
            statsdShipperThread.join(1000);
        }
        catch (Exception e)
        {
            log.fatal("error in main", e);
        }
    }

    public static SecurityHandler getDigestAuthHandler(String username, String password)
    {
        final String[] roles = {"user"};
        final HashLoginService loginService = new HashLoginService("MyRealm");
        loginService.putUser(username, new Password(password), roles);
        final ConstraintSecurityHandler constraintSecurityHandler = new ConstraintSecurityHandler();
        final Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(roles);
        final ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setPathSpec("/");
        constraintMapping.setConstraint(constraint);
        constraintSecurityHandler.setConstraintMappings(Arrays.asList(constraintMapping),
                new HashSet<String>(Arrays.asList(roles)));
        constraintSecurityHandler.setAuthenticator(new DigestAuthenticator());
        constraintSecurityHandler.setLoginService(loginService);
        return constraintSecurityHandler;
    }

    /**
     * Configures log4j using the passed-in log4j.conf properties file.
     *
     * @param log4jConfig Path to the log4j config file on disk.
     */
    private void setupLogging(final String log4jConfig)
    {
        LogManager.resetConfiguration();

        if (null != log4jConfig)
        {
            //
            // load log4j config file
            //
            PropertyConfigurator.configure(log4jConfig);
        }

        log = Logger.getLogger(Darkside.class);
    }
}
