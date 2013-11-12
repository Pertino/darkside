package org.devnull.darkside.configs;

import org.devnull.darkside.JsonBase;

public class LevelDBConfig extends JsonBase
{
	/**
	 * maximum number of items to have in cache
	 */
	public int maxItems = 1000000;

	/**
	 * directory where the cache should be instantiated
	 */
	public String dbPath = "/tmp/darksideCache";
}
