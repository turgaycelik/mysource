package com.atlassian.jira.cluster.lock;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.database.DatabaseSystemTimeReader;
import com.atlassian.jira.database.DatabaseSystemTimeReaderFactory;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.Update;

import org.ofbiz.core.entity.EntityOperator;

/**
 * JIRA implementation of ClusterNodeHeartBeatDao - used for cluster locks.
 *
 * @since 6.3
 */
public class JiraClusterNodeHeartBeatDao implements TimedClusterNodeHeartBeatDao
{
    private final String localNodeId;
    private final EntityEngine entityEngine;
    private final DatabaseSystemTimeReader dbTimeReader;
    private final ClusterManager clusterManager;

    public JiraClusterNodeHeartBeatDao(ClusterManager clusterManager, EntityEngine entityEngine, DatabaseSystemTimeReaderFactory dbTimeReaderFactory)
    {
        this.entityEngine = entityEngine;
        this.clusterManager = clusterManager;
        this.dbTimeReader = dbTimeReaderFactory.getReader();
        if (clusterManager.isClustered())
        {
            // Use the configured Cluster Node ID
            localNodeId = clusterManager.getNodeId();
        }
        else
        {
            // There is no cluster, and therefore no node ID - we set up a fake one for use by Beehive Cluster locks.
            localNodeId = "SINGLE_NODE";
        }
    }

    @Nonnull
    @Override
    public String getNodeId()
    {
        return localNodeId;
    }

    @Override
    public void writeHeartBeat(final long time)
    {
        long databaseTime;
        try
        {
            databaseTime = dbTimeReader.getDatabaseSystemTimeMillis();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        // Assume we have already done a heartbeat, and try to update
        int rows = Update.into(Entity.CLUSTER_NODE_HEARTBEAT)
                .set(ClusterNodeHeartbeat.HEARTBEAT_TIME, time)
                .set(ClusterNodeHeartbeat.DATABASE_TIME, databaseTime)
                .whereEqual(ClusterNodeHeartbeat.NODE_ID, localNodeId)
                .execute(entityEngine);

        // Check we actually updated a row:
        if (rows == 0)
        {
            // nothing updated - need to write the initial heartbeat
            entityEngine.createValueWithoutId(Entity.CLUSTER_NODE_HEARTBEAT, new ClusterNodeHeartbeat(localNodeId, time, databaseTime));
        }

        clusterManager.refreshLiveNodes();
    }

    @Nullable
    @Override
    public Long getLastHeartbeatTime(@Nonnull final String nodeId)
    {
        ClusterNodeHeartbeat heartbeat = Select.from(Entity.CLUSTER_NODE_HEARTBEAT)
                .whereEqual(ClusterNodeHeartbeat.NODE_ID, nodeId)
                .runWith(entityEngine)
                .singleValue();
        if (heartbeat == null)
        {
            return null;
        }
        else
        {
            return heartbeat.getHeartbeatTime();
        }
    }

    @Nonnull
    @Override
    public Map<String, Long> getActiveNodesDatabaseTimeOffsets(long databaseActiveTime)
    {
        List<ClusterNodeHeartbeat> results =
            Select.from(Entity.CLUSTER_NODE_HEARTBEAT)
               .where(ClusterNodeHeartbeat.DATABASE_TIME, EntityOperator.GREATER_THAN, databaseActiveTime)
               .runWith(entityEngine)
               .asList();

        Map<String, Long> offsetMap = new LinkedHashMap<String, Long>();

        for (ClusterNodeHeartbeat result : results)
        {
            //Possible window between database migration and first heartbeat entry that no database
            //time has been filled in, or may be just old entries from ages ago - need to allow for null
            Long timeOffset;
            if (result.getDatabaseTime() == null)
            {
                timeOffset = null;
            }
            else
            {
                timeOffset = result.getHeartbeatTime() - result.getDatabaseTime();
            }
            offsetMap.put(result.getNodeId(), timeOffset);
        }

        return offsetMap;
    }

    @Nonnull
    @Override
    public Collection<String> findNodesWithHeartbeatsAfter(final long time)
    {
        return Select.stringColumn(ClusterNodeHeartbeat.NODE_ID)
                .from(Entity.CLUSTER_NODE_HEARTBEAT)
                .where(ClusterNodeHeartbeat.HEARTBEAT_TIME, EntityOperator.GREATER_THAN, time)
                .runWith(entityEngine)
                .asList();
    }
}
