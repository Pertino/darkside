package org.devnull.darkside;

public interface BackendDB
{
	public void put(final DNSRecordSet record) throws Exception;

	public DNSRecordSet get(final String fqdn) throws Exception;

	public void delete(final String fqdn) throws Exception;

	public void shutdown();
}
