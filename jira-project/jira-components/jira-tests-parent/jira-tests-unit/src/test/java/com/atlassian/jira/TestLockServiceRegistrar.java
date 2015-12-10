package com.atlassian.jira;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.db.spi.ClusterLockDao;
import com.atlassian.beehive.db.spi.ClusterNodeHeartBeatDao;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.cluster.lock.JiraClusterLockDao;
import com.atlassian.jira.cluster.lock.JiraClusterNodeHeartBeatDao;
import com.atlassian.jira.cluster.lock.StartableDatabaseClusterLockService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.ComponentContainer.Scope.INTERNAL;
import static com.atlassian.jira.ComponentContainer.Scope.PROVIDED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestLockServiceRegistrar
{
    @Mock private ComponentContainer mockComponentContainer;

    private void setUpClustering()
    {
        final ClusterNodeProperties mockClusterNodeProperties = mock(ClusterNodeProperties.class);
        when(mockClusterNodeProperties.getNodeId()).thenReturn("anyNodeId");
        when(mockComponentContainer.getComponentInstance(ClusterNodeProperties.class))
                .thenReturn(mockClusterNodeProperties);
    }

    @Test
    public void shouldRegisterSimpleClusterLockServiceWhenNotClustered()
    {
        // Invoke
        LockServiceRegistrar.registerLockService(mockComponentContainer);

        // Check
        verify(mockComponentContainer).implementation(
                PROVIDED, ClusterLockService.class, SimpleClusterLockService.class);
    }

    @Test
    public void shouldRegisterDatabaseBackedClusterLockServiceWhenClustered()
    {
        // Set up
        setUpClustering();

        // Invoke
        LockServiceRegistrar.registerLockService(mockComponentContainer);

        // Check
        verify(mockComponentContainer).implementation(
                PROVIDED, ClusterLockService.class, StartableDatabaseClusterLockService.class);
        verify(mockComponentContainer).implementation(
                INTERNAL, ClusterLockDao.class, JiraClusterLockDao.class);
        verify(mockComponentContainer).implementation(
                INTERNAL, ClusterNodeHeartBeatDao.class, JiraClusterNodeHeartBeatDao.class);
    }
}
