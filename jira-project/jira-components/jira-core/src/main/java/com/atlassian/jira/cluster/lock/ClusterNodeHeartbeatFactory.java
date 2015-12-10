package com.atlassian.jira.cluster.lock;

import java.util.Map;

import com.atlassian.jira.entity.AbstractEntityFactory;
import com.atlassian.jira.ofbiz.FieldMap;

import org.ofbiz.core.entity.GenericValue;

public class ClusterNodeHeartbeatFactory extends AbstractEntityFactory<ClusterNodeHeartbeat>
{
    @Override
    public Map<String, Object> fieldMapFrom(final ClusterNodeHeartbeat value)
    {
        return FieldMap.build(ClusterNodeHeartbeat.NODE_ID, value.getNodeId())
                .add(ClusterNodeHeartbeat.HEARTBEAT_TIME, value.getHeartbeatTime())
                .add(ClusterNodeHeartbeat.DATABASE_TIME, value.getDatabaseTime());
    }

    @Override
    public String getEntityName()
    {
        return "ClusterNodeHeartbeat";
    }

    @Override
    public ClusterNodeHeartbeat build(final GenericValue gv)
    {
        return new ClusterNodeHeartbeat(gv.getString(ClusterNodeHeartbeat.NODE_ID), gv.getLong(ClusterNodeHeartbeat.HEARTBEAT_TIME), gv.getLong(ClusterNodeHeartbeat.DATABASE_TIME));
    }
}
