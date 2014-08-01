package org.devnull.darkside;

import org.apache.log4j.Logger;
import org.devnull.darkside.backends.BackendDB;
import org.devnull.darkside.configs.DarksideConfig;
import org.devnull.statsd_client.StatsObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * works for path /fqdn/1 which is the version of this API implementation
 */
@Path("/fqdn/1")
public class RestHandler {
    private static final Logger log = Logger.getLogger(RestHandler.class);

    private static DarksideConfig config = null;
    private BackendDB db = null;
    private static final StatsObject so = StatsObject.getInstance();

    /**
     * Jersey requires there be a no-args public constructor.
     */
    public RestHandler() {
        db = StuffHolder.getInstance().getDB();
        config = StuffHolder.getInstance().getConfig();
    }

    /**
     * this should return 404 not found if the record isn't there
     * and some other thing if the fqdn isn't specified, etc.
     *
     * @return Response object for request
     */
    @GET
    @Path("/{fqdn}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("fqdn") String fqdn) {
        long start = System.nanoTime();

        try {
            so.increment("RestHandler.gets.total");

            if (fqdn == null || fqdn.trim().length() == 0) {
                log.debug("missing fqdn or it was 0 length");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new HandlerException("fqdn cannot be blank")).build();
            }

            DNSRecordSet r = null;

            try {
                if (log.isDebugEnabled()) {
                    log.debug("fetching fqdn: " + fqdn.toLowerCase());
                }

                r = db.get(fqdn.toLowerCase());
            } catch (Exception e) {
                so.increment("RestHandler.gets.errors_in_lookup");
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(new HandlerException("exception fetching item: " + e)).build();
            }

            if (r == null) {
                so.increment("RestHandler.gets.not_found");
                // return 404
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            so.increment("RestHandler.gets.found");

            //
            // if there is no TTL stored with the record in the database,
            // use the default TTL.
            //
            if (r.getTtl() == null) {
                r.setTtl(config.defaultTTL);
            }

            return Response.ok(r).build();
        } finally {
            so.timing("RestHandler.get", (System.nanoTime() - start) / 1000);
        }
    }

    /**
     * @param fqdn      the hostname to update records for
     * @param recordSet the set of records to insert for the hostname.  All updates must include *all* of the
     *                  records relevant for the given fqdn.
     * @return 200 OK or something else
     */
    @POST
    @Path("/{fqdn}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(@PathParam("fqdn") String fqdn, DNSRecordSet recordSet) {

        long start = System.nanoTime();

        try {
            so.increment("RestHandler.posts.total");

            if (fqdn == null || fqdn.trim().length() == 0) {
                log.debug("missing fqdn or it was 0 length");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new HandlerException("fqdn cannot be blank")).build();
            }

            if (recordSet == null) {
                return Response.status(Response.Status.PRECONDITION_FAILED)
                        .entity(new HandlerException("missing record body")).build();
            }

            if (recordSet.getRecords() == null) {
                so.increment("RestHandler.posts.no_records");
                return Response.status(Response.Status.PRECONDITION_FAILED)
                        .entity(new HandlerException("no records were specified")).build();
            }

            if (recordSet.getRecords().size() == 0) {
                so.increment("RestHandler.posts.no_records");
                return Response.status(Response.Status.PRECONDITION_FAILED)
                        .entity(new HandlerException("records list was empty")).build();
            }

            try {
                if (log.isDebugEnabled()) {
                    log.debug("inserting new record for " + fqdn.toLowerCase());
                }

                recordSet.setFqdn(fqdn.toLowerCase());
                db.put(recordSet);
                so.increment("RestHandler.posts.success");
            } catch (Exception e) {
                so.increment("RestHandler.posts.exceptions");
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(new HandlerException("exception creating item: " + e)).build();
            }

            return Response.ok().build();
        }
        finally {
            so.timing("RestHandler.post", (System.nanoTime() - start) / 1000);
        }
    }

    /**
     * this should return 200 OK all the time
     *
     * @return Response object for request
     */
    @DELETE
    @Path("/{fqdn}")
    public Response delete(@PathParam("fqdn") String fqdn) {

        long start = System.nanoTime();

        try {
            so.increment("RestHandler.deletes.total");

            if (fqdn == null || fqdn.trim().length() == 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new HandlerException("fqdn cannot be blank")).build();
            }

            try {
                if (log.isDebugEnabled()) {
                    log.debug("deleting fqdn: " + fqdn.toLowerCase());
                }

                db.delete(fqdn.toLowerCase());
                so.increment("RestHandler.deletes.success");
            } catch (Exception e) {
                so.increment("RestHandler.deletes.exceptions");
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(new HandlerException("exception deleting item: " + e)).build();
            }

            return Response.ok().build();
        }
        finally {
            so.timing("RestHandler.delete", (System.nanoTime() - start) / 1000);
        }
    }
}
