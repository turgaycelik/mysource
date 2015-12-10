package com.atlassian.jira.cluster;

import java.util.List;

/**
 * Synchronously send and receive messages
 *
 * @since v6.1
 */
public interface MessageHandlerService extends ClusterMessagingService
{
    public ClusterMessage sendMessage(String destinationNode, Message message);

    public List<ClusterMessage> receiveMessages();

    public void start();

    public void stop();
}
