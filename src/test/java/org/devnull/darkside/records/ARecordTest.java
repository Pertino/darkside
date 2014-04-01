package org.devnull.darkside.records;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.devnull.darkside.records.ARecord;
import org.devnull.darkside.records.Record;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Created by dhawthorne on 4/1/14.
 */
public class ARecordTest
{
	private static final Logger log = Logger.getLogger(ARecordTest.class);

	@BeforeTest
	public void setup()
	{
		BasicConfigurator.configure();
	}

	@Test
	public void testRecord() throws Exception
	{
		ARecord r = new ARecord();
		assertTrue(r.getAddress() == null);
		r.setAddress("1.1.1.1");
		assertEquals(r.getAddress(), "1.1.1.1");

		try
		{
			r.setAddress(null);
			fail();
		}
		catch (Exception e)
		{
			assertEquals("address cannot be null", e.getMessage());
		}

		try
		{
			r.setAddress("2001::fefe");
			fail();
		}
		catch (Exception e)
		{
			assertEquals("address is not valid: 2001::fefe", e.getMessage());
		}

		try
		{
			r.setAddress("some text");
			fail();
		}
		catch (Exception e)
		{
			assertEquals("address is not valid: some text", e.getMessage());
		}

		assertEquals(r.toString(), "{\"type\":\"A\",\"address\":\"1.1.1.1\"}");
	}
}
