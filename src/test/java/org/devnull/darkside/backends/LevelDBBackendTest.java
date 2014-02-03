package org.devnull.darkside.backends;

import org.devnull.darkside.*;
import org.devnull.darkside.configs.LevelDBConfig;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.devnull.darkside.records.ARecord;
import org.devnull.darkside.records.Record;
import org.devnull.statsd_client.StatsObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.testng.AssertJUnit.*;

public class LevelDBBackendTest extends JsonBase
{
	private static final Logger log = Logger.getLogger(LevelDBBackendTest.class);
	private BackendDB db = null;
	private String path = "/tmp/leveldbtest";

	@BeforeClass
	public void setUp() throws Exception
	{
		StatsObject.getInstance().clear();

		File dbFile = new File(path);

		if (dbFile.exists())
		{
			FileUtils.deleteDirectory(dbFile);
		}

		LevelDBConfig config = new LevelDBConfig();
		config.dbPath = path;
		db = DBFactory.getBackendDBInstance("leveldb", mapper.valueToTree(config));

		assertNotNull(db);
	}

	@Test
	public void testAll() throws Exception
	{
		List<Record> l = new ArrayList<Record>();

		ARecord a = new ARecord();
		a.setAddress("1.1.1.1");
		l.add(a);

		a = new ARecord();
		a.setAddress("2001:fefe");
		l.add(a);

		DNSRecordSet r = new DNSRecordSet();
		r.setRecords(l);
		r.setFqdn("foo.bar.baz");

		assertNull(db.get("foo.bar.baz"));

		db.put(r);
		assertNotNull(db.get("foo.bar.baz"));

		r = db.get("foo.bar.baz");
		assertNotNull(r);
		assertTrue(r.getRecords().size() == 2);
		db.delete("foo.bar.baz");
		assertNull(db.get("foo.bar.baz"));

		StatsObject so = StatsObject.getInstance();
		TreeMap<String, Long> map = new TreeMap<String, Long>(so.getMap());
		assertTrue(mapper.writeValueAsString(map), mapper.writeValueAsString(map).equals("{\"LevelDBBackend.deletes.ok\":1,\"LevelDBBackend.gets.ok\":4,\"LevelDBBackend.puts.ok\":1}"));

	}

	@Test
	public void testShutdown() throws Exception
	{
		db.shutdown();
	}
}
