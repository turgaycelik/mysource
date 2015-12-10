package com.atlassian.jira.cluster.lock;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Indicator of how a node is using a shared home directory.
 *
 * @since 6.3.4
 */
@ExperimentalApi
public class NodeSharedHomeStatus
{
    private final String nodeId;
    private final long updateTime;

    public NodeSharedHomeStatus(@Nonnull String nodeId, long updateTime)
    {
        this.nodeId = nodeId;
        this.updateTime = updateTime;
    }

    @Nonnull
    public String getNodeId()
    {
        return nodeId;
    }

    /**
     * @return the time in milliseconds since the last update.
     */
    public long getUpdateTime()
    {
        return updateTime;
    }

    @Override
    public String toString()
    {
        return getNodeId() + ": " + getUpdateTime();
    }
}
