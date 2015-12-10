package com.atlassian.jira.plugin;

import com.atlassian.plugin.loaders.PluginLoader;

import java.util.List;

public interface PluginLoaderFactory
{
    /**
     * Return a list of plugin loaders that implement {@link PluginLoader}.
     */
    List<PluginLoader> getPluginLoaders();
}
