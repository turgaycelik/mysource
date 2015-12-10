package com.atlassian.jira.mail;

import com.atlassian.plugin.PluginAccessor;

/**
 *
 */
public class JiraMailPluginsHelperImpl implements JiraMailPluginsHelper
{
    private final PluginAccessor pluginAccessor;

    public JiraMailPluginsHelperImpl(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public boolean isPluginModuleEnabled(String pluginModuleKey)
    {
        return pluginAccessor.isPluginModuleEnabled(pluginModuleKey);
    }
}
