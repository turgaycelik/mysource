package com.atlassian.jira.cluster;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.atlassian.beehive.db.ClusterNodeHeartbeatService;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.index.ha.NodeReindexService;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.mock.component.MockComponentWorker;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultClusterManager
{
    public static final String NODE_ID = "NODE 1";

    @Rule public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock private ApplicationProperties mockApplicationProperties;
    @Mock private ClusterServicesRegistry mockClusterServicesRegistry;
    @Mock private ClusterNodeProperties mockClusterNodeProperties;
    @Mock private JiraProperties mockJiraProperties;
    @Mock private NodeStateManager mockNodeStateManager;
    @Mock private EventPublisher mockEventPublisher;
    @Mock private ClusterNodeHeartbeatService heartbeatService;
    @Mock
    @AvailableInContainer
    private NodeReindexService mockReindexService;
    @Mock
    @AvailableInContainer
    private MessageHandlerService mockMessageHandlerService;

    @Mock
    @AvailableInContainer
    private JiraLicenseManager mockJiraLicenseManager;
    private ClusterManager clusterManager;


    @Before
    public void setup()
    {
        when(mockClusterServicesRegistry.getNodeReindexService()).thenReturn(mockReindexService);
        when(mockClusterServicesRegistry.getMessageHandlerService()).thenReturn(mockMessageHandlerService);
        clusterManager = new DefaultClusterManager(mockClusterServicesRegistry, mockNodeStateManager, mockEventPublisher);
    }

    @Test
    public void testGetNodeId() throws Exception
    {
        makeClustered();
        assertEquals("Node id should be NODE 1", NODE_ID, clusterManager.getNodeId());
    }

    @Test
    public void testGetNodeIdReturnsNullWhenNotClustered()
    {
        makeNonClustered();
        assertNull("get node id should return null", clusterManager.getNodeId());
    }

    @Test
    public void testCheckIndex()
    {
        makePassive();
        clusterManager.checkIndex();
        verify(mockReindexService).canIndexBeRebuilt();
    }

    @Test
    public void testRequestCurrentIndexFromNode()
    {
        makeClustered();
        clusterManager.requestCurrentIndexFromNode("2");
        verify(mockMessageHandlerService).sendMessage("2", Message.fromString("Backup Index"));
    }

    @Test
    public void testFirstNodeUpBecomesActive()
    {
        makeClustered();
        assertTrue("node should be active", clusterManager.isActive());
    }

    @Test
    public void testIsClustered()
    {
        makeClustered();
        assertTrue("node should be clustered", clusterManager.isClustered());
    }

    @Test
    public void testFindLiveNodes()
    {
        final Node node1 = new Node("node1", Node.NodeState.ACTIVE);
        final Node node2 = new Node("node2", Node.NodeState.ACTIVE);
        final Set<Node> nodes = ImmutableSet.of(node1, node2, new Node("node3", Node.NodeState.OFFLINE));

        MockComponentWorker worker = new MockComponentWorker();
        worker.init();
        worker.registerMock(ClusterNodeHeartbeatService.class, heartbeatService);

        makeClustered();
        when(mockNodeStateManager.getAllNodes()).thenReturn(nodes);
        when(heartbeatService.findLiveNodes()).thenReturn(Arrays.asList("node1", "node2", "node3"));

        final Collection<Node> liveNodes = clusterManager.findLiveNodes();
        assertThat(liveNodes, hasSize(2));
        assertThat(liveNodes, contains(node1, node2));
    }

    private void makeClustered()
    {
        when(mockClusterNodeProperties.getNodeId()).thenReturn(NODE_ID);
        when(mockClusterNodeProperties.propertyFileExists()).thenReturn(true);
        when(mockClusterNodeProperties.isValid()).thenReturn(true);
        when(mockNodeStateManager.getNode()).thenReturn(new Node(NODE_ID, Node.NodeState.ACTIVE));
    }

    private void makeNonClustered()
    {
        when(mockClusterNodeProperties.getNodeId()).thenReturn(null);
        when(mockClusterNodeProperties.propertyFileExists()).thenReturn(false);
        when(mockClusterNodeProperties.isValid()).thenReturn(true);
        when(mockNodeStateManager.getNode()).thenReturn(Node.NOT_CLUSTERED);
    }

    private void makePassive()
    {
        when(mockClusterNodeProperties.getNodeId()).thenReturn(NODE_ID);
        when(mockClusterNodeProperties.propertyFileExists()).thenReturn(true);
        when(mockClusterNodeProperties.isValid()).thenReturn(true);
        when(mockNodeStateManager.getNode()).thenReturn(new Node(NODE_ID, Node.NodeState.PASSIVE));
    }
}
