package com.atlassian.jira.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.atlassian.cache.CacheManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.cluster.NodeActivatedEvent;
import com.atlassian.jira.event.cluster.NodeActivatingEvent;
import com.atlassian.jira.event.cluster.NodePassivatedEvent;
import com.atlassian.jira.event.cluster.NodePassivatingEvent;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.user.util.DirectorySynchroniserBarrier;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.scheduler.core.LifecycleAwareSchedulerService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of DefaultNodeStateManager.
 *
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultNodeStateManager
{
    // Constants
    private static final String THIS_NODE_ID = "this";
    private static final String OTHER_NODE_ID = "that";
    private static final Long LISTENER_PORT = Long.valueOf(EhCacheConfigurationFactory.DEFAULT_LISTENER_PORT);
    private static final long TIMESTAMP = System.currentTimeMillis();

    // Fixture
    @Rule public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock private CacheManager mockCacheManager;
    @Mock private ComponentFactory mockComponentFactory;
    @Mock private DirectorySynchroniserBarrier mockDirectorySynchroniserBarrier;
    @Mock private EventPublisher mockEventPublisher;
    @Mock private ClusterNodeProperties mockClusterNodeProperties;
    @Mock private JiraSystemRestarter mockJiraSystemRestarter;
    @Mock private MailQueue mockMailQueue;
    @Mock private Node mockNode;
    @Mock private OfBizClusterNodeStore mockOfBizClusterNodeStore;
    @Mock private LifecycleAwareSchedulerService mockSchedulerService;
    @Mock @AvailableInContainer private TaskManager mockTaskManager;
    @Mock @AvailableInContainer private ClusterManager mockClusterManager;

    private DefaultNodeStateManager nodeStateManager;

    @Before
    public void setUp()
    {
        nodeStateManager = new DefaultNodeStateManager(mockOfBizClusterNodeStore, mockClusterNodeProperties,
                mockSchedulerService, mockComponentFactory, mockMailQueue, mockEventPublisher,
                mockJiraSystemRestarter, mockCacheManager);
    }

    @Test
    public void testActivate() throws Exception
    {

        // Set up
        final List<Node> nodeList = new ArrayList<Node>(2);
        nodeList.add(new Node(THIS_NODE_ID, Node.NodeState.PASSIVE, TIMESTAMP, JiraUtils.getHostname(), LISTENER_PORT));
        nodeList.add(new Node(OTHER_NODE_ID, Node.NodeState.ACTIVE));
        when(mockClusterNodeProperties.getNodeId()).thenReturn(THIS_NODE_ID);
        when(mockClusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_PORT)).thenReturn(LISTENER_PORT.toString());
        when(mockOfBizClusterNodeStore.findNodes(null, Collections.singletonList(OfBizClusterNodeStore.NODE_ID)))
                .thenReturn(nodeList);
        when(mockOfBizClusterNodeStore.getNode(THIS_NODE_ID))
                .thenReturn(nodeList.get(0), new Node(THIS_NODE_ID, Node.NodeState.ACTIVATING, System.currentTimeMillis(), JiraUtils.getHostname(), LISTENER_PORT));

        // Invoke
        nodeStateManager.activate();

        // Check
        verify(mockEventPublisher).publish(ClearCacheEvent.INSTANCE);
        verify(mockCacheManager).flushCaches();
        verify(mockSchedulerService).start();
        verify(mockTaskManager).start();
        verify(mockEventPublisher).publish(NodeActivatingEvent.INSTANCE);
        verify(mockEventPublisher).publish(NodeActivatedEvent.INSTANCE);
        verify(mockOfBizClusterNodeStore).updateNode(THIS_NODE_ID, Node.NodeState.ACTIVATING, JiraUtils.getHostname(), LISTENER_PORT);
        verify(mockOfBizClusterNodeStore).updateNode(THIS_NODE_ID, Node.NodeState.ACTIVE, JiraUtils.getHostname(),  LISTENER_PORT);
    }

    @Test
    public void testDeactivateWhenSchedulerActive() throws Exception
    {
        // Set up
        when(mockClusterNodeProperties.getNodeId()).thenReturn(THIS_NODE_ID);
        when(mockClusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_PORT)).thenReturn(LISTENER_PORT.toString());
        when(mockOfBizClusterNodeStore.getNode(THIS_NODE_ID)).thenReturn(new Node(THIS_NODE_ID, Node.NodeState.ACTIVE, TIMESTAMP, JiraUtils.getHostname(), LISTENER_PORT));
        when(mockComponentFactory.createObject(DirectorySynchroniserBarrier.class))
                .thenReturn(mockDirectorySynchroniserBarrier);

        // Invoke
        nodeStateManager.deactivate();

        // Check
        verify(mockSchedulerService).standby();
        verify(mockTaskManager).shutdownAndWait(20);
        verify(mockDirectorySynchroniserBarrier).await(20, TimeUnit.SECONDS);
        verify(mockMailQueue).sendBuffer();
        verify(mockEventPublisher).publish(NodePassivatingEvent.INSTANCE);
        verify(mockEventPublisher).publish(NodePassivatedEvent.INSTANCE);
        verify(mockOfBizClusterNodeStore).updateNode(THIS_NODE_ID, Node.NodeState.PASSIVATING, JiraUtils.getHostname(), LISTENER_PORT);
        verify(mockOfBizClusterNodeStore).updateNode(THIS_NODE_ID, Node.NodeState.PASSIVE, JiraUtils.getHostname(), LISTENER_PORT);
    }

    @Test(expected = NotClusteredException.class)
    public void activatingNonClusteredInstanceShouldThrowException() throws Exception
    {
        //Set in no cluster
        when(mockClusterNodeProperties.getNodeId()).thenReturn(null);

        // Invoke
        nodeStateManager.activate();
    }

    @Test(expected = NotClusteredException.class)
    public void deactivatingNonClusteredInstanceShouldThrowException() throws Exception
    {
        //Set in no cluster
        when(mockClusterNodeProperties.getNodeId()).thenReturn(null);

        // Invoke
        nodeStateManager.deactivate();
    }

    @Test
    public void testStateHasChanged()
    {
        Node node = new Node(THIS_NODE_ID, Node.NodeState.OFFLINE, System.currentTimeMillis(), JiraUtils.getHostname(), LISTENER_PORT);
        assertThat(nodeStateManager.stateHasChanged(node), is(true));

        node = new Node(THIS_NODE_ID, Node.NodeState.ACTIVE, System.currentTimeMillis(), "another-hostname", LISTENER_PORT);
        assertThat(nodeStateManager.stateHasChanged(node), is(true));

        node = new Node(THIS_NODE_ID, Node.NodeState.ACTIVE, System.currentTimeMillis(), "another-hostname", 1234l);
        assertThat(nodeStateManager.stateHasChanged(node), is(true));

        // and now a proper state
        node = new Node(THIS_NODE_ID, Node.NodeState.ACTIVE, System.currentTimeMillis(), JiraUtils.getHostname(), LISTENER_PORT);
        assertThat(nodeStateManager.stateHasChanged(node), is(false));
    }

    @Test
    public void testGetHostnameSetInProperties()
    {
        when(mockClusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_HOSTNAME)).thenReturn("atlassian-test");
        assertThat(nodeStateManager.buildHostname(), is("atlassian-test"));
    }

    @Test
    public void testNoHostnameInProperties()
    {
        when(mockClusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_HOSTNAME)).thenReturn(null);
        assertThat(nodeStateManager.buildHostname(), is(JiraUtils.getHostname()));
    }

    @Test
    public void testGetCacheListenerPortInProperties()
    {
        when(mockClusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_PORT)).thenReturn("1234");
        assertThat(nodeStateManager.getCacheListenerPort(), is(1234l));
    }

    @Test
    public void testGetDefaultCacheListenerPort()
    {
        when(mockClusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_PORT)).thenReturn(null);
        assertThat(nodeStateManager.getCacheListenerPort(), is(LISTENER_PORT));
    }
}
