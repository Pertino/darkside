package org.devnull.darkside;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.Logger;
import org.devnull.darkside.backends.BackendDB;
import org.devnull.darkside.backends.DynamoDBBackend;
import org.devnull.darkside.backends.LevelDBBackend;

public class DBFactory
{
	private static final Logger log = Logger.getLogger(DBFactory.class);

	public static BackendDB getBackendDBInstance(final String type, final JsonNode config)
		throws Exception
	{
		if (type == null || config == null)
		{
			throw new IllegalArgumentException("type or config is null");
		}

		String lowerType = type.toLowerCase();

		BackendDB ret = null;

		if (lowerType.equals("leveldb"))
		{
			ret = new LevelDBBackend();
            ret.configure(config);
		}
		else if (lowerType.equals("dynamo"))
		{
			ret = new DynamoDBBackend();
            ret.configure(config);
		}
		else
		{
			throw new IllegalArgumentException("unknown backend type: " + type);
		}

		return ret;
	}
}
