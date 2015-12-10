package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;

/**
 * Represents a Plugin available in JIRA.
 *
 * @since v4.4
 */
public abstract class Plugin
{
    private final Administration administration;

    public Plugin(final Administration administration)
    {
        this.administration = administration;
    }

    abstract String getKey();

    public final void enable()
    {
        administration.plugins().enablePlugin(getKey());
    }

    public final void disable()
    {
        administration.plugins().disablePlugin(getKey());
    }

    public final boolean isEnabled()
    {
        return administration.plugins().isPluginEnabled(getKey());
    }

    public final boolean isDisabled()
    {
        return administration.plugins().isPluginDisabled(getKey());
    }

    public final void enableModule(final String moduleKey)
    {
        administration.plugins().enablePluginModule(getKey(), moduleKey);
    }

    public final void disableModule(final String moduleKey)
    {
        administration.plugins().disablePluginModule(getKey(), moduleKey);
    }

    public final boolean isInstalled()
    {
        return administration.plugins().isPluginInstalled(getKey());
    }
}
