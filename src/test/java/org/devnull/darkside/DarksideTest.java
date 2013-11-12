package org.devnull.darkside;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.devnull.statsd_client.StatsObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static org.testng.AssertJUnit.assertTrue;

public class DarksideTest extends JsonBase
{
	protected static Logger log = null;
	private static StatsObject so = StatsObject.getInstance();

	@BeforeClass
	public void setUp() throws Exception
	{
		Properties logProperties = new Properties();

		logProperties.put("log4j.rootLogger", "INFO, stdout");
		logProperties.put("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
		logProperties.put("log4j.appender.stdout.layout", "org.apache.log4j.EnhancedPatternLayout");
		logProperties.put("log4j.appender.stdout.layout.ConversionPattern", "%d [%F:%L] [%p] %C: %m%n");
		logProperties.put("log4j.appender.stdout.layout.ConversionPattern", "%d [%p] %C: %m%n");
		logProperties.put("log4j.appender.stdout.immediateFlush", "true");
		logProperties.put("log4j.category.org.apache.http.wire", "INFO, stdout");
		logProperties.put("log4j.additivity.org.apache.http.wire", false);

		BasicConfigurator.resetConfiguration();
		PropertyConfigurator.configure(logProperties);

		log = Logger.getLogger(DarksideTest.class);

		so.clear();
	}

	@Test
	public void testRun() throws Exception
	{

		so.clear();
		String[] args = {"-c", "src/test/resources/test.conf", "-l", "src/test/resources/log4j.conf"};
		Darkside ds = new Darkside(args);
		Thread darksideThread = new Thread(ds, "Darkside");
		darksideThread.start();

		//
		// give it time to start up
		//
		Thread.sleep(200);

		ds.shutdown();
		darksideThread.join(2000L);
		assertTrue(!darksideThread.isAlive());

		//
		// confirm StatsObject contains stuff
		//
		Map<String, Long> soMap = new TreeMap<String, Long>(so.getMapAndClear());
		String soMapString = mapper.writeValueAsString(soMap);

		log.debug(soMapString);

		Thread.sleep(100);
	}
}
