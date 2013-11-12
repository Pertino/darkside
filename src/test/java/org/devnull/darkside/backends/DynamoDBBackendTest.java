package org.devnull.darkside.backends;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import org.devnull.darkside.*;
import org.devnull.darkside.configs.DynamoConfig;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.devnull.statsd_client.StatsObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import static org.testng.AssertJUnit.*;

public class DynamoDBBackendTest extends JsonBase
{
	private static final Logger log = Logger.getLogger(DynamoDBBackendTest.class);
	private BackendDB db = null;

	@BeforeClass
	public void setUp() throws Exception
	{
		StatsObject.getInstance().clear();

		Properties logProperties = new Properties();

		logProperties.put("log4j.rootLogger", "DEBUG, stdout");
		logProperties.put("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
		logProperties.put("log4j.appender.stdout.layout", "org.apache.log4j.EnhancedPatternLayout");
		logProperties.put("log4j.appender.stdout.layout.ConversionPattern", "%d [%F:%L] [%p] %C: %m%n");
		logProperties.put("log4j.appender.stdout.immediateFlush", "true");

		BasicConfigurator.resetConfiguration();
		PropertyConfigurator.configure(logProperties);

		/**
		 * make sure localhost dynamo is running.
		 * delete and recreate table in dynamo.
		 * set up and fetch a db instance using DBFactory.
		 */

		/**
		 * This is a weakness to have to depend on an external process.
		 */
		try
		{
			Socket s = new Socket();
			s.connect(new InetSocketAddress("localhost", 8000));
		}
		catch (IOException e)
		{
			assertTrue("localhost dynamo instance is not running", false);
			e.printStackTrace();
		}

		AmazonDynamoDBClient client = new AmazonDynamoDBClient(new BasicAWSCredentials("foo", "bar"));
		client.setEndpoint("http://localhost:8000");

		try
		{
			client.deleteTable(new DeleteTableRequest("fqdn"));
			log.debug("Waiting for table to be deleted");

			//
			// I give it 30 seconds
			//
			long startTime = System.currentTimeMillis();
			long endTime = startTime + (60 * 1000);

			while (System.currentTimeMillis() < endTime)
			{
				try
				{
					log.debug("fetching table status");

					DescribeTableRequest request = new DescribeTableRequest().withTableName("fqdn");
					TableDescription tableDescription = client.describeTable(request).getTable();
					String tableStatus = tableDescription.getTableStatus();

					log.debug("table status: " + tableStatus);

					if (tableStatus.equals(TableStatus.ACTIVE.toString()))
					{
						break;
					}
				}
				catch (ResourceNotFoundException e)
				{
					break;
				}

				try
				{
					Thread.sleep(1000);
				}
				catch (Exception e)
				{
				}
			}

			if (System.currentTimeMillis() > endTime)
			{
				assertTrue("table was not deleted in time", false);
			}
		}
		catch (ResourceNotFoundException rnfe)
		{

		}
		catch (Exception e)
		{
			assertTrue(e.getMessage(), false);
		}

		//
		// table is deleted, recreate it
		//
		try

		{
			List<KeySchemaElement> schema = new ArrayList<KeySchemaElement>();

			ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

			schema.add(new KeySchemaElement().withAttributeName("fqdn").withKeyType(KeyType.HASH));

			attributeDefinitions.add(
				new AttributeDefinition().
					withAttributeName("fqdn").
					withAttributeType(ScalarAttributeType.S)
			);

			client.createTable(
				new CreateTableRequest().
					withTableName("fqdn").
					withKeySchema(schema).
					withAttributeDefinitions(attributeDefinitions).
					withProvisionedThroughput(new ProvisionedThroughput(5L, 5L))
			);
		}
		catch (Exception e)
		{
			assertTrue(e.getMessage(), false);
		}

		//
		// table is now created, ready for testing
		//
		DynamoConfig config = new DynamoConfig();
		config.accessKey = "foo";
		config.secretKey = "bar";
		config.endPoint = "http://localhost:8000";

		db = DBFactory.getBackendDBInstance("dynamo", mapper.valueToTree(config));

		assertNotNull(db);

		log.info("finished with setup");
	}

	@Test
	public void testAll() throws Exception
	{
		List<IPRecord> l = new ArrayList<IPRecord>();
		l.add(new IPRecord("1.1.1.1"));
		l.add(new IPRecord("2001::fefe"));
		DNSRecord r = new DNSRecord();
		r.setRecords(l);

		assertNull(db.get("foo.bar.baz"));

		db.put("foo.bar.baz", r);
		assertNotNull(db.get("foo.bar.baz"));

		r = db.get("foo.bar.baz");
		assertTrue(r.getRecords().size() == 2);

		db.delete("foo.bar.baz");
		assertNull(db.get("foo.bar.baz"));

		StatsObject so = StatsObject.getInstance();
		TreeMap<String, Long> map = new TreeMap<String, Long>(so.getMap());
		assertTrue(mapper.writeValueAsString(map), mapper.writeValueAsString(map).equals("{\"DynamoDBBackend.deletes.ok\":1,\"DynamoDBBackend.gets.ok\":4,\"DynamoDBBackend.puts.ok\":1,\"DynamoDBBackend.puts.total\":1}"));
	}

	@AfterClass
	public void testShutdown() throws Exception
	{
		db.shutdown();
	}
}
