package com.atlassian.jira.cluster.lock;

import com.atlassian.jira.entity.EntityEngine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.entity.ClusterLockStatusEntity.LOCKED_BY_NODE;
import static com.atlassian.jira.entity.Delete.from;
import static com.atlassian.jira.entity.Entity.CLUSTER_LOCK_STATUS;
import static org.mockito.Mockito.verify;

/**
 * Unit test of JiraClusterLockDao.
 *
 * @since 6.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestJiraClusterLockDao
{
    private static final String NODE_ID = "theNodeId";

    @Mock private EntityEngine mockEntityEngine;

    @InjectMocks private JiraClusterLockDao dao;

    @Test
    public void daoShouldBeAbleToDeleteLocksHeldByAGivenNode()
    {
        // Invoke
        dao.deleteLocksHeldByNode(NODE_ID);

        // Check
        verify(mockEntityEngine).delete(from(CLUSTER_LOCK_STATUS).whereEqual(LOCKED_BY_NODE, NODE_ID));
    }
}
