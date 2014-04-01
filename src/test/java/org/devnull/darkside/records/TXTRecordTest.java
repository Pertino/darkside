package org.devnull.darkside.records;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Created by dhawthorne on 4/1/14.
 */
public class TXTRecordTest
{
	private static final Logger log = Logger.getLogger(TXTRecordTest.class);

	@BeforeTest
	public void setup()
	{
		BasicConfigurator.configure();
	}

	@Test
	public void testRecord() throws Exception
	{
		TXTRecord r = new TXTRecord();
		assertNull(r.getAddress());

		try
		{
			r.setAddress(null);
			fail();
		}
		catch (Exception e)
		{
			assertEquals("record cannot be null", e.getMessage());
		}

		r.setAddress("sometext");
		assertEquals(r.getAddress(), "sometext");

		assertEquals(r.toString(), "{\"type\":\"TXT\",\"address\":\"sometext\"}");
	}
}
