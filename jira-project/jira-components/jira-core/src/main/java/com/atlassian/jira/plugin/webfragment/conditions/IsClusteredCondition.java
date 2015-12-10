package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Checks to see if JIRA is running in a cluster.
 *
 * @since v6.3
 */
@SuppressWarnings("unused")
public class IsClusteredCondition extends AbstractWebCondition
{
    private final boolean clustered;

    public IsClusteredCondition(final ClusterManager clusterManager)
    {
        this.clustered = clusterManager.isClustered();
    }

    @Override
    public boolean shouldDisplay(final ApplicationUser user, final JiraHelper jiraHelper)
    {
        return clustered;
    }
}
