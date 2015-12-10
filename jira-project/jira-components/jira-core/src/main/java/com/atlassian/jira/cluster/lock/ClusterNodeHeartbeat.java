package com.atlassian.jira.cluster.lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a row in the cluster heartbeat DB table.
 *
 * @since 6.3
 */
public final class ClusterNodeHeartbeat
{
    public static final String NODE_ID = "nodeId";
    public static final String HEARTBEAT_TIME = "heartbeatTime";
    public static final String DATABASE_TIME = "databaseTime";

    private final String nodeId;
    private final long heartbeatTime;
    private final Long databaseTime;

    public ClusterNodeHeartbeat(@Nonnull final String nodeId, final long heartbeatTime, final @Nullable Long databaseTime)
    {
        this.nodeId = nodeId;
        this.heartbeatTime = heartbeatTime;
        this.databaseTime = databaseTime;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public long getHeartbeatTime()
    {
        return heartbeatTime;
    }

    @Nullable
    public Long getDatabaseTime()
    {
        return databaseTime;
    }
}
