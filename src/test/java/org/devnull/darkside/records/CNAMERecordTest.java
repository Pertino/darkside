package org.devnull.darkside.records;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Created by dhawthorne on 4/1/14.
 */
public class CNAMERecordTest
{
	private static final Logger log = Logger.getLogger(CNAMERecordTest.class);

	@BeforeTest
	public void setup()
	{
		BasicConfigurator.configure();
	}

	@Test
	public void testRecord() throws Exception
	{
		CNAMERecord r = new CNAMERecord();
		assertNull(r.getAddress());

		try
		{
			r.setAddress(null);
			fail();
		}
		catch (Exception e)
		{
			assertEquals("address cannot be null", e.getMessage());
		}

		r.setAddress("some text");
		assertEquals(r.getAddress(), "some text");

		assertEquals(r.toString(), "{\"type\":\"CNAME\",\"address\":\"some text\"}");
	}
}
