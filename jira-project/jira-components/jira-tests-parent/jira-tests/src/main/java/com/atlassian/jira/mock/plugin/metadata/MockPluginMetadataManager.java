package com.atlassian.jira.mock.plugin.metadata;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @since v6.2.3
 */
public class MockPluginMetadataManager implements PluginMetadataManager
{
    private final Set<Plugin> systemPlugins = Sets.newHashSet();

    @Override
    public boolean isUserInstalled(final Plugin plugin)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isSystemProvided(final Plugin plugin)
    {
        return systemPlugins.contains(plugin);
    }

    @Override
    public boolean isOptional(final Plugin plugin)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isOptional(final ModuleDescriptor<?> moduleDescriptor)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MockPluginMetadataManager addSystemPlugin(final Plugin plugin)
    {
        systemPlugins.add(plugin);
        return this;
    }
}
