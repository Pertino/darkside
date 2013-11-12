package org.devnull.darkside.configs;

import org.devnull.darkside.JsonBase;

public class DynamoConfig extends JsonBase
{
	public String tableName = "fqdn";
	public String accessKey = null;
	public String secretKey = null;

	/**
	 * override this to change the endpoint we're going to connect to.  Amazon defaults
	 * to something in US-east.
	 */
	public String endPoint = null;
}
