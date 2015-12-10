package com.atlassian.jira.plugin.util;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.Resources;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.annotation.concurrent.ThreadSafe;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.2.3
 */
@ThreadSafe
public class SimplePluginsTracker implements PluginsTracker
{
    private final Set<PluginInfo> pluginsInvolved;

    public SimplePluginsTracker()
    {
        this.pluginsInvolved = new CopyOnWriteArraySet<PluginInfo>();
    }

    private PluginInfo toInfo(Plugin plugin)
    {
        return new PluginInfo(plugin.getKey(), plugin.getPluginInformation().getVersion());
    }

    @Override
    public void trackInvolvedPlugin(Plugin plugin)
    {
        pluginsInvolved.add(toInfo(notNull("plugin", plugin)));
    }

    @Override
    public void trackInvolvedPlugin(ModuleDescriptor moduleDescriptor)
    {
        pluginsInvolved.add(toInfo(notNull("moduleDescriptor", moduleDescriptor).getPlugin()));
    }

    @Override
    public boolean isPluginInvolved(Plugin plugin)
    {
        return pluginsInvolved.contains(toInfo(notNull("plugin", plugin)));
    }

    @Override
    public boolean isPluginInvolved(ModuleDescriptor moduleDescriptor)
    {
        return pluginsInvolved.contains(toInfo(notNull("moduleDescriptor", moduleDescriptor).getPlugin()));
    }

    @Override
    public boolean isPluginWithModuleDescriptor(ModuleDescriptor moduleDescriptor, Class<? extends ModuleDescriptor> targetModuleClass)
    {
        return isPluginWithModuleDescriptor(notNull("moduleDescriptor", moduleDescriptor).getPlugin(), targetModuleClass);
    }

    @Override
    public boolean isPluginWithModuleDescriptor(Plugin plugin, Class<? extends ModuleDescriptor> targetModuleClass)
    {
        for (final ModuleDescriptor<?> moduleDescriptor : plugin.getModuleDescriptors())
        {
            final Class<?> moduleClass = moduleDescriptor.getClass();
            if (targetModuleClass.isAssignableFrom(moduleClass))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPluginWithResourceType(Plugin plugin, String pluginResourceType)
    {
        final Resources.TypeFilter filter = new Resources.TypeFilter(pluginResourceType);
        if (Iterables.any(plugin.getResourceDescriptors(), filter))
        {
            return true;
        }
        for (final ModuleDescriptor<?> moduleDescriptor : plugin.getModuleDescriptors())
        {
            if (Iterables.any(moduleDescriptor.getResourceDescriptors(), filter))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPluginWithResourceType(ModuleDescriptor moduleDescriptor, String pluginResourceType)
    {
        return isPluginWithResourceType(notNull("moduleDescriptor", moduleDescriptor).getPlugin(), pluginResourceType);
    }

    @Override
    public Set<PluginInfo> getInvolvedPluginKeys()
    {
        return Sets.newLinkedHashSet(pluginsInvolved);
    }

    @Override
    public void clear()
    {
        pluginsInvolved.clear();
    }

    @Override
    public String getStateHashCode()
    {
        return Integer.toString(pluginsInvolved.hashCode(), Character.MAX_RADIX);
    }

    @Override
    public String toString()
    {
        return super.toString() + " " + pluginsInvolved.toString();
    }
}
