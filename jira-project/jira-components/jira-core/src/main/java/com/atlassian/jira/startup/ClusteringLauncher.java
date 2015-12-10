package com.atlassian.jira.startup;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.ClusterServicesManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.upgrade.PluginUpgradeLauncher;
import com.atlassian.jira.upgrade.UpgradeLauncher;

/**
 * Sets up clustered services as appropriate, starts upgrade services and scheduler as needed
 *
 * @since v6.1
 */
public class ClusteringLauncher implements JiraLauncher
{

    @Override
    public void start()
    {
        final ClusterManager clusterManager = ComponentAccessor.getComponent(ClusterManager.class);
        if (clusterManager.isClustered())
        {
            clusterManager.checkIndex();
        }
    }

    @Override
    public void stop()
    {
        final ClusterServicesManager clusterServicesManager = ComponentAccessor.getComponent(ClusterServicesManager.class);
        clusterServicesManager.stopServices();
    }
}
