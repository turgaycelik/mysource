package com.atlassian.jira.web.bean;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.atlassian.jira.plugin.language.Language;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptorImpl;
import com.atlassian.plugin.InstallationMode;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Permissions;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.Resourced;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.predicate.ModuleDescriptorPredicate;
import com.atlassian.plugin.predicate.PluginPredicate;

import com.google.common.collect.ImmutableSet;

/**
 * Mock plugin accessor that return module descriptor of LanguageModuleDescriptor class
 *
 * @since v4.3
 */
public class MockV2LanguagePackPluginAccessor implements PluginAccessor
{
    private DescriptorStrategy strategy;

    interface DescriptorStrategy
    {
        <D extends ModuleDescriptor<?>> List<D> get(Class<D> dClass);
    }

    static class DefaultStrategy implements DescriptorStrategy
    {
        private final Locale locale;
        private final String resourceBundleName;
        private final ClassLoader classLoader;

        public DefaultStrategy(Locale locale, String resourceBundleName, ClassLoader classLoader)
        {
            this.locale = locale;
            this.resourceBundleName = resourceBundleName;
            this.classLoader = classLoader;
        }

        @Override
        public <D extends ModuleDescriptor<?>> List<D> get(Class<D> dClass)
        {
            List<D> result = new ArrayList<D>();
            if (dClass.equals(LanguageModuleDescriptor.class))
            {
                LanguageModuleDescriptor descriptor = new TestingLanguageModuleDescriptor(locale, resourceBundleName, classLoader);
                result.add((D) descriptor);
            }
            return result;
        }
    }

    static class TestingLanguageModuleDescriptor extends LanguageModuleDescriptorImpl
    {
        private final Locale locale;
        private final String resourceBundleName;
        private final ClassLoader classLoader;

        public TestingLanguageModuleDescriptor(Locale locale, String resourceBundleName, ClassLoader classLoader)
        {
            super(null, ModuleFactory.LEGACY_MODULE_FACTORY);
            this.locale = locale;
            this.resourceBundleName = resourceBundleName;
            this.classLoader = classLoader;
        }

        @Override
        public Language getModule()
        {
            return new Language(locale);
        }

        @Override
        public String getResourceBundleName()
        {
            return resourceBundleName;
        }

        @Override
        public Plugin getPlugin()
        {
            return new Plugin()
            {
                private PluginInformation pluginInformation = new PluginInformation();

                @Override
                public int compareTo(Plugin o)
                {
                    return 0;
                }

                @Override
                public int getPluginsVersion()
                {
                    return 0;
                }

                @Override
                public void setPluginsVersion(int i)
                {
                }

                @Override
                public String getName()
                {
                    return "pluginName";
                }

                @Override
                public void setName(String s)
                {
                }

                @Override
                public String getI18nNameKey()
                {
                    return null;
                }

                @Override
                public void setI18nNameKey(String s)
                {
                }

                @Override
                public String getKey()
                {
                    return "pluginKey";
                }

                @Override
                public void setKey(String s)
                {
                }

                @Override
                public void addModuleDescriptor(ModuleDescriptor<?> moduleDescriptor)
                {
                }

                @Override
                public Collection<ModuleDescriptor<?>> getModuleDescriptors()
                {
                    return null;
                }

                @Override
                public ModuleDescriptor<?> getModuleDescriptor(String s)
                {
                    return null;
                }

                @Override
                public <M> List<ModuleDescriptor<M>> getModuleDescriptorsByModuleClass(Class<M> mClass)
                {
                    return null;
                }

                @Override
                public boolean isEnabledByDefault()
                {
                    return false;
                }

                @Override
                public void setEnabledByDefault(boolean b)
                {
                }

                @Override
                public PluginInformation getPluginInformation()
                {
                    return pluginInformation;
                }

                @Override
                public void setPluginInformation(PluginInformation pluginInformation)
                {
                    this.pluginInformation = pluginInformation;
                }

                @Override
                public void setResources(Resourced resourced)
                {
                }

                @Override
                public PluginState getPluginState()
                {
                    return null;
                }

                @Override
                public boolean isEnabled()
                {
                    return false;
                }

                @Override
                public boolean isSystemPlugin()
                {
                    return false;
                }

                @Override
                public void setSystemPlugin(boolean b)
                {
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
                    return null;
                }

                @Override
                public Date getDateInstalled()
                {
                    return null;
                }

                @Override
                public boolean isUninstallable()
                {
                    return false;
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
                public <T> Class<T> loadClass(String s, Class<?> aClass) throws ClassNotFoundException
                {
                    return null;
                }

                @Override
                public ClassLoader getClassLoader()
                {
                    return classLoader;
                }

                @Override
                public URL getResource(String s)
                {
                    return null;
                }

                @Override
                public InputStream getResourceAsStream(String s)
                {
                    return null;
                }

                @Override
                public void setEnabled(boolean b)
                {
                }

                @Override
                public void close()
                {
                }

                @Override
                public void install() throws PluginException
                {
                }

                @Override
                public void uninstall() throws PluginException
                {
                }

                @Override
                public void enable() throws PluginException
                {
                }

                @Override
                public void disable() throws PluginException
                {
                }

                @Override
                public Set<String> getRequiredPlugins()
                {
                    return null;
                }

                @Override
                public List<ResourceDescriptor> getResourceDescriptors()
                {
                    return null;
                }

                @Override
                public List<ResourceDescriptor> getResourceDescriptors(String s)
                {
                    return null;
                }

                @Override
                public ResourceDescriptor getResourceDescriptor(String s, String s1)
                {
                    return null;
                }

                @Override
                public ResourceLocation getResourceLocation(String s, String s1)
                {
                    return null;
                }

                @Override
                public InstallationMode getInstallationMode()
                {
                    return InstallationMode.LOCAL;
                }

                @Override
                public Set<String> getActivePermissions()
                {
                    return ImmutableSet.of(Permissions.ALL_PERMISSIONS);
                }

                @Override
                public boolean hasAllPermissions()
                {
                    return true;
                }
            };
        }
    }

    public MockV2LanguagePackPluginAccessor(Locale locale, String resourceBundleName, ClassLoader classLoader)
    {
        this.strategy = new DefaultStrategy(locale, resourceBundleName, classLoader);
    }

    // Use this constructor if you plan on having your own DescriptorStrategy.
    public MockV2LanguagePackPluginAccessor()
    {}

    public MockV2LanguagePackPluginAccessor setDescriptorStrategy(final DescriptorStrategy strategy)
    {
        this.strategy = strategy;
        return this;
    }

    @Override
    public Collection<Plugin> getPlugins()
    {
        return null;
    }

    @Override
    public Collection<Plugin> getPlugins(PluginPredicate pluginPredicate)
    {
        return null;
    }

    @Override
    public Collection<Plugin> getEnabledPlugins()
    {
        return Collections.emptyList();
    }

    @Override
    public <M> Collection<M> getModules(ModuleDescriptorPredicate<M> mModuleDescriptorPredicate)
    {
        return null;
    }

    @Override
    public <M> Collection<ModuleDescriptor<M>> getModuleDescriptors(ModuleDescriptorPredicate<M> mModuleDescriptorPredicate)
    {
        return null;
    }

    @Override
    public Plugin getPlugin(String s) throws IllegalArgumentException
    {
        return null;
    }

    @Override
    public Plugin getEnabledPlugin(String s) throws IllegalArgumentException
    {
        return null;
    }

    @Override
    public ModuleDescriptor<?> getPluginModule(String s)
    {
        return null;
    }

    @Override
    public ModuleDescriptor<?> getEnabledPluginModule(String s)
    {
        return null;
    }

    @Override
    public boolean isPluginEnabled(String s) throws IllegalArgumentException
    {
        return false;
    }

    @Override
    public boolean isPluginModuleEnabled(String s)
    {
        return false;
    }

    @Override
    public <M> List<M> getEnabledModulesByClass(Class<M> mClass)
    {
        return null;
    }

    @Override
    public <M> List<M> getEnabledModulesByClassAndDescriptor(Class<ModuleDescriptor<M>>[] classes, Class<M> mClass)
    {
        return null;
    }

    @Override
    public <M> List<M> getEnabledModulesByClassAndDescriptor(Class<ModuleDescriptor<M>> moduleDescriptorClass, Class<M> mClass)
    {
        return null;
    }

    @Override
    public <D extends ModuleDescriptor<?>> List<D> getEnabledModuleDescriptorsByClass(Class<D> dClass)
    {
        return strategy.get(dClass);
    }

    @Override
    public <D extends ModuleDescriptor<?>> List<D> getEnabledModuleDescriptorsByClass(Class<D> dClass, boolean b)
    {
        return null;
    }

    @Override
    public <M> List<ModuleDescriptor<M>> getEnabledModuleDescriptorsByType(String s) throws PluginParseException
    {
        return null;
    }

    @Override
    public InputStream getDynamicResourceAsStream(String s)
    {
        return null;
    }

    @Override
    public InputStream getPluginResourceAsStream(String s, String s1)
    {
        return null;
    }

    @Override
    public Class<?> getDynamicPluginClass(String s) throws ClassNotFoundException
    {
        return null;
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return null;
    }

    @Override
    public boolean isSystemPlugin(String s)
    {
        return false;
    }

    @Override
    public PluginRestartState getPluginRestartState(String s)
    {
        return null;
    }
}
