package com.atlassian.jira.plugin.ha;

import com.atlassian.jira.plugin.ClusterAwareJiraPluginController;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import org.apache.log4j.Logger;

/**
 *  disable/enables our local plugin copies
 *
 * @since v6.1
 */
public class ReplicatedPluginManager
{
    private final ClusterAwareJiraPluginController pluginController;
    private final PluginAccessor pluginAccessor;

    private static final Logger log = Logger.getLogger(ReplicatedPluginManager.class);

    public ReplicatedPluginManager(final ClusterAwareJiraPluginController pluginController, final PluginAccessor pluginAccessor)
    {
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
    }

    public void enablePlugin(String pluginKey)
    {
        pluginController.enablePluginsLocalOnly(pluginKey);
    }

    public void upgradePlugin(String pluginKey)
    {
        pluginController.scanForNewPlugins();
    }

    public void disablePlugin(String pluginKey)
    {
        pluginController.disablePluginLocalOnly(pluginKey);
    }

    public void enablePluginModule(String completeKey)
    {
        pluginController.enablePluginModuleLocalOnly(completeKey);
    }

    public void disablePluginModule(String completeKey)
    {
        pluginController.disablePluginModuleLocalOnly(completeKey);
    }

    public void installPlugin(final String pluginKey)
    {
        pluginController.scanForNewPlugins();
    }

    public void uninstallPlugin(final String pluginKey)
    {
        Plugin plugin = pluginAccessor.getPlugin(pluginKey);
        if (plugin != null )
        {
            pluginController.uninstallLocalOnly(plugin);
        }
    }
}
