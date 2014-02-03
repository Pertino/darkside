package org.devnull.darkside;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.devnull.darkside.records.ARecord;
import org.devnull.darkside.records.Record;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class RecordTests
{
	@Test
	public void testGetAddress() throws Exception
	{
		ARecord r = new ARecord();
		assertTrue(r.getAddress() == null);
	}

	@Test
	public void testSetAddress() throws Exception
	{
		ARecord r = new ARecord();
		r.setAddress("1.1.1.1");
		assertTrue(r.getAddress().equals("1.1.1.1"));
	}

	@Test
	public void testGetType() throws Exception
	{
		ARecord r = new ARecord();
		// assertTrue(r.getType().equals("A"));
		r.setAddress("1.1.1.1");

		try
		{
			r.setAddress("2001::fefe");
		}
		catch (Exception e)
		{
			assertEquals("address is not valid: 2001::fefe", e.getMessage());
		}
	}
}
