package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v6.3
 */
@RunWith (ListeningMockitoRunner.class)
public class TestIsClusteredCondition
{
    @Mock
    ClusterManager clusterManager;

    @Test
    public void shouldDisplayWhenClustered()
    {
        when(clusterManager.isClustered()).thenReturn(true);
        assertTrue(new IsClusteredCondition(clusterManager).shouldDisplay(null, null));
    }

    @Test
    public void shouldHideWhenSingle()
    {
        when(clusterManager.isClustered()).thenReturn(false);
        assertFalse(new IsClusteredCondition(clusterManager).shouldDisplay(null, null));
    }
}
