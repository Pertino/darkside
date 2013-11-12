package org.devnull.darkside.configs;

import java.util.HashMap;
import java.util.Map;

import org.devnull.darkside.JsonBase;

public class DarksideConfig extends JsonBase
{
	/**
	 * The default statsd shipper instantiated will be the NullStatsdShipper, which simply clears the
	 * StatsObject every second.
	 */
	public String statsd_client_type = null;

	/**
	 * A pointer to a map that holds the contents of a UDPStatsdShipperConfig or a ZMQStatsdShipperConfig.
	 * Leave null if the statsd_client_type is not "udp" or "zmq".
	 */
	public Map<String, Object> statsd_config = null;

	/**
	 * the address and port to listen on
	 */
	public String listenAddress = "localhost";
	public int listenPort = 8080;

	/**
	 * The maximum number of threads to use to answer requests.
	 * This directly correllates to the number of concurrent db requests that can be done.
	 */
	public int workerThreads = 40;

	/**
	 * enable DIGEST authentication on requests, and the username and password to use.
	 */
	public boolean useAuthentication = true;
	public String username = "foo";
	public String password = "bar";

	/**
	 * enable this to get a bunch of internal Jersey debug output for working out issues
	 */
	public boolean enableInternalDebugLogging = false;

	/**
	 * database type
	 */
	public String dbType = "leveldb";
	public Map<String, String> dbConfig = new HashMap<String, String>();

	/**
	 * set the default TTL for responses.  This value is not used when records are written,
	 * only when they are returned, and only when the TTL associated with the record in
	 * the database is null.
	 */
	public int defaultTTL = 300;
}
