package com.atlassian.jira.cluster;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.dataimport.ha.RemoteImportCompletedEvent;
import com.atlassian.jira.bc.dataimport.ha.RemoteImportStartedEvent;

/**
 * Consumes cluster messages and pushes them out to the EventPublisher.
 *
 * @since v6.3
 */
public class EventMessageConsumer implements ClusterMessageConsumer
{
    public final static String IMPORT_STARTED = "Import Started";
    public final static String IMPORT_FINISHED = "Import Done";
    private final EventPublisher eventPublisher;

    public EventMessageConsumer(final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    public static Message importFinishedMessage(final Boolean success)
    {
        return new Message(IMPORT_FINISHED, success.toString());
    }

    public static Message importStartedMessage()
    {
        return new Message(IMPORT_STARTED, null);
    }

    @Override
    public void receive(final String channel, final String message, final String senderId)
    {
        if (channel.equals(IMPORT_STARTED))
        {
            eventPublisher.publish(new RemoteImportStartedEvent());
        }
        else if (channel.equals(IMPORT_FINISHED))
        {
            eventPublisher.publish(new RemoteImportCompletedEvent(message));
        }
    }
}
