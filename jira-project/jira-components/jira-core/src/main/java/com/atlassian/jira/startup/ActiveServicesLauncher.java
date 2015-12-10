package com.atlassian.jira.startup;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.ha.PluginMessageSender;
import com.atlassian.jira.scheduler.JiraPassivatedSchedulerLauncher;
import com.atlassian.jira.scheduler.JiraSchedulerLauncher;
import com.atlassian.jira.upgrade.PluginUpgradeLauncher;
import com.atlassian.jira.upgrade.UpgradeLauncher;
import com.google.common.annotations.VisibleForTesting;

/**
 * There are a number of services that should only be started in the active mode, start them here
 *
 * @since v6.1
 */
public class ActiveServicesLauncher implements JiraLauncher
{

    private final PluginUpgradeLauncher pluginUpgradeLauncher;
    private final UpgradeLauncher upgradeLauncher;

    public ActiveServicesLauncher() {
        this.upgradeLauncher = new UpgradeLauncher();
        this.pluginUpgradeLauncher = new PluginUpgradeLauncher();
    }

    @VisibleForTesting
    ActiveServicesLauncher(final UpgradeLauncher upgradeLauncher, final PluginUpgradeLauncher pluginUpgradeLauncher)
    {
        this.upgradeLauncher = upgradeLauncher;
        this.pluginUpgradeLauncher = pluginUpgradeLauncher;
    }

    @Override
    public void start()
    {

        final ClusterManager clusterManager = ComponentAccessor.getComponent(ClusterManager.class);
        if (clusterManager.isActive())
        {
            upgradeLauncher.start();
            pluginUpgradeLauncher.start();
            if (clusterManager.isClustered())
            {
                ComponentAccessor.getComponent(PluginMessageSender.class).activate();
            }
            getSchedulerLauncher().start();
        }
    }

    @Override
    public void stop()
    {
        pluginUpgradeLauncher.stop();
        upgradeLauncher.stop();
        getSchedulerLauncher().stop();
        final PluginMessageSender messageSender = ComponentAccessor.getComponent(PluginMessageSender.class);
        if (messageSender != null)
        {
            messageSender.stop();
        }
    }

    private JiraSchedulerLauncher getSchedulerLauncher()
    {
        final ClusterManager clusterManager = ComponentAccessor.getComponent(ClusterManager.class);
        if (clusterManager != null && clusterManager.isActive())
        {
            return new JiraSchedulerLauncher();
        }
        else
        {
            return new JiraPassivatedSchedulerLauncher();
        }
    }
}
