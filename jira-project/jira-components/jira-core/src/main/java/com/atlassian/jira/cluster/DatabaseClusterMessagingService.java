package com.atlassian.jira.cluster;

/**
 * Provides an implementation for the {@link com.atlassian.jira.cluster.ClusterMessagingService} that wraps an existing
 * {@link MessageHandlerService} in order to provide an API that can be consumed by plugins.
 *
 * @since v6.3
 */
public class DatabaseClusterMessagingService implements ClusterMessagingService
{
    private final MessageHandlerService messageHandlerService;

    public DatabaseClusterMessagingService(MessageHandlerService messageHandlerService)
    {
        this.messageHandlerService = messageHandlerService;
    }

    @Override
    public void registerListener(final String channel, final ClusterMessageConsumer consumer)
    {
        messageHandlerService.registerListener(channel, consumer);
    }

    @Override
    public void unregisterListener(final String channel, final ClusterMessageConsumer consumer)
    {
        messageHandlerService.unregisterListener(channel, consumer);
    }

    @Override
    public void unregisterListener(final ClusterMessageConsumer consumer)
    {
        messageHandlerService.unregisterListener(consumer);
    }

    @Override
    public void sendRemote(final String channel, final String message)
    {
        messageHandlerService.sendRemote(channel, message);
    }
}
