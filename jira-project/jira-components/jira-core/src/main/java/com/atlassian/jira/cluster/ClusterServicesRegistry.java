package com.atlassian.jira.cluster;

import com.atlassian.jira.index.ha.NodeReindexService;
import com.atlassian.jira.plugin.ha.PluginMessageSender;

/**
 * Holds the cluster specific services
 *
 * @since v6.1
 */
public interface ClusterServicesRegistry
{
    NodeReindexService getNodeReindexService();

    MessageHandlerService getMessageHandlerService();

    PluginMessageSender getPluginMessageSender();
}
