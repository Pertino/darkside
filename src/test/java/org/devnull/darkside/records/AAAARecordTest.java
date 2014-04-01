package org.devnull.darkside.records;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Created by dhawthorne on 4/1/14.
 */
public class AAAARecordTest
{
	private static final Logger log = Logger.getLogger(AAAARecordTest.class);

	@BeforeTest
	public void setup()
	{
		BasicConfigurator.configure();
	}

	@Test
	public void testRecord() throws Exception
	{
		AAAARecord r = new AAAARecord();
		assertTrue(r.getAddress() == null);
		r.setAddress("2001::fefe");
		assertEquals(r.getAddress(), "2001::fefe");

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
			r.setAddress("1.1.1.1");
			fail();
		}
		catch (Exception e)
		{
			assertEquals("IPv6 address is malformed", e.getMessage());
		}

		try
		{
			r.setAddress("some text");
			fail();
		}
		catch (Exception e)
		{
			assertEquals("IPv6 address is malformed", e.getMessage());
		}

		assertEquals(r.toString(), "{\"type\":\"AAAA\",\"address\":\"2001::fefe\"}");
	}
}
