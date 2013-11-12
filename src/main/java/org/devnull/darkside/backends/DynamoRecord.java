package org.devnull.darkside.backends;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * This is a class that encapsulates the json-as-string DNSRecord into
 * a simple string: string attribute, to get around the fact that dynamo
 * mapper can't handle nested complex objects.
 */
@DynamoDBTable(tableName = "fqdn")
public class DynamoRecord
{
	private String fqdn = null;
	private String record = null;

	@DynamoDBHashKey(attributeName = "fqdn")
	public String getFqdn()
	{
		return fqdn;
	}

	public void setFqdn(String fqdn)
	{
		this.fqdn = fqdn;
	}

	@DynamoDBAttribute
	public String getRecord()
	{
		return record;
	}

	public void setRecord(String record)
	{
		this.record = record;
	}
}
