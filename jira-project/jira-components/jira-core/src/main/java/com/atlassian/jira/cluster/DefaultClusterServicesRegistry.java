package com.atlassian.jira.cluster;

import com.atlassian.jira.index.ha.NodeReindexService;
import com.atlassian.jira.plugin.ha.PluginMessageSender;
import com.atlassian.jira.util.ComponentLocator;

/**
 * Simple registry for clustered services.
 *
 * @since v6.1
 */
public class DefaultClusterServicesRegistry implements ClusterServicesRegistry
{
    private final MessageHandlerService messageHandlerService;
    private final ComponentLocator componentLocator;
    private volatile NodeReindexService nodeReindexService;
    private volatile PluginMessageSender pluginMessageSender;

    public DefaultClusterServicesRegistry(final MessageHandlerService messageHandlerService, final ComponentLocator componentLocator)
    {
        this.messageHandlerService = messageHandlerService;
        this.componentLocator = componentLocator;
    }


    @Override
    public NodeReindexService getNodeReindexService()
    {
        if (nodeReindexService == null)
        {
            nodeReindexService = componentLocator.getComponent(NodeReindexService.class);
        }
        return nodeReindexService;
    }

    @Override
    public MessageHandlerService getMessageHandlerService()
    {
        return messageHandlerService;
    }

    @Override
    public PluginMessageSender getPluginMessageSender()
    {
        if (pluginMessageSender == null)
        {
            pluginMessageSender = componentLocator.getComponent(PluginMessageSender.class);
        }
        return pluginMessageSender;
    }
}
