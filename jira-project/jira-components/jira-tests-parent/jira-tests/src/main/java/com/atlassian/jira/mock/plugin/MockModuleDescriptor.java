package com.atlassian.jira.mock.plugin;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;
import org.dom4j.Element;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * @since v6.2.3
 */
public abstract class MockModuleDescriptor<T> implements ModuleDescriptor<T>
{
    private final Class<T> type;
    private Plugin plugin;
    private String key;
    private MockResources resources = new MockResources();

    public MockModuleDescriptor(final Class<T> type, final Plugin plugin, final String key)
    {
        this.type = type;
        this.plugin = plugin;
        this.key = key;
    }

    public MockModuleDescriptor(Class<T> type)
    {
        this(type, null, null);
    }

    public void setKey(final String key)
    {
        this.key = key;
    }

    public void setPlugin(final Plugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public String getCompleteKey()
    {
        return String.format("%s:%s", plugin.getKey(), key);
    }

    @Override
    public String getPluginKey()
    {
        return plugin.getKey();
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public String getName()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getDescription()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Class<T> getModuleClass()
    {
        return type;
    }

    @Override
    public void init(@Nonnull final Plugin plugin, @Nonnull final Element element)
    {
    }

    @Override
    public boolean isEnabledByDefault()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isSystemModule()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void destroy(final Plugin plugin)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void destroy()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Float getMinJavaVersion()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean satisfiesMinJavaVersion()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<String, String> getParams()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getI18nNameKey()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getDescriptionKey()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Plugin getPlugin()
    {
        return plugin;
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
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResourceLocation getResourceLocation(final String type, final String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public ResourceDescriptor createI18nResource(final String name, final String location)
    {
        return resources.createI18nResource(name, location);
    }

    public ResourceDescriptor createHelpResource(final String name, final String location)
    {
        return resources.createHelpResource(name, location);
    }
}
