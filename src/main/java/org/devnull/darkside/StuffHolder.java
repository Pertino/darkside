package org.devnull.darkside;

import org.devnull.darkside.configs.DarksideConfig;

public class StuffHolder
{
	private static StuffHolder ourInstance = new StuffHolder();
	private BackendDB db = null;
	private DarksideConfig config = null;

	public static StuffHolder getInstance()
	{
		return ourInstance;
	}

	private StuffHolder()
	{
	}

	public void setDB(final BackendDB db)
	{
		this.db = db;
	}

	public BackendDB getDB()
	{
		return this.db;
	}

	public void setConfig(final DarksideConfig config)
	{
		this.config = config;
	}

	public DarksideConfig getConfig()
	{
		return this.config;
	}
}
