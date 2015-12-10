package com.atlassian.jira.cluster;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.Nullable;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.util.concurrent.ThreadFactories;

import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.is;
import static java.util.concurrent.TimeUnit.SECONDS;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides an implementation for the {@link com.atlassian.jira.cluster.ClusterMessagingService} that uses OfBiz to
 * manage the message queue in the database and holds any listeners via a weak reference so that it doesn't keep
 * listeners alive across plugin restarts, etc.
 *
 * Polls the database on a regular interval to see if there are actions to perform
 *
 * @since v6.1
 */
public class OfBizMessageHandlerService implements MessageHandlerService
{
    // Constants
    private static final int INITIAL_DELAY = 3;
    private static final int PERIOD = 3;
    private static final int CHANNEL_MAX_LENGTH = 20;
    private static final int MESSAGE_MAX_LENGTH = 200;
    private static final Logger log = Logger.getLogger(OfBizMessageHandlerService.class);

    // Fields
    private final NodeStateManager nodeStateManager;
    private final OfBizClusterMessageStore clusterMessageStore;
    private final ScheduledExecutorService scheduler;
    private final HashMap<String, List<WeakReference<ClusterMessageConsumer>>> listeners;
    private final EventMessageConsumer eventMessageConsumer; // Needed to keep the consumer from being garbage collected.

    private final Map<String, Long> lastMessageProcessedByNodeId = new HashMap<String, Long>();

    @Nullable
    private volatile ScheduledFuture<?> messageHandlerService;

    private final Runnable handler = new Runnable()
    {
        public void run()
        {
            handleReceivedMessages();
        }
    };

    public OfBizMessageHandlerService(final NodeStateManager nodeStateManager, final OfBizClusterMessageStore clusterMessageStore,
            final EventPublisher eventPublisher)
    {
        this.clusterMessageStore = clusterMessageStore;
        this.nodeStateManager = nodeStateManager;
        scheduler = Executors.newScheduledThreadPool(1, ThreadFactories.namedThreadFactory("ClusterMessageHandlerServiceThread"));
        listeners = new HashMap<String, List<WeakReference<ClusterMessageConsumer>>>();
        eventMessageConsumer = new EventMessageConsumer(eventPublisher);
        registerListener(EventMessageConsumer.IMPORT_STARTED, eventMessageConsumer);
        registerListener(EventMessageConsumer.IMPORT_FINISHED, eventMessageConsumer);

        // Find the latest message in the messageQueue, we don't want to process stale messages.
        // In other words we start subscribing from now.

        if (getCurrentNode().isClustered())
        {
            for (Node node : nodeStateManager.getAllNodes())
            {
                if (node.isClustered())
                {
                    lastMessageProcessedByNodeId.put(node.getNodeId(), clusterMessageStore.getLatestMessageByNodeId(node.getNodeId()));
                }
            }
        }
    }

    @Override
    @Nullable
    public ClusterMessage sendMessage(final String destinationId, Message message)
    {
        ClusterMessage clusterMessage = null;
        if (getCurrentNode().isClustered())
        {
            final String sourceId = getCurrentNode().getNodeId();
            clusterMessage =  clusterMessageStore.createMessage(sourceId, destinationId, message.toString());
        }
        return clusterMessage;
    }

    @Override
    public List<ClusterMessage> receiveMessages()
    {
        final List<ClusterMessage> allMessages = new ArrayList<ClusterMessage>();
        final Node currentNode = getCurrentNode();
        if (currentNode.isClustered())
        {
            for (Node node : nodeStateManager.getAllNodes())
            {
                if (node.isClustered() && !node.getNodeId().equals(currentNode.getNodeId()))
                {
                    Long startAfterId = lastMessageProcessedByNodeId.get(node.getNodeId());
                    allMessages.addAll(clusterMessageStore.getMessages(node, currentNode, startAfterId));
                }
            }
        }
        return allMessages;
    }

    @Override
    public void start()
    {
        messageHandlerService = scheduler.scheduleAtFixedRate(handler, INITIAL_DELAY, PERIOD, SECONDS);
    }

    @Override
    public void stop()
    {
        if (messageHandlerService != null)
        {
            messageHandlerService.cancel(false);
        }
        scheduler.shutdown();
    }

    private Node getCurrentNode()
    {
        return nodeStateManager.getNode();
    }

    private void handleReceivedMessages()
    {
        try
        {
            for (ClusterMessage message : receiveMessages())
            {
                String channel = message.getMessage().getChannel();
                String supplementalInformation = message.getMessage().getSupplementalInformation();
                String sourceNode = message.getSourceNode();
                try
                {
                    sendLocalFromNode(channel, supplementalInformation, sourceNode);
                }
                catch (Exception e)
                {
                    log.error("There was a problem handling a cluster message", e);
                }
                finally
                {
                    lastMessageProcessedByNodeId.put(sourceNode, message.getId());
                }
            }
        }
        catch (Exception e)
        {
            log.error("There was a problem handling a cluster message", e);
        }
    }

    @Override
    public void registerListener(String channel, ClusterMessageConsumer consumer)
    {
        synchronized(listeners)
        {
            List<WeakReference<ClusterMessageConsumer>> channelListeners = listeners.get(channel);
            if (channelListeners == null)
            {
                channelListeners = new ArrayList<WeakReference<ClusterMessageConsumer>>();
                listeners.put(channel, channelListeners);
            }
            channelListeners.add(new WeakReference<ClusterMessageConsumer>(consumer));
        }
    }

    @Override
    public void unregisterListener(final String channel, final ClusterMessageConsumer consumer)
    {
        synchronized (listeners)
        {
            List<WeakReference<ClusterMessageConsumer>> channelListeners = listeners.get(channel);
            if (channelListeners != null)
            {
                removeRef(channelListeners, consumer);
                if (channelListeners.isEmpty())
                {
                    listeners.remove(channel);
                }
            }
        }
    }

    @Override
    public void unregisterListener(final ClusterMessageConsumer consumer)
    {
        synchronized (listeners)
        {
            for (Iterator<List<WeakReference<ClusterMessageConsumer>>> iterator = listeners.values().iterator(); iterator.hasNext(); )
            {
                final List<WeakReference<ClusterMessageConsumer>> channelListeners = iterator.next();
                removeRef(channelListeners, consumer);
                if (channelListeners.isEmpty())
                {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void sendRemote(String channel, String message)
    {
        notNull("channel", channel);
        is("channel exceeds max length", channel.length() <= CHANNEL_MAX_LENGTH);
        is("message exceeds max length", message.length() <= MESSAGE_MAX_LENGTH);

        sendMessage(ClusterManager.ALL_NODES, new Message(channel, message));
    }

    private void sendLocalFromNode(String channel, String message, String senderId)
    {
        List<ClusterMessageConsumer> channelListeners = new ArrayList<ClusterMessageConsumer>();
        synchronized (listeners)
        {
            List<WeakReference<ClusterMessageConsumer>> channelListenerRefs = listeners.get(channel);
            if (channelListenerRefs == null)
            {
                return;
            }
            for (Iterator<WeakReference<ClusterMessageConsumer>> iterator = channelListenerRefs.iterator(); iterator.hasNext(); )
            {
                WeakReference<ClusterMessageConsumer> consumerReference = iterator.next();
                ClusterMessageConsumer messageConsumer = consumerReference.get();
                if (messageConsumer != null)
                {
                    channelListeners.add(messageConsumer);
                }
                else
                {
                    iterator.remove();
                }
            }
            if (channelListenerRefs.isEmpty())
            {
                listeners.remove(channel);
            }
        }
        for (ClusterMessageConsumer messageConsumer : channelListeners)
        {
            messageConsumer.receive(channel, message, senderId);
        }
    }

    private <T> void removeRef(List<WeakReference<T>> list, T object)
    {
        for (Iterator<WeakReference<T>> iterator = list.iterator(); iterator.hasNext(); )
        {
            final WeakReference<T> ref = iterator.next();
            if (ref.get() == object)
            {
                iterator.remove();
            }
        }
    }
}
