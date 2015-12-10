package com.atlassian.jira;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.db.ClusterNodeHeartbeatService;
import com.atlassian.beehive.db.spi.ClusterLockDao;
import com.atlassian.beehive.db.spi.ClusterNodeHeartBeatDao;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.cluster.lock.JiraClusterLockDao;
import com.atlassian.jira.cluster.lock.JiraClusterNodeHeartBeatDao;
import com.atlassian.jira.cluster.lock.NullClusterNodeHeartbeatService;
import com.atlassian.jira.cluster.lock.NullJiraClusterNodeHeartBeatDao;
import com.atlassian.jira.cluster.lock.StartableClusterNodeHeartbeatService;
import com.atlassian.jira.cluster.lock.StartableDatabaseClusterLockService;
import com.atlassian.jira.cluster.lock.TimedClusterNodeHeartBeatDao;

import static com.atlassian.jira.ComponentContainer.Scope.INTERNAL;
import static com.atlassian.jira.ComponentContainer.Scope.PROVIDED;

/**
 * Registers the ClusterLockService as a JIRA component.
 *
 * @since 6.3
 */
public class LockServiceRegistrar
{
    /**
     * Registers the ClusterLockService with the given component container.
     *
     * @param container the container with which to register the service (required)
     */
    public static void registerLockService(final ComponentContainer container)
    {
        if (isClustered(container))
        {
            registerDatabaseLockService(container);
        }
        else
        {
            registerJvmLockService(container);
        }
    }

    private static boolean isClustered(final ComponentContainer container)
    {
        final ClusterNodeProperties clusterNodeProperties = container.getComponentInstance(ClusterNodeProperties.class);
        return clusterNodeProperties != null && clusterNodeProperties.getNodeId() != null;
    }

    private static void registerDatabaseLockService(final ComponentContainer container)
    {
        // required dependency for ClusterLockService
        container.implementation(INTERNAL, ClusterNodeHeartbeatService.class, StartableClusterNodeHeartbeatService.class);
        // ClusterLock Service
        container.implementation(PROVIDED, ClusterLockService.class, StartableDatabaseClusterLockService.class);
        // DAOs for the DatabaseClusterLockService
        container.implementation(INTERNAL, ClusterLockDao.class, JiraClusterLockDao.class);
        container.implementation(INTERNAL, ClusterNodeHeartBeatDao.class, JiraClusterNodeHeartBeatDao.class);
        container.implementation(INTERNAL, TimedClusterNodeHeartBeatDao.class, JiraClusterNodeHeartBeatDao.class);
    }

    private static void registerJvmLockService(final ComponentContainer container)
    {
        container.implementation(PROVIDED, ClusterLockService.class, SimpleClusterLockService.class);
        container.implementation(INTERNAL, ClusterNodeHeartBeatDao.class, NullJiraClusterNodeHeartBeatDao.class);
        container.implementation(INTERNAL, ClusterNodeHeartbeatService.class, NullClusterNodeHeartbeatService.class);
        container.implementation(INTERNAL, TimedClusterNodeHeartBeatDao.class, NullJiraClusterNodeHeartBeatDao.class);

    }
}
