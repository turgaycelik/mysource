package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;

/**
 * JRA-24218: Ensure UPM is enabled.
 *
 * @since v5.0.5
 */
public class UpgradeTask_Build758 extends AbstractUpgradeTask
{
    public static final String UPM_KEY = "com.atlassian.upm.atlassian-universal-plugin-manager-plugin";
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;

    public UpgradeTask_Build758(PluginController pluginController, PluginAccessor pluginAccessor)
    {
        super(false);
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
    }
    @Override
    public String getBuildNumber()
    {
        return "758";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        // UPM can be enabled by setting the property string with key
        // "jira.plugin.state-.com.atlassian.upm.atlassian-universal-plugin-manager-plugin" to "true".
        // However, by the time the Upgrade Tasks are run, the plugin system has been started and a restart is required
        // for the change to the property string to take effect.
        // Therefore, we will use the PluginController to enable the UPM plugin.
        Plugin plugin = pluginAccessor.getPlugin(UPM_KEY);
        if (plugin != null && plugin.getPluginState() != PluginState.ENABLED)
        {
            pluginController.enablePlugins(UPM_KEY);
        }
    }

    @Override
    public String getShortDescription()
    {
        return "Ensure UPM is enabled";
    }
}
