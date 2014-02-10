package org.devnull.darkside.records;

public class CNAMERecord extends Record
{
	private String address = null;

	public CNAMERecord()
	{
	}

	/**
	 * @return the String value of the IP address or null if it is not set
	 */
	public String getAddress()
	{
		return address;
	}

	/**
	 * Sets the address string
	 *
	 * @param address The String of the ip address
	 */
	public void setAddress(final String address)
	{
		if (null == address)
		{
			throw new NullPointerException("address cannot be null");
		}

		this.address = address;
	}
}
