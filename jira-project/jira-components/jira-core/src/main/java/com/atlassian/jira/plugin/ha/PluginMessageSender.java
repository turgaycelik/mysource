package com.atlassian.jira.plugin.ha;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.ClusterServicesRegistry;
import com.atlassian.jira.cluster.Message;
import com.atlassian.jira.cluster.MessageHandlerService;

/**
 * Sends  messages across the cluster for specific plugin events
 *
 * @since v6.1
 */
public class PluginMessageSender
{
    private final MessageHandlerService messageHandlerService;
    private volatile boolean canSendMessages;

    public PluginMessageSender(final ClusterServicesRegistry clusterServicesRegistry)
    {
        this.messageHandlerService = clusterServicesRegistry.getMessageHandlerService();
    }

    public void sendPluginModuleEnabledMessage(String completeKey)
    {
        sendPluginMessage(serializeAsString(PluginEventType.PLUGIN_MODULE_ENABLED, completeKey));
    }

    public void sendPluginModuleDisabledMessage(String completeKey)
    {
        sendPluginMessage(serializeAsString(PluginEventType.PLUGIN_MODULE_DISABLED, completeKey));
    }

    public void sendPluginEnabledMessage(String key)
    {
        sendPluginMessage(serializeAsString(PluginEventType.PLUGIN_ENABLED, key));
    }

    public void sendPluginDisabledMessage(String key)
    {
        sendPluginMessage(serializeAsString(PluginEventType.PLUGIN_DISABLED, key));
    }

    public void sendPluginUninstalledMessage(String key)
    {
        sendPluginMessage(serializeAsString(PluginEventType.PLUGIN_UNINSTALLED, key));
    }

    public void sendPluginUpgradedMessage(String key)
    {
        sendPluginMessage(serializeAsString(PluginEventType.PLUGIN_UPGRADED, key));
    }

    public void sendPluginInstalledMessage(String key)
    {
        sendPluginMessage(serializeAsString(PluginEventType.PLUGIN_INSTALLED, key));
    }

    public void activate()
    {
        canSendMessages = true;
    }

    public void stop()
    {
        canSendMessages = false;
    }

    private String serializeAsString(PluginEventType pluginEventType, String pluginKey)
    {
        return pluginEventType + "-" + pluginKey;
    }

    private void sendPluginMessage(final String supplementalInfo)
    {
        if (canSendMessages)
        {
            final Message message = new Message(DefaultPluginSyncService.PLUGIN_CHANGE, supplementalInfo);
            messageHandlerService.sendMessage(ClusterManager.ALL_NODES, message);
        }
    }
}
