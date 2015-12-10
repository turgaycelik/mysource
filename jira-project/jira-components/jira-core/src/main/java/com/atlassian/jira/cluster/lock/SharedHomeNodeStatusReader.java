package com.atlassian.jira.cluster.lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Reads the shared home status of a node. Shared home status indicates whether a node is actively using a shared
 * home directory.
 *
 * @since 6.3.4
 */
@ExperimentalApi
public interface SharedHomeNodeStatusReader
{
    /**
     * Reads the status for a node.
     *
     * @param nodeId the node ID.
     *
     * @return the status for the specified node, or <code>null</code> if the node does not exist or is not using
     *          the same shared home as the current node.
     */
    @Nullable
    NodeSharedHomeStatus readNodeStatus(@Nonnull String nodeId);
}
