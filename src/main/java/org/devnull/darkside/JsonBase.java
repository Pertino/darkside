package org.devnull.darkside;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

/**
 * JsonBase is a utility class that most classes will extend because it makes serializing them with toString()
 * much more consistent, which is very useful for debugging and unit testing, and saves us from having to manually
 * format toString for everything.
 */
public abstract class JsonBase
{
	protected static final ObjectMapper mapper = new ObjectMapper();

	static
	{
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
	}

	public String toString()
	{
		try
		{
			return mapper.writeValueAsString(this);
		}
		catch (IOException e)
		{
			return "unable to write value as string: " + e.getMessage();
		}
	}
}

