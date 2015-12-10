package com.atlassian.jira.mock.plugin;

import com.atlassian.plugin.InstallationMode;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.Resourced;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @since v6.2.3
*/
public class MockPlugin implements Plugin
{
    private int pluginsVersion;
    private String name;
    private String key;
    private boolean enabledByDefault;
    private PluginInformation pluginInformation;
    private boolean enabled;
    private boolean systemPlugin;
    private PluginState pluginState;
    private ClassLoader loader;
    private Map<String, ModuleDescriptor<?>> descriptors = Maps.newLinkedHashMap();
    private MockResources resources = new MockResources();
    private final Map<String, String> resourceContent = Maps.newHashMap();


    public MockPlugin()
    {
    }

    public MockPlugin(String key)
    {
        this.key = key;
    }

    public MockPlugin(String name, String key, PluginInformation pluginInformation, PluginState pluginState)
    {
        this.name = name;
        this.key = key;
        this.pluginInformation = pluginInformation;
        this.pluginState = pluginState;
    }


    public MockPlugin(String name, String key, PluginInformation pluginInformation)
    {
        this.name = name;
        this.key = key;
        this.pluginInformation = pluginInformation;
    }

    @Override
    public int getPluginsVersion()
    {
        return pluginsVersion;
    }

    @Override
    public void setPluginsVersion(final int version)
    {
        this.pluginsVersion = version;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public String getI18nNameKey()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setI18nNameKey(final String i18nNameKey)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public void setKey(final String key)
    {
        this.key = key;
    }

    @Override
    public void addModuleDescriptor(final ModuleDescriptor<?> moduleDescriptor)
    {
        descriptors.put(moduleDescriptor.getKey(), moduleDescriptor);
    }

    @Override
    public Collection<ModuleDescriptor<?>> getModuleDescriptors()
    {
        return ImmutableList.copyOf(descriptors.values());
    }

    @Override
    public ModuleDescriptor<?> getModuleDescriptor(final String key)
    {
        return descriptors.get(key);
    }

    @Override
    public <M> List<ModuleDescriptor<M>> getModuleDescriptorsByModuleClass(final Class<M> moduleClass)
    {
        List<ModuleDescriptor<M>> result = Lists.newArrayList();
        for (ModuleDescriptor<?> descriptor : descriptors.values())
        {
            if (descriptor.getModuleClass().equals(moduleClass))
            {
                //noinspection unchecked
                result.add((ModuleDescriptor<M>)descriptor);
            }
        }
        return result;
    }

    @Override
    public InstallationMode getInstallationMode()
    {
        return InstallationMode.LOCAL;
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return enabledByDefault;
    }

    @Override
    public void setEnabledByDefault(final boolean enabledByDefault)
    {
        this.enabledByDefault = enabledByDefault;
    }

    @Override
    public PluginInformation getPluginInformation()
    {
        return pluginInformation;
    }

    @Override
    public void setPluginInformation(final PluginInformation pluginInformation)
    {
        this.pluginInformation = pluginInformation;
    }

    @Override
    public void setResources(final Resourced resources)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PluginState getPluginState()
    {
        return pluginState;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public boolean isSystemPlugin()
    {
        return systemPlugin;
    }

    @Override
    public void setSystemPlugin(final boolean system)
    {
        this.systemPlugin = system;
    }

    @Override
    public boolean containsSystemModule()
    {
        return false;
    }

    @Override
    public boolean isBundledPlugin()
    {
        return false;
    }

    @Override
    public Date getDateLoaded()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Date getDateInstalled()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isUninstallable()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isDeleteable()
    {
        return false;
    }

    @Override
    public boolean isDynamicallyLoaded()
    {
        return false;
    }

    @Override
    public <T> Class<T> loadClass(final String clazz, final Class<?> callingClass) throws ClassNotFoundException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return loader;
    }

    public MockPlugin setClassLoader(ClassLoader loader)
    {
        this.loader = loader;
        return this;
    }

    @Override
    public URL getResource(final String path)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public InputStream getResourceAsStream(final String name)
    {
        final String resource = resourceContent.get(name);
        if (resource != null)
        {
            try
            {
                return IOUtils.toInputStream(resource, "UTF-8");
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return null;

    }

    @Override
    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public void close()
    {
    }

    @Override
    public void install()
    {
    }

    @Override
    public void uninstall()
    {
    }

    @Override
    public void enable()
    {
        setEnabled(true);
    }

    @Override
    public void disable()
    {
        setEnabled(false);
    }

    @Override
    public Set<String> getRequiredPlugins()
    {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getActivePermissions()
    {
        return Collections.emptySet();
    }

    @Override
    public boolean hasAllPermissions()
    {
        return false;
    }

    @Override
    public int compareTo(final Plugin o)
    {
        return getKey().compareTo(o.getKey());
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors()
    {
        return resources.getAll();
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors(final String type)
    {
        return resources.getAllByType(type);
    }

    @Override
    public ResourceDescriptor getResourceDescriptor(final String type, final String name)
    {
        return resources.getResourceDescriptor(type, name);
    }

    @Override
    public ResourceLocation getResourceLocation(final String type, final String name)
    {
        return resources.getResourceLocation(type, name);
    }

    @Override
    public String toString()
    {
        return String.format("Plugin(%s)", key);
    }

    public MockPlugin resource(String name, String value)
    {
        resourceContent.put(name, value);
        return this;
    }

    public ResourceDescriptor createI18nResource(String name, String location)
    {
        return resources.createI18nResource(name, location);
    }

    public ResourceDescriptor createHelpResource(final String name, final String location)
    {
        return resources.createHelpResource(name, location);
    }

    /**
     * Add resource descriptor and corresponding contents that will be returned by
     * {@link #getResourceAsStream(String)}.
     *
     * @param resourceDescriptor resource descriptor to add
     * @param contents corresponding contents
     * @return this mock plugin
     */
    public MockPlugin addResourceDescriptor(ResourceDescriptor resourceDescriptor, String contents)
    {
        addResourceDescriptor(resourceDescriptor);
        resource(resourceDescriptor.getLocation(), contents);
        return this;
    }

    public void addResourceDescriptor(final ResourceDescriptor resourceDescriptor)
    {
        resources.add(resourceDescriptor);
    }
}
