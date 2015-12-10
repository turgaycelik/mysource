package com.atlassian.jira.plugin.ha;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.cluster.ClusterMessage;
import com.atlassian.jira.cluster.ClusterMessageConsumer;
import com.atlassian.jira.cluster.ClusterMessagingService;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @since v6.1
 */
public class DefaultPluginSyncService implements PluginSyncService
{
    public final static String PLUGIN_CHANGE = "Plugin event";
    private final MessageEventRegistry eventRegistry;
    private final MessageConsumer messageConsumer; // Needed to keep the consumer from being garbage collected.

    public DefaultPluginSyncService(final MessageEventRegistry eventRegistry, final ClusterMessagingService messagingService)
    {
        this.eventRegistry = eventRegistry;
        messageConsumer = new MessageConsumer(eventRegistry);
        messagingService.registerListener(PLUGIN_CHANGE, messageConsumer);
    }

    @Override
    public void syncPlugins(@Nonnull final List<ClusterMessage> pluginMessages)
    {
        List<PluginOperation> operations = Lists.transform(pluginMessages, new Function<ClusterMessage, PluginOperation>()
        {
            @Override
            public PluginOperation apply(@Nullable final ClusterMessage input)
            {
                if (input == null)
                {
                    return null;
                }
                else
                {
                    return new PluginOperation(input.getMessage().getSupplementalInformation());
                }
            }
        });
        if (!operations.isEmpty())
        {
            firePluginEvents(operations);
        }
    }

    private void firePluginEvents(final List<PluginOperation> filteredOperations)
    {
        // PluginState has changed on the other node, so clear caches

        for (PluginOperation operation: filteredOperations)
        {
            String completeKey = operation.getCompleteKey();
            Function<String, Void> eventFunction = eventRegistry.getEventFunction(operation.getPluginEventType());
            eventFunction.apply(completeKey);
        }
    }

    private static class MessageConsumer implements ClusterMessageConsumer
    {

        private final MessageEventRegistry eventRegistry;

        public MessageConsumer(final MessageEventRegistry eventRegistry)
        {
            this.eventRegistry = eventRegistry;
        }

        @Override
        public void receive(final String channel, final String message, final String senderId)
        {
            if (channel.equals(PLUGIN_CHANGE))
            {
                if (message != null)
                {
                    PluginOperation operation = new PluginOperation(message);
                    String completeKey = operation.getCompleteKey();
                    Function<String, Void> eventFunction = eventRegistry.getEventFunction(operation.getPluginEventType());
                    eventFunction.apply(completeKey);
                }
            }
        }
    }
}
