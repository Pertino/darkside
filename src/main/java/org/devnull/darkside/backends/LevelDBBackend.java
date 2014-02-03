package org.devnull.darkside.backends;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.Logger;
import org.devnull.darkside.BackendDB;
import org.devnull.darkside.DNSRecordSet;
import org.devnull.darkside.JsonBase;
import org.devnull.darkside.configs.LevelDBConfig;
import org.devnull.statsd_client.StatsObject;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class LevelDBBackend extends JsonBase implements BackendDB
{
	private static final Logger log = Logger.getLogger(BackendDB.class);
	private static final StatsObject so = StatsObject.getInstance();

	private DB db = null;

	public LevelDBBackend(final JsonNode configNode)
		throws Exception
	{
		LevelDBConfig config = mapper.treeToValue(configNode, LevelDBConfig.class);

		Options options = new Options();
		options.createIfMissing(true);
		options.cacheSize(config.maxItems);
		db = factory.open(new File(config.dbPath), options);
	}

	public void put(DNSRecordSet record) throws Exception
	{
		try
		{
			db.put(record.getFqdn().getBytes(), mapper.writeValueAsBytes(record));
			so.increment("LevelDBBackend.puts.ok");
		}
		catch (Exception e)
		{
			log.debug("excepting saving record in leveldb: " + e);
			so.increment("LevelDBBackend.puts.exceptions");
			throw e;
		}
	}

	public DNSRecordSet get(String fqdn) throws Exception
	{
		try
		{
			byte[] res = db.get(fqdn.getBytes());

			if (res == null)
			{
				so.increment("LevelDBBackend.gets.ok");
				return null;
			}

			DNSRecordSet r = mapper.readValue(res, DNSRecordSet.class);
			so.increment("LevelDBBackend.gets.ok");

			return r;
		}
		catch (Exception e)
		{
			log.debug("exception fetching record for fqdn " + fqdn + " from leveldb: " + e);
			so.increment("LevelDBBackend.gets.exceptions");
			throw e;
		}
	}

	public void delete(final String fqdn) throws Exception
	{
		if (fqdn == null)
		{
			throw new NullPointerException("fqdn argument is null");
		}

		try
		{
			db.delete(fqdn.getBytes());
			so.increment("LevelDBBackend.deletes.ok");
		}
		catch (Exception e)
		{
			log.debug("exception deleting record for fqdn " + fqdn + " from leveldb: " + e);
			so.increment("LevelDBBackend.deletes.exceptions");
			throw e;
		}
	}

	public void shutdown()
	{
		try
		{
			db.close();
		}
		catch (IOException e)
		{
			log.warn("error shutting down database: " + e);
			e.printStackTrace();
		}
	}
}
