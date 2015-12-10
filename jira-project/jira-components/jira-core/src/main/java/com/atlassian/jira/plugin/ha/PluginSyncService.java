package com.atlassian.jira.plugin.ha;

import com.atlassian.jira.cluster.ClusterMessage;

import java.util.List;

/**
 * Synchronize the state of the plugins with the active node
 *
 * @since v6.1
 */
public interface PluginSyncService
{
    void syncPlugins(List<ClusterMessage> pluginMessages);
}
