package org.devnull.darkside;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

/**
 * IPRecord encapsulates the string of the IP address and the type: A or AAAA used
 * in the DNS reply.  The type is set automatically when the address is set.
 */
public class IPRecord extends JsonBase
{
	private String address = null;
	private String type = "A";

	public IPRecord()
	{
	}

	/**
	 * constructor used during testing
	 *
	 * @param ip	the string representation of the ip address, e.g. "1.1.1.1"
	 * @throws Exception if the ip is null
	 */
	public IPRecord(final String ip) throws Exception
	{
		if (ip == null)
		{
			throw new NullPointerException("ip is null");
		}

		setAddress(ip);
	}

	/**
	 * @return the String value of the IP address or null if it is not set
	 */
	@DynamoDBAttribute
	public String getAddress()
	{
		return address;
	}

	/**
	 * Sets the address string and determines if it is IPv4 or IPv6
	 *
	 * @param address	The String of the ip address
	 */
	public void setAddress(final String address)
	{
		this.address = address;

		if (address.contains(":"))
		{
			type = "AAAA";
		}
	}

	/**
	 * @return The String of the type: A or AAAA
	 */
	@DynamoDBAttribute
	public String getType()
	{
		return type;
	}

}
