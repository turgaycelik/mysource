package com.atlassian.jira.plugin.ha;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.cluster.ClusterMessage;
import com.atlassian.jira.cluster.ClusterMessagingService;
import com.atlassian.jira.cluster.Message;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 *
 *
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestPluginSyncService
{
    @Mock
    private ReplicatedPluginManager mockReplicatedPluginManager;

    @Mock
    private EventPublisher mockEventPublisher;

    @Mock
    private ClusterMessagingService mockClusterMessagingService;

    final String[] shouldBeFilteredMessageStrings = new String[] {
            "Plugin event:-PLUGIN_MODULE_DISABLED-com.atlassian.jira.dev.reference-plugin:reference-userpicker",
            "Plugin event:-PLUGIN_DISABLED-com.atlassian.jira.dev.reference-plugin",
            "Plugin event:-PLUGIN_UPGRADED-com.atlassian.jira.dev.reference-plugin",
            "Plugin event:-PLUGIN_MODULE_ENABLED-com.atlassian.jira.dev.reference-plugin:refentity-list",
            "Plugin event:-PLUGIN_INSTALLED-com.atlassian.jira.dev.installed-plugin",
            "Plugin event:-PLUGIN_UNINSTALLED-com.atlassian.jira.dev.uninstalled-plugin"
    };



    private List<ClusterMessage> setupAllMessages()
    {
        List<ClusterMessage> messages = Lists.newArrayList();
        long count = 0;
        for (String str : shouldBeFilteredMessageStrings)
        {
            messages.add(new ClusterMessage(count++, "1", "2", null, Message.fromString(str), new Timestamp(new Date().getTime())));
        }
        return messages;
    }



    @Test
    public void testPluginSyncService()
    {
        PluginSyncService syncService =  new DefaultPluginSyncService(new MessageEventRegistry(mockReplicatedPluginManager), mockClusterMessagingService);

        syncService.syncPlugins(setupAllMessages());
        verify(mockReplicatedPluginManager).upgradePlugin("com.atlassian.jira.dev.reference-plugin");
        verify(mockReplicatedPluginManager).disablePlugin("com.atlassian.jira.dev.reference-plugin");
        verify(mockReplicatedPluginManager).disablePluginModule("com.atlassian.jira.dev.reference-plugin:reference-userpicker");
        verify(mockReplicatedPluginManager).enablePluginModule("com.atlassian.jira.dev.reference-plugin:refentity-list");
        verify(mockReplicatedPluginManager).installPlugin("com.atlassian.jira.dev.installed-plugin");
        verify(mockReplicatedPluginManager).uninstallPlugin("com.atlassian.jira.dev.uninstalled-plugin");
        verifyNoMoreInteractions(mockReplicatedPluginManager);
    }

    @Test
    public void testWithEmptyList()
    {
        PluginSyncService syncService =  new DefaultPluginSyncService(new MessageEventRegistry(mockReplicatedPluginManager), mockClusterMessagingService);

        syncService.syncPlugins(Lists. <ClusterMessage>newArrayList());
        verifyNoMoreInteractions(mockReplicatedPluginManager);
    }
}
