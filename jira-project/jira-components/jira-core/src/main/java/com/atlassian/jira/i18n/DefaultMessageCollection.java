package com.atlassian.jira.i18n;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Copied from sal-core
 *
 * @since v5.0
 */
public class DefaultMessageCollection implements MessageCollection
{
	private final List<Message> messages = new ArrayList<Message>();

	public void addMessage(String key, Serializable... arguments)
	{
		addMessage(new DefaultMessage(key, arguments));
	}

	public void addMessage(Message message)
	{
		messages.add(message);
	}

	public boolean isEmpty()
	{
		return messages.isEmpty();
	}

	public List<Message> getMessages()
	{
//		return Collections.unmodifiableList(messages); // issues with xstream
		return messages;
	}

	public void addAll(List<Message> remoteMessages)
	{
		messages.addAll(remoteMessages);
	}
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (Message message : messages)
		{
			builder.append(message);
			builder.append("\n");
		}
		return builder.toString();
	}
}