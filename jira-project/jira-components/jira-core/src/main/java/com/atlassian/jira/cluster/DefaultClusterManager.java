package com.atlassian.jira.cluster;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.beehive.db.ClusterNodeHeartbeatService;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentReference;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.index.ha.IndexesRestoredEvent;
import com.atlassian.jira.index.ha.NodeReindexService;
import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;

import com.google.common.collect.ImmutableSet;

import static com.atlassian.jira.cluster.Node.NodeState.ACTIVE;
import static com.atlassian.jira.index.ha.DefaultIndexCopyService.BACKUP_INDEX;

/**
 * Manages the cluster - addition of nodes, removal, etc
 *
 * @since v6.1
 */
public class DefaultClusterManager implements ClusterManager, Startable
{
    // Fields
    private final ClusterServicesRegistry clusterServicesRegistry;
    private final NodeStateManager nodeStateManager;
    private final EventPublisher eventPublisher;

    /**
     * This list will hold the list of nodes till a new heartbeat occurs and the list is refreshed.
     */
    private Collection<Node> liveNodes;

    @ClusterSafe ("This reference is loaded like this to avoid cyclic dependency")
    private final ComponentReference<ClusterNodeHeartbeatService> heartbeatServiceRef = ComponentAccessor.getComponentReference(ClusterNodeHeartbeatService.class);

    public DefaultClusterManager(final ClusterServicesRegistry clusterServicesRegistry,
            final NodeStateManager nodeStateManager, final EventPublisher eventPublisher)
    {
        this.clusterServicesRegistry = clusterServicesRegistry;
        this.nodeStateManager = nodeStateManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void start()
    {
        eventPublisher.register(this);
    }

    /**
     * @return node id - null if the server is not in a cluster
     */
    @Nullable
    @Override
    public String getNodeId()
    {
        return nodeStateManager.getNode().getNodeId();
    }

    /**
     * If there is a cluster.properties that appears to be valid
     *
     * @return true if clustered
     */
    @Override
    public boolean isClustered()
    {
        return nodeStateManager.getNode().isClustered();
    }

    @Override
    public Set<Node> getAllNodes()
    {
        return isClustered() ? nodeStateManager.getAllNodes() : ImmutableSet.<Node>of();
    }

    /**
     * Returns {@code true} if this node is active.
     *
     * @return {@code true} if this node is active.
     */
    @Override
    public boolean isActive()
    {
        return nodeStateManager.getNode().getState().equals(ACTIVE);
    }

    /**
     * Forces an index check to see if it is current (or can be rebuilt from current) - if the index is too out of date
     * request an index replica, only do this on passive nodes for now
     */
    @Override
    public void checkIndex()
    {
        final NodeReindexService nodeReindexService = clusterServicesRegistry.getNodeReindexService();
        if (!nodeReindexService.canIndexBeRebuilt())
        {
            requestCurrentIndexFromNode(ANY_NODE);
        }
    }

    @Override
    public void requestCurrentIndexFromNode(final String node)
    {
        final NodeReindexService nodeReindexService = clusterServicesRegistry.getNodeReindexService();
        nodeReindexService.pause();
        nodeReindexService.resetIndexCount();
        clusterServicesRegistry.getMessageHandlerService().sendMessage(node, new Message(BACKUP_INDEX, null));
    }

    @Override
    public Collection<Node> findLiveNodes()
    {
        if (liveNodes == null)
        {
            refreshLiveNodes();
        }

        // Already an immutable collection, so safe to return as-is
        return liveNodes;
    }

    @Override
    public void refreshLiveNodes()
    {
        final Collection<String> heartbeatLiveNodesIds = heartbeatServiceRef.get().findLiveNodes();
        Collection<Node> filter = CollectionUtil.filter(getAllNodes(), new Predicate<Node>()
        {
            @Override
            public boolean evaluate(final Node node)
            {
                return node != null && node.getState() == ACTIVE && heartbeatLiveNodesIds.contains(node.getNodeId());
            }
        });

        liveNodes = ImmutableSet.<Node>copyOf(filter);
    }

    @EventListener
    public void releaseNodeReindexService(IndexesRestoredEvent ev)
    {
        final NodeReindexService nodeReindexService = clusterServicesRegistry.getNodeReindexService();
        nodeReindexService.start();
        nodeReindexService.replayLocalOperations();
    }

    public boolean isClusterLicensed()
    {
        final JiraLicenseManager licenseManager = ComponentAccessor.getComponent(JiraLicenseManager.class);
        return licenseManager.getLicense().isDataCenter();
    }
}
