package org.devnull.darkside;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.devnull.darkside.configs.DynamoConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateDynamoTable extends JsonBase
{
	private static Logger log = Logger.getLogger(CreateDynamoTable.class);

	/**
	 * Reads command line arguments and creates a table in Dynamo.  The configuration file for
	 * this script is the exact same as the DynamoConfig object used in a Darkside conf file for
	 * accessing dynamo.
	 * <p/>
	 * Command line args:
	 * <p/>
	 * -c <dynamo.conf>		Path to the json-based configuration file, required
	 * -l <log4j.conf>		Path to a log4j properties file, optional
	 *
	 * @param args The String[] array of the command-line arguments
	 * @throws Exception If there is an error
	 */
	public static void main(String[] args) throws Exception
	{
		DynamoConfig config = null;

		try
		{
			BasicConfigurator.configure();

			for (int i = 0; i < args.length; i++)
			{
				if (args[i].trim().equals("-c"))
				{
					String configFile = args[i + 1];
					config = mapper.readValue(new File(configFile), DynamoConfig.class);
					i++;
				}
				else if (args[i].trim().equals("-l"))
				{
					i++;
					setupLogging(args[i]);
				}
				else
				{
					log.info("Unknown command line argument: " + args[i]);
				}
			}

			if (null == config)
			{
				throw new IllegalArgumentException("No configuration file specified on command line");
			}

			if (null == config.endPoint)
			{
				throw new IllegalArgumentException("endPoint was not specified in the config file");
			}

			if (null == config.accessKey)
			{
				throw new IllegalArgumentException("accessKey was not specified in the config file");
			}

			if (null == config.secretKey)
			{
				throw new IllegalArgumentException("secretKey was not specified in the config file");
			}

			if (null == config.tableName)
			{
				throw new IllegalArgumentException("tableName was not specified in the config file");
			}

			List<KeySchemaElement> schema = new ArrayList<KeySchemaElement>();

			ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

			schema.add(new KeySchemaElement().withAttributeName("fqdn").withKeyType(KeyType.HASH));

			attributeDefinitions.add(
				new AttributeDefinition().
					withAttributeName("fqdn").
					withAttributeType(ScalarAttributeType.S)
			);

			AmazonDynamoDBClient client = new AmazonDynamoDBClient(
				new BasicAWSCredentials(config.accessKey, config.secretKey));
			client.setEndpoint(config.endPoint);

			client.createTable(
				new CreateTableRequest().
					withTableName(config.tableName).
					withKeySchema(schema).
					withAttributeDefinitions(attributeDefinitions).
					withProvisionedThroughput(new ProvisionedThroughput(5L, 5L))
			);
		}
		catch (Exception e)
		{
			log.error(e);
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}

	/**
	 * Configures log4j using the passed-in log4j.conf properties file.
	 *
	 * @param log4jConfig Path to the log4j config file on disk.
	 */
	private static void setupLogging(final String log4jConfig)
	{
		LogManager.resetConfiguration();

		if (null != log4jConfig)
		{
			//
			// load log4j config file
			//
			PropertyConfigurator.configure(log4jConfig);
		}

		log = Logger.getLogger(CreateDynamoTable.class);
	}
}
