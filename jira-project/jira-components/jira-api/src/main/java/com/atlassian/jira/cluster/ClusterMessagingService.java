package com.atlassian.jira.cluster;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Service responsible for notifying {@link com.atlassian.jira.cluster.ClusterMessageConsumer} instances when
 * messages are waiting to be processed. Also provides a mechanism for broadcasting messages to a channel.
 *
 * @since v6.3
 */
@ExperimentalApi
public interface ClusterMessagingService
{
    /**
     * Register a ClusterMessageConsumer to receive messages sent to the nominated channel.
     *
     * The receive method of the ClusterMessageConsumer will be invoked when a message is sent to a channel
     * it is listening to. Registering a new listener for a channel will not remove any existing listeners on that
     * channel. Listeners are weakly referenced, it is the responsibility of the caller to ensure that listeners
     * are not prematurely garbage collected.
     *
     * @param channel The name of the channel
     * @param consumer The consumer that will receive messages
     */
    void registerListener(String channel, ClusterMessageConsumer consumer);

    /**
     * Removes a ClusterMessageConsumer from the channel.
     *
     * The ClusterMessageConsumer will no longer receive messages that are sent to the nominated channel.
     *
     * @param channel The name of the channel
     * @param consumer The consumer to be unregistered
     */
    void unregisterListener(String channel, ClusterMessageConsumer consumer);

    /**
     * Removes a ClusterMessageConsumer from all channels.
     *
     * The ClusterMessageConsumer will no longer receive any messages.
     *
     * @param consumer The consumer to be unregistered
     */
    void unregisterListener(ClusterMessageConsumer consumer);

    /**
     * Sends an inter-node message to registered listeners, listeners on the node the message was raised will not be
     * notified.
     *
     * @param channel The name of the channel (up to 20 alphanumeric characters in length)
     * @param message The message to send (up to 200 characters in length)
     */
    void sendRemote(String channel, String message);
}
