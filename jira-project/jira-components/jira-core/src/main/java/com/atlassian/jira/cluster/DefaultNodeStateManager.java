package com.atlassian.jira.cluster;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.CacheManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.cluster.NodeActivatedEvent;
import com.atlassian.jira.event.cluster.NodeActivatingEvent;
import com.atlassian.jira.event.cluster.NodePassivatedEvent;
import com.atlassian.jira.event.cluster.NodePassivatingEvent;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.user.util.DirectorySynchroniserBarrier;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.core.LifecycleAwareSchedulerService;
import com.atlassian.util.concurrent.ResettableLazyReference;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.cluster.Node.NodeState.ACTIVATING;
import static com.atlassian.jira.cluster.Node.NodeState.ACTIVE;
import static com.atlassian.jira.cluster.Node.NodeState.OFFLINE;
import static com.atlassian.jira.cluster.Node.NodeState.PASSIVATING;
import static com.atlassian.jira.cluster.Node.NodeState.PASSIVE;

/**
 * Manage the state of the current node. The {@link #activate()} and {@link #deactivate()} methods are synchronised as
 * we should only ever be running one of those methods at a time to avoid weird race conditions that would be possible
 * if we tried to go from active to passive and vice-versa at the same time.
 *
 * @since v6.1
 */
@EventComponent
public class DefaultNodeStateManager implements NodeStateManager
{
    private static final Logger LOG = Logger.getLogger(DefaultNodeStateManager.class);
    private static final int WAIT_SECONDS = 20;

    private final CacheManager cacheManager;
    private final EventPublisher eventPublisher;
    private final ClusterNodeProperties clusterNodeProperties;
    private final LifecycleAwareSchedulerService schedulerService;
    private final ComponentFactory componentFactory;
    private final JiraSystemRestarter jiraSystemRestarter;
    private final MailQueue mailQueue;
    private final OfBizClusterNodeStore ofBizClusterNodeStore;
    private final String hostname;

    // A reference to the node where this cluster instance is running
    @ClusterSafe
    private final ResettableLazyReference<Node> nodeRef = new ResettableLazyReference<Node>()
    {
        @Override
        protected Node create() throws ClusterStateException
        {
            return initializeNode();
        }
    };


    public DefaultNodeStateManager(final OfBizClusterNodeStore ofBizClusterNodeStore,
            final ClusterNodeProperties clusterNodeProperties, final LifecycleAwareSchedulerService schedulerService,
            final ComponentFactory componentFactory, final MailQueue mailQueue, final EventPublisher eventPublisher,
            final JiraSystemRestarter jiraSystemRestarter, final CacheManager cacheManager)
    {
        this.ofBizClusterNodeStore = ofBizClusterNodeStore;
        this.clusterNodeProperties = clusterNodeProperties;
        this.schedulerService = schedulerService;
        this.componentFactory = componentFactory;
        this.mailQueue = mailQueue;
        this.eventPublisher = eventPublisher;
        this.jiraSystemRestarter = jiraSystemRestarter;
        this.cacheManager = cacheManager;
        this.hostname = buildHostname();
    }

    @EventListener
    public void clearCache(@SuppressWarnings ("unused") final ClearCacheEvent event)
    {
        nodeRef.reset();
    }

    /**
     * We are going to change the state when the component manager stops so the other nodes do not get alerts about this
     * node
     */
    public void shutdownNode()
    {
        final Node node = getNode();

        if (node != null && node.getNodeId() != null)
        {
            ofBizClusterNodeStore.updateNode(node.getNodeId(), OFFLINE, hostname, getCacheListenerPort());
        }
    }

    @Override
    public Node getNode()
    {
        return nodeRef.get();
    }

    @Override
    public Node getNodeWithRefresh()
    {
        nodeRef.reset();
        return getNode();
    }

    @Override
    public Set<Node> getAllNodes()
    {
        return ImmutableSet.copyOf(ofBizClusterNodeStore.getAllNodes());
    }

    Node initializeNode() throws ClusterStateException
    {
        final String nodeId = clusterNodeProperties.getNodeId();
        if (nodeId == null)
        {
            return Node.NOT_CLUSTERED;
        }
        if (StringUtils.isBlank(nodeId))
        {
            throw new ClusterStateException("The cluster node ID was explicitly set to a blank value");
        }
        return getOrCreateNode(nodeId);
    }

    /**
     * We get or create the node.
     * But if we get the node and the state is different we need to update it also
     *
     * @param nodeId the node id
     * @return the new node
     */
    private Node getOrCreateNode(final String nodeId)
    {
        Node node = ofBizClusterNodeStore.getNode(nodeId);

        //This is the first scenario when the cluster was started correctly
        if (node == null)
        {
            node = ofBizClusterNodeStore.createNode(nodeId, ACTIVE, hostname, getCacheListenerPort());
        }
        else if (stateHasChanged(node))
        {
            //This scenario occurs when a graceful shutdown occurred and we need to restart
            node = ofBizClusterNodeStore.updateNode(nodeId, ACTIVE, hostname, getCacheListenerPort());
        }

        return node;
    }

    @Override
    @ClusterSafe ("This is a local node instance behaviour.")
    public synchronized void activate() throws ClusterStateException
    {
        final Node currentNode = getNode();
        final String nodeId = requireNodeId(currentNode);

        LOG.info("Activating cluster instance: '" + nodeId + '\'');
        eventPublisher.publish(NodeActivatingEvent.INSTANCE);
        updateState(currentNode, ACTIVATING);
        nodeRef.reset();

        eventPublisher.publish(ClearCacheEvent.INSTANCE);
        cacheManager.flushCaches();

        startServices();
        updateState(currentNode, ACTIVE);

        eventPublisher.publish(NodeActivatedEvent.INSTANCE);
        LOG.info("Activated cluster instance: '" + currentNode.getNodeId() + '\'');
    }

    private void startServices()
    {
        try
        {
            schedulerService.start();
        }
        catch (SchedulerServiceException e)
        {
            throw new IllegalStateException(e);
        }
        getTaskManager().start();
    }

    /**
     * Stores the given node state in the database.
     *
     * @param node the node for which to store the state (required, must have an ID)
     * @param state the state to set (required)
     * @throws NotClusteredException if we are not clustered
     */
    private void updateState(final Node node, final Node.NodeState state) throws NotClusteredException
    {
        ofBizClusterNodeStore.updateNode(requireNodeId(node), state, hostname, getCacheListenerPort());
        nodeRef.reset();
    }

    /**
     * There are times when you want the server to go into a deep sleep mode, such as when an import is taking place on
     * the cluster.
     */
    @Override
    @ClusterSafe ("This is a local node instance behaviour.")
    public synchronized void quiesce() throws NotClusteredException
    {
        final String nodeId = requireNodeId(getNode());
        LOG.info("Quiescing cluster instance: '" + nodeId + '\'');
        shutdownAndFlushAsyncServices();
    }

    @Override
    public void restart()
    {
        if (ComponentManager.getInstance().getState().isStarted())
        {
            jiraSystemRestarter.ariseSirJIRA();
        }
    }

    @Override
    @ClusterSafe ("This is a local node instance behaviour.")
    public synchronized void deactivate() throws NotClusteredException
    {
        final Node currentNode = getNodeWithRefresh();
        final String nodeId = requireNodeId(currentNode);

        LOG.info("Passivating cluster instance: '" + nodeId + '\'');
        eventPublisher.publish(NodePassivatingEvent.INSTANCE);

        updateState(currentNode, PASSIVATING);
        nodeRef.reset();

        shutdownAndFlushAsyncServices();

        updateState(currentNode, PASSIVE);
        nodeRef.reset();

        eventPublisher.publish(NodePassivatedEvent.INSTANCE);
        LOG.info("Passivated cluster instance: '" + nodeId + '\'');
    }

    /**
     * We validate if the state of the node has changed.
     *
     * 1) if the node is in offline state in the db, and we are initializing it.
     * 2) if the node has a different IP address.
     * 3) if the port of multicasting has changed
     * @param node the node
     * @return true if the state has changed, false if not
     */
    protected boolean stateHasChanged(Node node)
    {
        return node.getState() == OFFLINE
                || !StringUtils.equalsIgnoreCase(hostname, node.getIp())
                || !Objects.equal(node.getCacheListenerPort(), getCacheListenerPort());
    }

    protected Long getCacheListenerPort()
    {
        final String port = clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_PORT);
        return Long.valueOf(port != null ? port : EhCacheConfigurationFactory.DEFAULT_LISTENER_PORT);
    }

    /**
     * We are going to evaluate if the user set a hostname in the .properties file
     * if that is the case then we need to use that one all over the cluster
     * @return the ip/hostname
     */
    protected String buildHostname()
    {
        String hostname = clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_HOSTNAME);
        return hostname != null ? hostname : JiraUtils.getHostname();
    }

    private void shutdownAndFlushAsyncServices()
    {
        try
        {
            schedulerService.standby();
        }
        catch (SchedulerServiceException e)
        {
            throw new RuntimeException(e);
        }

        //We need to clean up the task manager now. Leaving tasks running during import may cause a deadlock.
        //The task manager is restarted in globalRefresh.
        cleanUpTaskManager();

        //Crowd can be synching with remote directories. We need to wait until this has finished.
        cleanUpCrowd();

        // Send the emails on the Mail Queue
        try
        {
            mailQueue.sendBuffer();
        }
        catch (final RuntimeException e)
        {
            LOG.warn("Sending buffer failed: " + e.getMessage(), e);
        }
    }

    /**
     * We need this "barrier" to ensure that we don't get synchronizations running across multiple servers at the same
     * time.
     */
    private void cleanUpCrowd()
    {
        DirectorySynchroniserBarrier barrier = componentFactory.createObject(DirectorySynchroniserBarrier.class);
        if (!barrier.await(WAIT_SECONDS, TimeUnit.SECONDS))
        {
            LOG.error("Unable to stop remote directory synchronization.");
        }
    }

    private void cleanUpTaskManager()
    {
        // Shutdown the task manager before the import. Tasks should not be running on the passive server.
        getTaskManager().shutdownAndWait(WAIT_SECONDS);
    }

    // Injection would cause a circular dependency
    private static TaskManager getTaskManager()
    {
        return ComponentAccessor.getComponent(TaskManager.class);
    }

    @Nonnull
    private static String requireNodeId(@Nonnull Node node) throws NotClusteredException
    {
        final String nodeId = node.getNodeId();
        if (nodeId == null)
        {
            throw new NotClusteredException();
        }
        return nodeId;
    }
}
