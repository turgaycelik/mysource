package com.atlassian.jira.mock.plugin;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugin.predicate.ModuleDescriptorPredicate;
import com.atlassian.plugin.predicate.PluginPredicate;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @since v6.0
 */
public class MockPluginAccessor implements PluginAccessor
{
    private Map<String, Plugin> plugins = Maps.newLinkedHashMap();

    @Override
    public Collection<Plugin> getPlugins()
    {
        return plugins.values();
    }

    public MockPluginAccessor addPlugins(Plugin...plugins)
    {
        for (Plugin plugin : plugins)
        {
            addPlugin(plugin);
        }
        return this;
    }

    public MockPluginAccessor addPlugin(Plugin plugin)
    {
        plugins.put(plugin.getKey(), plugin);
        return this;
    }

    @Override
    public Collection<Plugin> getPlugins(final PluginPredicate pluginPredicate)
    {
        return ImmutableList.copyOf(Iterables.filter(plugins.values(), new Predicate<Plugin>()
        {
            @Override
            public boolean apply(@Nullable final Plugin input)
            {
                return pluginPredicate.matches(input);
            }
        }));
    }

    @Override
    public Collection<Plugin> getEnabledPlugins()
    {
        return getPlugins(new PluginPredicate()
        {
            @Override
            public boolean matches(final Plugin plugin)
            {
                return MockPluginAccessor.this.isPluginEnabled(plugin.getKey());
            }
        });
    }

    @Override
    public <M> Collection<M> getModules(final ModuleDescriptorPredicate<M> moduleDescriptorPredicate)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <M> Collection<ModuleDescriptor<M>> getModuleDescriptors(final ModuleDescriptorPredicate<M> moduleDescriptorPredicate)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Plugin getPlugin(final String key) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Plugin getEnabledPlugin(final String pluginKey) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ModuleDescriptor<?> getPluginModule(final String completeKey)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ModuleDescriptor<?> getEnabledPluginModule(final String completeKey)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isPluginEnabled(final String key) throws IllegalArgumentException
    {
        return plugins.containsKey(key);
    }

    @Override
    public boolean isPluginModuleEnabled(final String completeKey)
    {
        final String[] split = completeKey.split(":");
        return isPluginEnabled(split[0]);
    }

    @Override
    public <M> List<M> getEnabledModulesByClass(final Class<M> moduleClass)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <M> List<M> getEnabledModulesByClassAndDescriptor(final Class<ModuleDescriptor<M>>[] descriptorClazz, final Class<M> moduleClass)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <M> List<M> getEnabledModulesByClassAndDescriptor(final Class<ModuleDescriptor<M>> moduleDescriptorClass, final Class<M> moduleClass)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <D extends ModuleDescriptor<?>> List<D> getEnabledModuleDescriptorsByClass(final Class<D> descriptorClazz)
    {
        List<D> data = Lists.newArrayList();
        for (Plugin plugin : getEnabledPlugins())
        {
            for (final ModuleDescriptor<?> module : plugin.getModuleDescriptors())
            {
                if (descriptorClazz.isInstance(module))
                {
                    if (isPluginModuleEnabled(module.getCompleteKey()))
                    {
                        data.add(descriptorClazz.cast(module));
                    }
                }
            }
        }
        return data;
    }

    @Override
    public <D extends ModuleDescriptor<?>> List<D> getEnabledModuleDescriptorsByClass(final Class<D> descriptorClazz, final boolean verbose)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <M> List<ModuleDescriptor<M>> getEnabledModuleDescriptorsByType(final String type)
            throws PluginParseException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public InputStream getDynamicResourceAsStream(final String resourcePath)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public InputStream getPluginResourceAsStream(final String pluginKey, final String resourcePath)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Class<?> getDynamicPluginClass(final String className) throws ClassNotFoundException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ClassLoader getClassLoader()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isSystemPlugin(final String key)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PluginRestartState getPluginRestartState(final String key)
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
