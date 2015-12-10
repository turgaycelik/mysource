package com.atlassian.jira.mail;

/**
 * This is a facade between the mail subsystem and the plugins manager
 */
public interface JiraMailPluginsHelper
{
    /**
     * @param pluginModuleKey
     * @return true if the specified plugin key is enabled, false otherwise
     */
    public abstract boolean isPluginModuleEnabled(String pluginModuleKey);
}
