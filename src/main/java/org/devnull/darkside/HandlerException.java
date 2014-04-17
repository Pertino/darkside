package org.devnull.darkside;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
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
