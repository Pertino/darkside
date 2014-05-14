package org.devnull.darkside.backends;

import com.fasterxml.jackson.databind.JsonNode;
import org.devnull.darkside.DNSRecordSet;

public interface BackendDB
{
    public void configure(final JsonNode configNode) throws Exception;

	public void put(final DNSRecordSet record) throws Exception;

	public DNSRecordSet get(final String fqdn) throws Exception;

	public void delete(final String fqdn) throws Exception;

	public void shutdown();
}
