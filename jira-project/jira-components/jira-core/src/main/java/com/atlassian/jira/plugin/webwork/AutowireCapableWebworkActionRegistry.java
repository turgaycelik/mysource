package com.atlassian.jira.plugin.webwork;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.ModuleDescriptor;

/**
 * Registry to link all webwork actions defined in plugins2 osgi bundles to their plugin.  This is necessary since when
 * the action gets instantiated in the {@link com.atlassian.jira.config.webwork.JiraActionFactory} it needs to be
 * auto-wired via the plugin.
 *
 * @since v4.0
 */
public interface AutowireCapableWebworkActionRegistry
{
    /**
     * Register the action class (simple class name) to its Plugin.
     *
     * @param action The action class
     * @param moduleDescriptor The plugin module in which the action lives.
     */
    void registerAction(String action, ModuleDescriptor<?> moduleDescriptor);

    /**
     * Unregister all actions for a plugin.  This should be called whenver a webwork module is disabled.
     *
     * @param moduleDescriptor The plugin module which will be unregistered
     */
    void unregisterPluginModule(ModuleDescriptor<?> moduleDescriptor);

    /**
     * Gets the Plugin registered for a particular action.  Will return Null if none is registered.
     *
     * @param action The action class being instantiated
     * @return The Plugin in which the action lives
     */
    AutowireCapablePlugin getPlugin(String action);

    /**
     * Checks if the registry contains an entry for a particular action.
     *
     * @param action The action class to check for
     * @return true if a plugin is registered for this action.
     */
    boolean containsAction(String action);
}
