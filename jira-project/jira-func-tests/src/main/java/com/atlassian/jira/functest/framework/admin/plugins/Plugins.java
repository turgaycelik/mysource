package com.atlassian.jira.functest.framework.admin.plugins;

/**
 * This is a utility class that uses UPM's restful interface
 * to find out info about installed plugins and enable/disable them
 *
 * All of these functions will try to return you to the page you were on before calling them.
 * @since v4.3
 */
public interface Plugins
{
    /**
     * Checks if the plugin is found in the UPM.
     * @param pluginKey - the key for the plugin eg. com.atlassian.examplePlugin
     * @return if the plugin is installed on the instance.
     */
    public boolean isPluginInstalled(String pluginKey);

    /**
     * Is it possible to disable a module of a plugin.
     * @param pluginKey  - the key for the plugin eg. com.atlassian.examplePlugin
     * @param completeModuleKey - the key for the module eg. com.atlassian.examplePlugin:exampleModule
     * @return if the module is able to be disabled (ie. optional)
     */
    public boolean canDisablePluginModule(final String pluginKey, final String completeModuleKey);

    /**
     * Is it possible to disable a plugin.
     * @param pluginKey  - the key for the plugin eg. com.atlassian.examplePlugin
     * @return if the plugin is able to be disabled (ie. optional)
     */
    public boolean canDisablePlugin(final String pluginKey);

    /**
     * Tries to enable a plugin (so long as it is installed).
     *
     * @param pluginKey - the key for the plugin eg. com.atlassian.examplePlugin
     */
    public void enablePlugin(final String pluginKey);

    /**
     * Tries to disable a plugin (so long as it is installed).
     *
     * @param pluginKey - the key for the plugin eg. com.atlassian.examplePlugin
     */
    public void disablePlugin(final String pluginKey);

    /**
     * Tries to enable a module within a plugin.
     *
     * @param pluginKey  - the key for the plugin eg. com.atlassian.examplePlugin
     * @param completeModuleKey - the key for the module eg. com.atlassian.examplePlugin:exampleModule
     */
    public void enablePluginModule(final String pluginKey, final String completeModuleKey);

    /**
     * Tries to disable a module within a plugin.
     *
     * @param pluginKey  - the key for the plugin eg. com.atlassian.examplePlugin
     * @param completeModuleKey - the key for the module eg. com.atlassian.examplePlugin:exampleModule
     */
    public void disablePluginModule(final String pluginKey, final String completeModuleKey);

     /**
     * Is a plugin enabled.
     * @param pluginKey  - the key of the plugin to check eg. com.atlassian.examplePlugin
     * @return true if the plugin is enabled; otherwise, false.
     */
    public boolean isPluginEnabled(final String pluginKey);

    /**
     * Is a plugin disabled.
     * @param pluginKey  - the key of the plugin to check eg. com.atlassian.examplePlugin
     * @return true if the plugin is disabled; otherwise, false.
     */
    boolean isPluginDisabled(String pluginKey);

    /**
     * Is a module of a plugin enabled.
     * @param pluginKey  - the key for the plugin eg. com.atlassian.examplePlugin
     * @param completeModuleKey - the key for the module eg. com.atlassian.examplePlugin:exampleModule
     * @return true if the module is enabled; otherwise, false.
     */
    public boolean isPluginModuleEnabled(final String pluginKey, final String completeModuleKey);

    /**
     * Is a module of a plugin disabled.
     * @param pluginKey  - the key of the plugin eg. com.atlassian.examplePlugin
     * @param completeModuleKey - the key of the module to check eg. com.atlassian.examplePlugin:exampleModule
     * @return true if the module is disabled; otherwise, false.
     */
    public boolean isPluginModuleDisabled(final String pluginKey, final String completeModuleKey);

    /**
     * Gets an instance of the reference plugin.
     * @return an instance of the reference plugin.
     */
    ReferencePlugin referencePlugin();

    /**
     * Gets an instance of the reference dependent plugin.
     * @return an instance of the reference dependent plugin.
     */
    ReferenceDependentPlugin referenceDependentPlugin();

    /**
     * Gets an instance of the reference language pack plugin.
     * @return an instance of the reference language pack plugin.
     */
    ReferenceLanguagePack referenceLanguagePack();
}
