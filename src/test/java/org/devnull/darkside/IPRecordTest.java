package org.devnull.darkside;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

public class IPRecordTest
{
	@Test
	public void testGetAddress() throws Exception
	{
		IPRecord r = new IPRecord();
		assertTrue(r.getAddress() == null);
	}

	@Test
	public void testSetAddress() throws Exception
	{
		IPRecord r = new IPRecord();
		r.setAddress("1.1.1.1");
		assertTrue(r.getAddress().equals("1.1.1.1"));
	}

	@Test
	public void testGetType() throws Exception
	{
		IPRecord r = new IPRecord();
		assertTrue(r.getType().equals("A"));
		r.setAddress("1.1.1.1");
		assertTrue(r.getType().equals("A"));
		r.setAddress("2001:fefe");
		assertTrue(r.getType().equals("AAAA"));
	}
}
