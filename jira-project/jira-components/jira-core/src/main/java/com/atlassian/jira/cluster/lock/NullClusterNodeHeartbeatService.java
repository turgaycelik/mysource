package com.atlassian.jira.cluster.lock;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.beehive.db.ClusterNodeHeartbeatService;

import com.google.common.collect.ImmutableSet;

/**
 * Empty service for non-clustered environments
 */
public class NullClusterNodeHeartbeatService implements ClusterNodeHeartbeatService
{
    @Nonnull
    @Override
    public String getNodeId()
    {
        return "not-clustered";
    }

    @Override
    public boolean isNodeLive(@Nonnull final String nodeId)
    {
        return false;
    }

    @Nullable
    @Override
    public Long getLastHeartbeatTime(@Nonnull final String nodeId)
    {
        return null;
    }

    @Nonnull
    @Override
    public Collection<String> findLiveNodes()
    {
        return ImmutableSet.<String>of();
    }

    @Nonnull
    @Override
    public Collection<String> findLiveNodes(final long threshold)
    {
        return ImmutableSet.<String>of();
    }
}
