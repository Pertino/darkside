package org.devnull.darkside;

import org.testng.annotations.Test;

import java.util.*;

import static org.testng.AssertJUnit.*;

public class DNSRecordTest
{
	@Test
	public void testGetTtl() throws Exception
	{
		DNSRecord r = new DNSRecord();
		assertNull(r.getTtl());
	}

	@Test
	public void testSetTtl() throws Exception
	{
		DNSRecord r = new DNSRecord();
		r.setTtl(200);
		assertTrue(r.getTtl() == 200);
	}

	@Test
	public void testGetRecords() throws Exception
	{
		DNSRecord r = new DNSRecord();
		assertNull(r.getRecords());
	}

	@Test
	public void testSetRecords() throws Exception
	{
		DNSRecord r = new DNSRecord();
		r.setRecords(new ArrayList<IPRecord>());
		assertNotNull(r.getRecords());
		assertTrue(r.getRecords().size() == 0);
	}

	@Test
	public void testGetFqdn() throws Exception
	{
		DNSRecord r = new DNSRecord();
		assertNull(r.getFqdn());
	}

	@Test
	public void testSetFqdn() throws Exception
	{
		DNSRecord r = new DNSRecord();
		r.setFqdn("foobar");
		assertNotNull(r.getFqdn());
		assertTrue(r.getFqdn().equals("foobar"));
	}
}
