package com.atlassian.jira.cluster.lock;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created this class to avoid problems of dependencies in non-clustered environments
 */
public class NullJiraClusterNodeHeartBeatDao implements TimedClusterNodeHeartBeatDao
{
    @Nonnull
    @Override
    public String getNodeId()
    {
        return "non-clustered";
    }

    @Override
    public void writeHeartBeat(final long time)
    {

    }

    @Nullable
    @Override
    public Long getLastHeartbeatTime(@Nonnull final String nodeId)
    {
        return null;
    }

    @Nonnull
    @Override
    public Collection<String> findNodesWithHeartbeatsAfter(final long time)
    {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Map<String, Long> getActiveNodesDatabaseTimeOffsets(long databaseActiveTime)
    {
        //Entry for the single node with zero offset
        return Collections.singletonMap(getNodeId(), 0L);
    }
}
