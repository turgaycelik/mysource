package com.atlassian.jira.startup;

import com.atlassian.plugin.PluginInformation;

/**
 * A value object of collected plugin information
 */
public interface PluginInfo
{
    /**
     * @return the plugin key
     */
    String getKey();

    /**
     * @return the plugin name
     */
    String getName();

    /**
     * @return the plugin information
     */
    PluginInformation getPluginInformation();

    /**
     * @return true if the plugin is unloadable
     */
    boolean isUnloadable();

    /**
     * @return the reason for unloadability
     */
    String getUnloadableReason();

    /**
     * @return true if the plugin is currently enabled
     */
    boolean isEnabled();

    /**
     * @return true if the plugin is a system plugin
     */
    boolean isSystemPlugin();

    /**
     * @return the platform the plugin targets, eg plugins 1 or plugins 2
     */
    int getPluginsVersion();
}
