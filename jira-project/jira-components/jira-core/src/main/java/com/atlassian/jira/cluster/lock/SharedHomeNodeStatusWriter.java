package com.atlassian.jira.cluster.lock;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Saves the shared home node status of a node.
 *
 * @since 6.3.4
 *
 * @see com.atlassian.jira.cluster.lock.SharedHomeNodeStatusReader
 */
@ExperimentalApi
public interface SharedHomeNodeStatusWriter
{
    void writeNodeStatus(@Nonnull NodeSharedHomeStatus status);
    void removeNodeStatus(@Nonnull String nodeId);
}
