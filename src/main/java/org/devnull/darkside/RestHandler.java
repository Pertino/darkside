package org.devnull.darkside;

import org.apache.log4j.*;
import org.devnull.statsd_client.StatsObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.devnull.darkside.configs.DarksideConfig;

/**
 * works for path /fqdn/1 which is the version of this API implementation
 */
@Path("/fqdn/1")
public class RestHandler
{
	private static final Logger log = Logger.getLogger(RestHandler.class);

	private static DarksideConfig config = null;
	private BackendDB db = null;
	private static final StatsObject so = StatsObject.getInstance();

	/**
	 * Jersey requires there be a no-args public constructor.
	 */
	public RestHandler()
	{
		db = StuffHolder.getInstance().getDB();
		config = StuffHolder.getInstance().getConfig();
	}

	/**
	 * this should return 404 not found if the record isn't there
	 * and some other thing if the fqdn isn't specified, etc.
	 * @return Response object for request
	 */
	@GET
	@Path("/{fqdn}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("fqdn") String fqdn)
	{
		if (fqdn == null || fqdn.trim().length() == 0)
		{
			return Response.serverError().entity(new HandlerException("fqdn cannot be blank")).build();
		}

		so.increment("RestHandler.gets.total");

		DNSRecord r = null;

		try
		{
			log.debug("fetching fqdn: " + fqdn);
			r = db.get(fqdn);
		}
		catch (Exception e)
		{
			so.increment("RestHandler.gets.errors_in_lookup");
			return Response.status(404).entity(new HandlerException("error fetching record: " + e)).build();
		}

		if (r == null)
		{
			so.increment("RestHandler.gets.not_found");
			// return 404
			return Response.status(404).entity(new HandlerException("no record for hostname " + fqdn)).build();
		}

		so.increment("RestHandler.gets.found");

		//
		// if there is no TTL stored with the record in the database,
		// use the default TTL.
		//
		if (r.getTtl() == null)
		{
			r.setTtl(config.defaultTTL);
		}

		return Response.ok(r).build();
	}


	/**
	 * this should return some kind of 4xx for insufficient data,
	 * with associated error, etc
	 * and some kind of 200 OK with a result: true for success.
	 * @return Response object for request
	 */
	@POST
	@Path("/{fqdn}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(@PathParam("fqdn") String fqdn, DNSRecord record)
	{
		so.increment("RestHandler.posts.total");

		if (fqdn == null || fqdn.trim().length() == 0)
		{
			return Response.serverError().entity(new HandlerException("fqdn cannot be blank")).build();
		}

		if (record == null)
		{
			return Response.status(400).entity(new HandlerException("missing record body")).build();
		}

		if (record.getRecords() == null)
		{
			so.increment("RestHandler.posts.no_records");
			return Response.status(400).entity(new HandlerException("no records were specified")).build();
		}

		if (record.getRecords().size() == 0)
		{
			so.increment("RestHandler.posts.no_records");
			return Response.status(400).entity(new HandlerException("records list was empty")).build();
		}

		try
		{
			record.setFqdn(fqdn);
			db.put(fqdn, record);
			so.increment("RestHandler.posts.success");
		}
		catch (Exception e)
		{
			so.increment("RestHandler.posts.exceptions");
			return Response.serverError().entity(new HandlerException("exception creating item: " + e)).build();
		}

		return Response.ok().build();
	}

	/**
	 * this should return 200 OK all the time
	 * @return Response object for request
	 */
	@DELETE
	@Path("/{fqdn}")
	public Response delete(@PathParam("fqdn") String fqdn)
	{
		if (fqdn == null || fqdn.trim().length() == 0)
		{
			return Response.serverError().entity(new HandlerException("fqdn cannot be blank")).build();
		}

		so.increment("RestHandler.deletes.total");

		try
		{
			db.delete(fqdn);
			so.increment("RestHandler.deletes.success");
		}
		catch (Exception e)
		{
			so.increment("RestHandler.deletes.exceptions");
			return Response.serverError().entity(new HandlerException("exception deleting item: " + e)).build();
		}

		return Response.ok().build();
	}
}
