package com.atlassian.jira.plugin;

import com.atlassian.plugin.Plugin;

/**
 * Provides methods to help load and instantiate classes that can handle OSGi plugins with their ClassLoaders and
 * Components that don't live in Pico Container.
 *
 * @since v4.0
 */
public interface ComponentClassManager
{
    /**
     * Instantiates the named class using OSGi ClassLoaders and Pico/Spring injection as appropriate.
     *
     * @param className the name of the class to instantiate.
     * @return the instance of the requested class.
     * @throws ClassNotFoundException if the given className was not able to be loaded.
     */
    public <T> T newInstance(String className) throws ClassNotFoundException;

    /**
     * Instantiates the named class by directly using the plugin the class was loaded from and its autowiring strategy.
     *
     * @param clazz the class to instantiate
     * @param plugin the plugin in which the class was loaded from
     * @return the instance of the requested class.
     * @see #newInstance(String) 
     */
    public <T> T newInstanceFromPlugin(Class<T> clazz, Plugin plugin);

    /**
     * Load a class from Jira core, or a plugin including plugins that are not enabled.
     *
     * @param className Fully qualified class name.
     * @param <T> The Class type.
     * @return the loaded Class of type T.
     * @throws ClassNotFoundException if the given className was not able to be loaded.
     */
    public <T> Class<T> loadClass(String className) throws ClassNotFoundException;
}
