package com.atlassian.jira.plugin;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.classloader.PluginsClassLoader;
import org.apache.log4j.Logger;

public class DefaultComponentClassManager implements ComponentClassManager
{
    private static final Logger log = Logger.getLogger(DefaultComponentClassManager.class);

    private final PluginAccessor pluginAccessor;
    private final PluginsClassLoader pluginsClassLoader;

    public DefaultComponentClassManager(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
        // TODO: shouldn't have to cast this.
        this.pluginsClassLoader = (PluginsClassLoader) pluginAccessor.getClassLoader();
    }

    @SuppressWarnings("unchecked")
    public <T> T newInstance(final String className) throws ClassNotFoundException
    {
        Class<T> clazz;
        try
        {
            // First we try to load the class using the standard (non-OSGi) class loaders
            clazz = ClassLoaderUtils.loadClass(className, this.getClass());
            if (log.isDebugEnabled())
            {
                log.debug("Class '" + className + "' loaded with the standard ClassLoader " + this.getClass().getClassLoader());
            }
            // Success - inject using Pico
            return JiraUtils.loadComponent(clazz);
        }
        catch (ClassNotFoundException ex)
        {
            // Not found on classpath - try the OSGi plugins.
            try
            {
                // First we try to load the class using the plugin framework's PluginsClassLoader
                // (this includes caching and is therfore the preferred method)
                clazz = (Class<T>) pluginsClassLoader.loadClass(className);
                // Success - Find which plugin it belongs to
                Plugin plugin = getPluginForClass(className);
                try
                {
                    return newInstanceFromPlugin(clazz, plugin);
                }
                catch (RuntimeException ex2)
                {
                    // JRA-23086 : Plugin may be able to load class but not wire it up. Time for Brute force.
                    // At time of writing this would throw an UnsupportedOperationException, but we will catch RuntimeException in case this changes.
                    log.debug("Unable to dependency inject '" + className+ "' in plugin '" + plugin.getKey() + "'. Attempting to wire it via brute force...");
                    return (T) bruteForceWireInstance(className);
                }
            }
            catch (ClassNotFoundException ex2)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Class '" + className + "' could not be loaded with the PluginsClassLoader - maybe it belongs to a plugin that is not enabled yet.");
                }
                // The PluginsClassLoader failed, but it may just be that the plugin is in the middle of enabling.
                // Try harder!
                return this.<T>constructEvenIfNotEnabled(className);
            }
        }
    }

    private Object bruteForceWireInstance(String className)
    {
        for (final Plugin plugin : pluginAccessor.getEnabledPlugins())
        {
            try
            {
                @SuppressWarnings("unchecked")
                final Class clazz = plugin.getClassLoader().loadClass(className);
                if (log.isDebugEnabled())
                {
                    log.debug("Class '" + className + "' loaded from plugin " + plugin.getKey() + " - attempting to instantiate with dependencies...");
                }
                Object o = newInstanceFromPlugin(clazz, plugin);
                if (log.isDebugEnabled())
                {
                    log.debug("Wired Class '" + className + "' successfully from plugin " + plugin.getKey() + " .");
                }
                return o;
            }
            catch (Exception autowireException)
            {
                // continue searching the other plugins
            }
        }
        throw new RuntimeException("Class '" + className + "' is loadable from OSGi but no enabled plugins could autowire an instance.");
    }

    public <T> T newInstanceFromPlugin(final Class<T> clazz, final Plugin plugin)
    {
        final String className = clazz.getName();
        if (plugin instanceof AutowireCapablePlugin)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Class '" + className + "' loaded with the PluginsClassLoader - attempting autowire with plugin " + plugin.getKey());
            }
            // We expect this for an OSGiPlugin
            return ((AutowireCapablePlugin) plugin).autowire(clazz);
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Loaded the class '" + className + "' via PluginsClassLoader but plugin '" + plugin.getKey() + "' is not an AutowireCapablePlugin.");
            }
            // try and load via Pico
            return JiraUtils.loadComponent(clazz);
        }
    }

    private <T> T constructEvenIfNotEnabled(final String className) throws ClassNotFoundException
    {
        // Here we don't limit to only enabled plugins like in PluginsClassLoader
        for (final Plugin plugin : pluginAccessor.getPlugins())
        {
            try
            {
                @SuppressWarnings("unchecked")
                final Class<T> clazz = (Class<T>) plugin.getClassLoader().loadClass(className);
                // Cool - we got the class from a plugin that is not enabled.
                // However, we cannot autowire an OSGi plugin if the plugin is not enabled.
                if (log.isDebugEnabled())
                {
                    log.debug("Class '" + className + "' loaded from plugin " + plugin.getKey() + " but this plugin was not enabled, trying to load via Pico.");
                }
                return JiraUtils.loadComponent(clazz);
            }
            catch (final ClassNotFoundException ex)
            {
                // continue searching the other plugins
            }
            catch (final RuntimeException re)
            {
                // Probably a ClassCastException from the wrong plugin or an IllegalStateException
                // from a plugin that's in the middle of disabling
                // continue searching the other plugins
            }
            catch (final LinkageError le)
            {
                // Binary incompatibility or runtime exception in a static initializer
                // continue searching the other plugins
            }
        }
        throw new ClassNotFoundException("Class '" + className + "' not found.");
    }

    public <T> Class<T> loadClass(String className) throws ClassNotFoundException
    {
        try
        {
            // First we try to load the class using the plugin framework's PluginsClassLoader
            // (this includes caching and is therfore the preferred method)
            //noinspection unchecked
            return ClassLoaderUtils.loadClass(className, pluginsClassLoader);
        }
        catch (ClassNotFoundException e)
        {
            // The PluginsClassLoader failed, but it may just be that the plugin is in the middle of enabling.
            // Try harder!
            return this.loadClassEvenIfNotEnabled(className);
        }
    }

    private <T> Class<T> loadClassEvenIfNotEnabled(final String className) throws ClassNotFoundException
    {
        // Here we don't limit to only enabled plugins like in PluginsClassLoader
        for (final Plugin plugin : pluginAccessor.getPlugins())
        {
            try
            {
                //noinspection unchecked
                return (Class<T>) plugin.getClassLoader().loadClass(className);
            }
            catch (final ClassNotFoundException ex)
            {
                // continue searching the other plugins
            }
            catch (final RuntimeException re)
            {
                // Probably a ClassCastException from the wrong plugin or an IllegalStateException
                // from a plugin that's in the middle of disabling
                // continue searching the other plugins
            }
            catch (final LinkageError le)
            {
                // Binary incompatibility or runtime exception in a static initializer
                // continue searching the other plugins
            }
        }
        throw new ClassNotFoundException("Class '" + className + "' not found.");
    }

    private Plugin getPluginForClass(final String className)
    {
        return pluginsClassLoader.getPluginForClass(className);
    }

}
