package org.devnull.darkside.backends;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.Logger;
import org.devnull.darkside.DNSRecordSet;
import org.devnull.darkside.JsonBase;
import org.devnull.darkside.configs.DynamoConfig;
import org.devnull.statsd_client.StatsObject;

public class DynamoDBBackend extends JsonBase implements BackendDB
{
	private AmazonDynamoDB dynamo = null;
	private DynamoDBMapper dynamoMapper = null;
	private static final Logger log = Logger.getLogger(BackendDB.class);
	private static final StatsObject so = StatsObject.getInstance();

	public DynamoDBBackend()
    {
    }

    public void configure(final JsonNode configNode) throws Exception
	{
		DynamoConfig config = mapper.treeToValue(configNode, DynamoConfig.class);
		dynamo = new AmazonDynamoDBClient();

		//
		// this is provided as a way to override which dynamo server VIP to talk to, and is used
		// in the unit testing of this class.
		//
		if (config.endPoint != null)
		{
			dynamo.setEndpoint(config.endPoint);
		}

        /*
		DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig(
			new DynamoDBMapperConfig.TableNameOverride(config.tableName)
		);
		*/

        DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig(
                DynamoDBMapperConfig.SaveBehavior.valueOf("CLOBBER"),
                DynamoDBMapperConfig.ConsistentReads.valueOf("CONSISTENT"),
                new DynamoDBMapperConfig.TableNameOverride(config.tableName)
        );

		dynamoMapper = new DynamoDBMapper(dynamo, mapperConfig);

		DescribeTableRequest dtr = new DescribeTableRequest().withTableName(config.tableName);
		TableDescription tableDescription = dynamo.describeTable(dtr).getTable();
		String tableStatus = tableDescription.getTableStatus();

		log.debug("status for table " + config.tableName + " is: " + tableStatus);

		if (!tableStatus.equals(TableStatus.ACTIVE.toString()))
		{
			throw new Exception("table does not exist in dynamo");
		}
	}

	public void put(DNSRecordSet record) throws Exception
	{
        long start = System.nanoTime();
		so.increment("DynamoDBBackend.puts.total");

		try
		{
			DynamoRecord r = new DynamoRecord();
			r.setFqdn(record.getFqdn());
			r.setRecord(record.toString());
			dynamoMapper.save(r);
			so.increment("DynamoDBBackend.puts.ok");
		}
		catch (Exception e)
		{
			log.debug("excepting saving record in dynamo: " + e);
			so.increment("DynamoDBBackend.puts.exceptions");
			throw e;
		}
        finally
        {
            so.timing("DynamoDBBackend.put", (System.nanoTime() - start) / 1000);
        }
	}

	public DNSRecordSet get(String fqdn) throws Exception
	{
        long start = System.nanoTime();
        so.increment("DynamoDBBackend.gets.total");

		try
		{
			DynamoRecord r = dynamoMapper.load(DynamoRecord.class, fqdn);
			so.increment("DynamoDBBackend.gets.ok");

			if (r == null)
			{
				return null;
			}

			return mapper.readValue(r.getRecord(), DNSRecordSet.class);
		}
		catch (Exception e)
		{
			log.debug("exception fetching record for fqdn " + fqdn + " from dynamo: " + e);
			so.increment("DynamoDBBackend.gets.exceptions");
			throw e;
		}
        finally
        {
            so.timing("DynamoDBBackend.get", (System.nanoTime() - start) / 1000);
        }
	}

	public void delete(String fqdn) throws Exception
	{
        long start = System.nanoTime();
        so.increment("DynamoDBBackend.deletes.total");

		if (fqdn == null)
		{
			throw new NullPointerException("fqdn argument is null");
		}

		try
		{
			DynamoRecord r = new DynamoRecord();
			r.setFqdn(fqdn);
			dynamoMapper.delete(r);
			so.increment("DynamoDBBackend.deletes.ok");
		}
		catch (Exception e)
		{
			log.debug("exception deleting record for fqdn " + fqdn + " from dynamo: " + e);
			so.increment("DynamoDBBackend.deletes.exceptions");
			throw e;
		}
        finally
        {
            so.timing("DynamoDBBackend.delete", (System.nanoTime() - start) / 1000);
        }
	}

	public void shutdown()
	{
		try
		{
			dynamo.shutdown();
		}
		catch (Exception e)
		{
			log.debug("exception shutting down dynamo client: " + e);
		}
	}
}
