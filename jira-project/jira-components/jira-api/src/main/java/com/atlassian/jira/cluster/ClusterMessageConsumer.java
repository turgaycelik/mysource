package com.atlassian.jira.cluster;

import com.atlassian.annotations.ExperimentalSpi;

/**
 * The interface to be implemented by classes wishing to listen to messages sent to a cluster. Implementing classes
 * need to be registered as listeners with the {@link com.atlassian.jira.cluster.ClusterMessagingService}.
 *
 * @since v6.3
 */
@ExperimentalSpi
public interface ClusterMessageConsumer
{
    /**
     * Called by the ClusterMessagingService when there is a message waiting to be processed by a node.
     *
     * @param channel The channel the message was sent to
     * @param message The content of the message
     * @param senderId The id of the node that raised the message
     */
    void receive(String channel, String message, String senderId);
}
