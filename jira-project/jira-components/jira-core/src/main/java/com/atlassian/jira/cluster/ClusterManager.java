package com.atlassian.jira.cluster;

import java.util.Collection;
import java.util.Set;

/**
 * Manages the cluster - addition of nodes, removal, etc
 *
 * @since v6.1
 */
public interface ClusterManager
{
    String ALL_NODES = "ALL";

    String ANY_NODE = "ANY";

    void checkIndex();

    String getNodeId();

    boolean isActive();

    boolean isClustered();

    /**
     * Returns all the nodes in the cluster.
     * If not clustered this will return an empty set.
     *
     * @return a collection of {@link com.atlassian.jira.cluster.Node}s in a cluster
     */
    Set<Node> getAllNodes();

    /**
     * Returns whether or not JIRA is licensed for clustered configurations.
     *
     * @return whether or not JIRA is licensed for clustered configurations.
     * @since 6.3
     */
    boolean isClusterLicensed();

    /**
     * We send a message to the node we wish a copy from.
     * @param node Node to send the message to get an Index Copy from.
     */
    void requestCurrentIndexFromNode(String node);

    /**
     * Returns a snapshot of the live nodes.
     * This collection is refreshed in every heartbeat of the server.
     * Or you can force it calling refreshLiveNodes()
     *
     * @return the list of nodes that are alive.
     */
    Collection<Node> findLiveNodes();

    /**
     * Merges the information from the heartbeat table and the clusternode table
     * to give accurate information of which nodes are alive.
     * @since 6.3.4
     */
    void refreshLiveNodes();
}
