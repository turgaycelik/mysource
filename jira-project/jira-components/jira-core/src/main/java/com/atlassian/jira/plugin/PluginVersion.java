package com.atlassian.jira.plugin;

import java.util.Date;

/**
 * Domain object for holding plugin version information.
 *
 * @since v3.13
 */
public interface PluginVersion
{
    /**
     * @return the id of the PluginVersion record.
     */
    Long getId();

    /**
     * @return the string representing the plugins key. This is the key that you can access the plugin with
     * via a call to {@link com.atlassian.plugin.PluginAccessor#getPlugin(String)}.
     */
    String getKey();

    /**
     * @return the descriptive name of the plugin.
     */
    String getName();

    /**
     * @return a string representing the version of the plugin.
     */
    String getVersion();

    /**
     * @return the date this plugin version was added/updated in the database.
     */
    Date getCreated();
}
