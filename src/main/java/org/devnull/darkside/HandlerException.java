package org.devnull.darkside;

public final class HandlerException extends JsonBase
{
	public String error = null;

	public HandlerException()
	{
	}

	public HandlerException(String s)
	{
		error = s;
	}
}
