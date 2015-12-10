package com.atlassian.jira.plugin.ha;

import java.sql.Timestamp;
import java.util.Date;

import com.atlassian.jira.cluster.ClusterMessage;
import com.atlassian.jira.cluster.Message;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * *
 * @since v6.1
 */
public class TestPluginOperation
{
    @Test
    public void testParsing()
    {
        ClusterMessage message = new ClusterMessage(1L, "sourceNode", "anyNode", null,
                new Message(DefaultPluginSyncService.PLUGIN_CHANGE, "PLUGIN_MODULE_ENABLED-com.atlassian.jira.dev.reference-plugin:reference-module"),
                new Timestamp(new Date().getTime()));
        PluginOperation  operation = new PluginOperation(message.getMessage().getSupplementalInformation());
        assertEquals(PluginEventType.PLUGIN_MODULE_ENABLED, operation.getPluginEventType());
        assertEquals("com.atlassian.jira.dev.reference-plugin", operation.getPluginKey());
        assertEquals("reference-module", operation.getModuleKey());
    }

}
