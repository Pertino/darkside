package org.devnull.darkside;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.devnull.darkside.records.ARecord;
import org.devnull.darkside.records.Record;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.AssertJUnit.*;

public class DNSRecordSetTest
{
	private static final Logger log = Logger.getLogger(DNSRecordSetTest.class);

	@BeforeTest
	public void setup()
	{
		BasicConfigurator.resetConfiguration();
		PropertyConfigurator.configure("src/test/resources/log4j.conf");
	}

	@Test
	public void testGetTtl() throws Exception
	{
		DNSRecordSet r = new DNSRecordSet();
		assertNull(r.getTtl());
	}

	@Test
	public void testSetTtl() throws Exception
	{
		DNSRecordSet r = new DNSRecordSet();
		r.setTtl(200);
		assertTrue(r.getTtl() == 200);
	}

	@Test
	public void testGetRecords() throws Exception
	{
		DNSRecordSet r = new DNSRecordSet();
		assertNull(r.getRecords());
	}

	@Test
	public void testSetRecords() throws Exception
	{
		DNSRecordSet r = new DNSRecordSet();
		r.setRecords(new ArrayList<Record>());
		assertNotNull(r.getRecords());
		assertTrue(r.getRecords().size() == 0);
	}

	@Test
	public void testGetFqdn() throws Exception
	{
		DNSRecordSet r = new DNSRecordSet();
		assertNull(r.getFqdn());
	}

	@Test
	public void testSetFqdn() throws Exception
	{
		DNSRecordSet r = new DNSRecordSet();
		r.setFqdn("foobar");
		assertNotNull(r.getFqdn());
		assertTrue(r.getFqdn().equals("foobar"));
	}

	@Test
	public void testJsonDeserialization() throws Exception
	{
		ObjectMapper mapper = new ObjectMapper();
		DNSRecordSet s = mapper.readValue("{\"fqdn\":\"foobar\",\"records\":[{\"type\":\"A\",\"address\":\"1.1.1.1\"}],\"ttl\":null}",
						  DNSRecordSet.class);
		assertNotNull(s);

		assertEquals("{\"fqdn\":\"foobar\",\"records\":[{\"type\":\"A\",\"address\":\"1.1.1.1\"}],\"ttl\":null}", s.toString());
		assertEquals(s.getFqdn(), "foobar");
		assertEquals(s.getTtl(), null);
		assertEquals(s.getRecords().size(), 1);
		assertTrue(s.getRecords().get(0) instanceof ARecord);
	}
}
