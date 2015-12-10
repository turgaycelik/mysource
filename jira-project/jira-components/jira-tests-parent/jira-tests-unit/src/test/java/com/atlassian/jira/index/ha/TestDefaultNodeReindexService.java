package com.atlassian.jira.index.ha;

import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.bc.project.index.ProjectReindexService;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.Node;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultNodeReindexService
{

    @Mock
    private ClusterManager clusterManager;
    @Mock
    private OfBizNodeIndexCounterStore ofBizNodeIndexCounterStore;
    @Mock
    private OfBizReplicatedIndexOperationStore ofBizNodeIndexOperationStore;
    @Mock
    private IssueIndexManager indexManager;
    @Mock
    private SharedEntityIndexer sharedEntityIndexer;
    @Mock
    private ProjectManager projectManager;
    @Mock
    private ProjectReindexService projectReindexService;
    @Mock
    private IssueManager issueManager;
    @Mock
    private CommentManager commentManager;
    @Mock
    private OfBizDelegator ofBizDelegator;
    @Mock
    private SharedEntityResolver sharedEntityResolver;
    @Mock
    private TaskManager taskManager;
    @Mock
    private I18nHelper i18nHelper;
    @Mock
    private InstrumentRegistry instruments;

    private DefaultNodeReindexService service;

    @Before
    public void setUp()
    {
        Set<Node> nodes = ImmutableSet.of(new Node("node1", Node.NodeState.ACTIVE, System.currentTimeMillis(), "localhost", 40001l),
                new Node("node2", Node.NodeState.ACTIVE, System.currentTimeMillis(), "localhost", 40002l));

        when(clusterManager.isClustered()).thenReturn(true);
        when(clusterManager.getAllNodes()).thenReturn(nodes);
        when(clusterManager.getNodeId()).thenReturn("node1");

        new MockComponentWorker().addMock(InstrumentRegistry.class, instruments).init();

        // We must initialise the DNRS after setting up the interactions for ClusterManager, as the ClusterManager is
        // interrogated during DNRS construction.
        service = new DefaultNodeReindexService(clusterManager, ofBizNodeIndexCounterStore, ofBizNodeIndexOperationStore,
                indexManager, sharedEntityIndexer, projectManager, projectReindexService, issueManager, commentManager,
                ofBizDelegator, sharedEntityResolver, null);
    }

    @Test
    public void testCacheCanBeRebuilt()
    {
        when(indexManager.isIndexConsistent()).thenReturn(true);
        when(ofBizNodeIndexCounterStore.getIndexOperationCounterForNodeId("node1", "node2")).thenReturn(1000l);
        when(ofBizNodeIndexOperationStore.contains(1000l)).thenReturn(true);

        assertThat(service.canIndexBeRebuilt(), is(true));
        verify(indexManager, times(1)).isIndexConsistent();
    }

    @Test
    public void testMultipleStartCalls()
    {
        service.start();
        final Object service1 = service.getIndexerService();
        service.start();
        final Object service2 = service.getIndexerService();
        // Still the same instance
        assertThat(service2, sameInstance(service1));
    }

    @Test
    public void testStartAfterPause()
    {
        service.start();
        final Object service1 = service.getIndexerService();
        assertThat(service1, not(nullValue()));
        service.pause();
        assertThat(service.getIndexerService(), nullValue());
        service.start();
        final Object service2 = service.getIndexerService();
        // Not the same instance
        assertThat(service2, not(sameInstance(service1)));
    }

    @Test(expected = RejectedExecutionException.class)
    public void testStartAfterCancelIsRejected()
    {
        service.start();
        service.cancel();
        service.start();
    }
}
