package org.devnull.darkside.records;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Created by dhawthorne on 4/1/14.
 */
public class MXRecordTest
{
	private static final Logger log = Logger.getLogger(MXRecordTest.class);

	@BeforeTest
	public void setup()
	{
		BasicConfigurator.configure();
	}

	@Test
	public void testRecord() throws Exception
	{
		MXRecord r = new MXRecord();
		assertNull(r.getAddress());
		assertEquals(0, r.getPriority());

		try
		{
			r.setAddress(null);
			fail();
		}
		catch (Exception e)
		{
			assertEquals("address cannot be null", e.getMessage());
		}

		r.setAddress("sometext");
		assertEquals(r.getAddress(), "sometext");
		assertEquals(0, r.getPriority());

		r.setAddress("10 sometext");
		assertEquals(r.getAddress(), "sometext");
		assertEquals(10, r.getPriority());

		assertEquals(r.toString(), "{\"type\":\"MX\",\"address\":\"sometext\",\"priority\":10}");
	}
}
