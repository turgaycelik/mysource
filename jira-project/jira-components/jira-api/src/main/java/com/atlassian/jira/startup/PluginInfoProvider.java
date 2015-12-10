package com.atlassian.jira.startup;

/**
 * This provides information about the plugins in the system. It's intended to be used by the system info and startup
 * pages to allow for better support capabilties.
 *
 * @since v4.3
 */
public interface PluginInfoProvider
{
    /**
     * @return Information about the current system plugins in the system
     */
    PluginInfos getSystemPlugins();

    /**
     * @param includeBuiltInPlugins a boolean indicating whether to include plugins that are so integral to JIRA thats
     * its hardly worth knowing about them
     * @return Information about the current system plugins in the system
     */
    PluginInfos getSystemPlugins(boolean includeBuiltInPlugins);

    /**
     * @return Information about the current user plugins in the system
     */
    PluginInfos getUserPlugins();
}
