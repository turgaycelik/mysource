package com.atlassian.jira.cluster.lock;

import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.beehive.db.spi.ClusterNodeHeartBeatDao;


/**
 * Cluster node heartbeat DAO that adds operations to retrieve database timing information.
 *
 * @since 6.3
 */
public interface TimedClusterNodeHeartBeatDao extends ClusterNodeHeartBeatDao
{
    /**
     * Returns each active cluster node's clock offset from the database clock.
     * <p>
     *
     * The returned map maps node IDs to their clock offset times in milliseconds.  A clock offset time is the
     * number of milliseconds difference between that node's system clock and the clock of the database.  These
     * values can be used to determine how synchronized the node's clocks are.
     * <p>
     *
     * It is possible that a node's time offset value is returned as <code>null</code> if they have not registered
     * a heartbeat yet after they have started up or if they are upgrading their version and have not written a
     * database time value in the database yet.
     *
     * @param databaseActiveTime the minimum database time a node should have in the system to regard it as active.
     *          Nodes that registered their heartbeat with an associated database time less than this value will not
     *          be returned.
     *
     * @return a map mapping node IDs to offset times in milliseconds.
     */
    @Nonnull
    Map<String, Long> getActiveNodesDatabaseTimeOffsets(long databaseActiveTime);
}
