package com.atlassian.jira.startup;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.JiraPluginSystemListener;
import com.atlassian.plugin.event.events.PluginFrameworkDelayedEvent;
import com.atlassian.plugin.event.events.PluginFrameworkShutdownEvent;
import com.atlassian.plugin.event.events.PluginFrameworkStartedEvent;
import com.atlassian.plugin.event.events.PluginFrameworkWarmRestartedEvent;

/**
 * Event listener for internal JIRA events that the JiraStartupChecklist cares about.
 *
 * @see JiraStartupChecklist
 * @see JiraStartupState
 * @since 4.3.1
 */
public class JiraStartupPluginSystemListener implements JiraPluginSystemListener
{
    /**
     * Creates a new JiraStartupPluginSystemListener.
     *
     * @param eventPublisher an EventPublisher
     */
    public JiraStartupPluginSystemListener(EventPublisher eventPublisher)
    {
        eventPublisher.register(this);
    }

    /**
     * Dispatches the "Plugin System Loaded" event to the current JIRA startup state.
     *
     * @param event a PluginFrameworkStartedEvent
     */
    @EventListener
    @SuppressWarnings ("unused")
    public void onPluginSystemStarted(PluginFrameworkStartedEvent event)
    {
        JiraStartupChecklist.getInstance().startupState().onPluginSystemStarted();
    }

    /**
     * Dispatches the "Plugin System Shutdown" event to the current JIRA startup state.
     *
     * @param event a PluginFrameworkShutdownEvent
     */
    @EventListener
    @SuppressWarnings ("unused")
    public void onPluginSystemShutdown(PluginFrameworkShutdownEvent event)
    {
        JiraStartupChecklist.getInstance().startupState().onPluginSystemStopped();
    }

    /**
     * Dispatches the "Plugin System Warn Restarting" event to the current JIRA startup state.
     *
     * @param event a PluginFrameworkWarmRestartedEvent
     */
    @EventListener
    @SuppressWarnings ("unused")
    public void onPluginSystemRestarted(PluginFrameworkWarmRestartedEvent event)
    {
        JiraStartupChecklist.getInstance().startupState().onPluginSystemRestarted();
    }

    @EventListener
    @SuppressWarnings ("unused")
    public void onPluginSystemDelayed(PluginFrameworkDelayedEvent event)
    {
        JiraStartupChecklist.getInstance().startupState().onPluginSystemDelayed();
    }
}
