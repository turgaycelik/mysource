package com.atlassian.jira.plugin;

/**
 * Marker interface for listeners that expect to receive plugin system-related events (e.g. {@link
 * com.atlassian.plugin.event.events.PluginFrameworkStartedEvent}). This interface is used to enforce a happens-before
 * relationship between construction of the listeners and constructions of JIRA's plugin manager. If the listener does
 * not implement this interface, there is currently no way in JIRA to inform PICO that the listeners need to be
 * instantiated before the plugin manager does its thing.
 * <p/>
 * Not implementing this interface may mean that your listener will miss events.
 *
 * @since 4.3.1
 */
public interface JiraPluginSystemListener
{
    // marker interface for plugin system event listeners
}
