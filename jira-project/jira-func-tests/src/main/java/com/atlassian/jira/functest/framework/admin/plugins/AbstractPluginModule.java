package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Base class for reference plugin modules.
 *
 * @since v4.4
 */
public abstract class AbstractPluginModule
{
    protected final String pluginKey;
    protected final Administration administration;

    protected AbstractPluginModule(String pluginKey, Administration administration)
    {
        this.pluginKey = notNull("pluginKey", pluginKey);
        this.administration = administration;
    }

    /**
     * Module key.
     *
     * @return module key
     */
    public abstract String moduleKey();

    /**
     * Name of the module
     *
     * @return name of the module
     */
    public abstract String moduleName();

    /**
     * Complete module key including the plugin key prefix.
     *
     * @return complete key of this plugin module
     */
    public final String completeModuleKey()
    {
        return pluginKey + ":" + moduleKey();
    }

    public final void disable()
    {
        administration.plugins().disablePluginModule(pluginKey, completeModuleKey());
    }

    public final void enable()
    {
        administration.plugins().enablePluginModule(pluginKey, completeModuleKey());
    }

    public final boolean isEnabled()
    {
        return administration.plugins().isPluginModuleEnabled(pluginKey, completeModuleKey());
    }

    public final boolean isInstalled()
    {
        return administration.plugins().isPluginInstalled(pluginKey);
    }
}
