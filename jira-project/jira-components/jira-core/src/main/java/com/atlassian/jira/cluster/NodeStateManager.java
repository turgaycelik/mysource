package com.atlassian.jira.cluster;

import java.util.Set;

/**
 * Manage the state of nodes in the HA Cluster.
 *
 * @since v6.1
 */
public interface NodeStateManager
{
    /**
     * Returns the current JIRA node.
     *
     * @return a non-null instance; call {@link com.atlassian.jira.cluster.Node#isClustered()} to see if it's part of a
     * cluster
     */
    Node getNode();

    /**
     * Returns the current JIRA node, first refreshing its state from the database.
     *
     * @return a non-null instance; call {@link com.atlassian.jira.cluster.Node#isClustered()} to see if it's part of a
     * cluster
     */
    Node getNodeWithRefresh();

    /**
     * Returns all the known nodes JIRA node. If not clustered this will return an empty set.
     *
     * @return a collection of {@link com.atlassian.jira.cluster.Node}s in a cluster
     */
    Set<Node> getAllNodes();

    /**
     * Activate the node. This method will block until the node becomes active.
     *
     * @throws ClusterStateException when we can't become active as requested
     */
    void activate() throws ClusterStateException;

    /**
     * Deactivate the node. This method will block until the node becomes inactive (passive).
     *
     * @throws NotClusteredException if the instance is not clustered
     */
    void deactivate() throws NotClusteredException;

    void quiesce() throws NotClusteredException;

    void restart();

    /**
     * Shutdowns the node in the db. The state of the node should be OFFLINE This will prevent the health checks to show
     * abnormal behaviour in the cluster
     */
    void shutdownNode();
}
