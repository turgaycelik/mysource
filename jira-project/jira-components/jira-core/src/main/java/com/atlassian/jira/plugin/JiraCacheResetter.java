package com.atlassian.jira.plugin;

import com.atlassian.annotations.Internal;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ComponentManagerShutdownEvent;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.portal.FlushablePortletConfigurationStore;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * A component that maintains resets main application caches upon plugin events.
 *
 * @since v5.0
 */
@Internal
public class JiraCacheResetter
{
    private final Delegate delegate;

    public JiraCacheResetter(EventPublisher eventPublisher)
    {
        // hard reference
        this.delegate = new Delegate();
        eventPublisher.register(delegate);
    }

    /**
     * Avoiding passing unfinished this to event manager.
     * Note: This delegate *must* remain static for thread safety!
     */
    public static class Delegate
    {
        private volatile boolean jiraIsShuttingDown = false;

        @EventListener
        public void onPluginDisabled(PluginDisabledEvent pluginDisabledEvent)
        {
            resetCaches();
        }

        @EventListener
        public void onPluginEnabled(PluginEnabledEvent pluginEnabledEvent)
        {
            resetCaches();
        }

        @EventListener
        public void onPluginModuleDisabled(PluginModuleDisabledEvent pluginModuleDisabledEvent)
        {
            resetCaches();
        }

        @EventListener
        public void onPluginModuleEnabled(PluginModuleEnabledEvent pluginModuleEnabledEvent)
        {
            resetCaches();
        }

        @EventListener
        public void onJiraShuttingDown(ComponentManagerShutdownEvent shutdownEvent)
        {
            jiraIsShuttingDown = true;
        }


        // TODO this is certainly toooo coarse-grained, we need to be more picky about which caches we reset
        // vs. disabled/enabled modules
        private void resetCaches()
        {
            // If we are shutting down there is no need to flush the world, which cause a flood of messages in a
            // clustered environment and floods the logs with harmless errors in the HA functests.
            if (jiraIsShuttingDown)
            {
                return;
            }
            // Need to flush custom field manager, field layout manager and column layout manager - so flush the whole field manager
            // JRADEV-12034: When we're in the bootstrap container, these components don't exist, so we need to check for null (until we deal with this in a smarter way)
            final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
            if (customFieldManager != null)
            {
                customFieldManager.refresh();
            }
            final FieldManager fieldManager = ComponentAccessor.getFieldManager();
            if (fieldManager != null)
            {
                fieldManager.refresh();
            }
            final FieldScreenManager screenManager = ComponentAccessor.getFieldScreenManager();
            if (screenManager != null)
            {
                screenManager.refresh();
            }

            //this cache stores direct references to portlets. Clear the cache so these references are not kept. This is SOOOOOOO evil.
            final PortletConfigurationStore store = ComponentAccessor.getComponent(PortletConfigurationStore.class);
            if (store instanceof FlushablePortletConfigurationStore)
            {
                ((FlushablePortletConfigurationStore) store).flush();
            }
            PropertyUtils.clearDescriptors();
        }
    }

}
