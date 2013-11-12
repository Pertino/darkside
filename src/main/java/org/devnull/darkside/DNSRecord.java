package org.devnull.darkside;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class DNSRecord extends JsonBase
{
	/**
	 * A copy of the fqdn that this record is for
	 */
	private String fqdn = null;

	/**
	 * default TTL for responses is configurable in the DarksideConfig, and is
	 * used when the record is served if this value is still null, not when it 
	 * is stored.
	 */
	private Integer ttl = null;

	/**
	 * list of IP addresses associated with the fqdn this DNSRecord is for
	 */
	private List<IPRecord> records = null;

	public Integer getTtl()
	{
		return ttl;
	}

	public void setTtl(Integer ttl)
	{
		this.ttl = ttl;
	}

	public List<IPRecord> getRecords()
	{
		return records;
	}

	public void setRecords(List<IPRecord> records)
	{
		this.records = records;
	}

	public String getFqdn()
	{
		return fqdn;
	}

	public void setFqdn(String fqdn)
	{
		this.fqdn = fqdn;
	}
}
