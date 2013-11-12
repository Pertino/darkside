package org.devnull.darkside;

public interface BackendDB
{
	public void put(final String fqdn, final DNSRecord record) throws Exception;
	public DNSRecord get(final String fqdn) throws Exception;
	public void delete(final String fqdn) throws Exception;
	public void shutdown();
}
