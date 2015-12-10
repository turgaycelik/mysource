package com.atlassian.jira.plugin;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;

/**
 * A helper class for doing Dependency Injection on classes living in plugins.
 *
 * @since v4.0
 */
public class PluginInjector
{
    /**
     * Returns a new instance of the given class with dependencies injected in a way appropriate to the plugin it comes from.
     *
     * <p> If this class comes from an OSGi plugin, it will be injected by Spring, else it will be injected by Pico.
     *
     * @param clazz The class to construct.
     * @param plugin The Plugin that the class comes from.
     * @param <T> The type of the object to be constructed.
     * @return a new instance of the given class with dependencies injected in a way appropriate to the plugin it comes from.
     */
    public static <T> T newInstance(final Class<T> clazz, final Plugin plugin)
    {
        // Eventually delegates to the {@link com.atlassian.plugin.module.ContainerAccessor#createBean}, but does more
        // checks before then.
        if (plugin instanceof AutowireCapablePlugin)
        {
            return ((AutowireCapablePlugin) plugin).autowire(clazz);
        }
        else
        {
            return JiraUtils.loadComponent(clazz);
        }
    }
}
