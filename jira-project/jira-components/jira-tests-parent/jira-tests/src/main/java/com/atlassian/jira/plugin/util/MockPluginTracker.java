package com.atlassian.jira.plugin.util;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

/**
 * @since v6.2.3
 */
public class MockPluginTracker implements PluginsTracker
{
    private List<Plugin> trackedPlugins = Lists.newArrayList();

    @Override
    public void trackInvolvedPlugin(final Plugin plugin)
    {
        trackedPlugins.add(plugin);
    }

    @Override
    public void trackInvolvedPlugin(final ModuleDescriptor moduleDescriptor)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isPluginInvolved(final Plugin plugin)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isPluginInvolved(final ModuleDescriptor moduleDescriptor)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isPluginWithModuleDescriptor(final ModuleDescriptor moduleDescriptor, final Class<? extends ModuleDescriptor> targetModuleClass)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isPluginWithModuleDescriptor(final Plugin plugin, final Class<? extends ModuleDescriptor> targetModuleClass)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isPluginWithResourceType(final Plugin plugin, final String pluginResourceType)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isPluginWithResourceType(final ModuleDescriptor moduleDescriptor, final String pluginResourceType)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Set<PluginInfo> getInvolvedPluginKeys()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void clear()
    {
        trackedPlugins.clear();
    }

    @Override
    public String getStateHashCode()
    {
        return "47";
    }

    public List<Plugin> getTrackedPlugins()
    {
        return trackedPlugins;
    }
}
