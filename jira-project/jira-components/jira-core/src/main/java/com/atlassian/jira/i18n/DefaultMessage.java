package com.atlassian.jira.i18n;

import com.atlassian.sal.api.message.Message;

import java.io.Serializable;

/**
 * Copied from sal-core
 *
 * @since v5.0
 */
public class DefaultMessage implements Message
{
	private final Serializable[] arguments;
	private String key;

	public DefaultMessage(String key, Serializable... arguments)
	{
		this.key = key;
		this.arguments = arguments;
	}

	public Serializable[] getArguments()
	{
		return arguments;
	}

	public String getKey()
	{
		return key;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(key);
		builder.append(": ");
		for (Serializable argument : arguments)
		{
			builder.append(argument);
			builder.append(",");
		}
		return builder.toString();
	}
}
