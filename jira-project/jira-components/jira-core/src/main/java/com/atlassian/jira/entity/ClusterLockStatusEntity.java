package com.atlassian.jira.entity;

import java.util.Map;

import com.atlassian.beehive.db.spi.ClusterLockStatus;
import com.atlassian.jira.ofbiz.FieldMap;

import org.ofbiz.core.entity.GenericValue;

/**
 * EntityFactory for ClusterLockStatus table.
 * <p>
 *     Builds ClusterLockStatus objects from GenericValues and vice versa.
 * </p>
 *
 * @since 6.3
 */
public class ClusterLockStatusEntity extends AbstractEntityFactory<ClusterLockStatus>
{
    public static final String LOCK_NAME = "lockName";
    public static final String LOCKED_BY_NODE = "lockedByNode";
    public static final String UPDATE_TIME = "updateTime";

    @Override
    public String getEntityName()
    {
        return "ClusterLockStatus";
    }

    @Override
    public ClusterLockStatus build(final GenericValue gv)
    {
        // Originally there was no UPDATE_TIME column, so legacy data could contain null.
        // We will run an upgrade task to seed this value in order to avoid null, but lets be defensive here too in case there is a race.
        Long updateTime = gv.getLong(UPDATE_TIME);
        if (updateTime == null)
            updateTime = 0L;
        return new ClusterLockStatus(gv.getString(LOCK_NAME), gv.getString(LOCKED_BY_NODE), updateTime);
    }

    @Override
    public Map<String, Object> fieldMapFrom(final ClusterLockStatus clusterLockStatus)
    {
        return new FieldMap()
                .add(LOCK_NAME, clusterLockStatus.getLockName())
                .add(LOCKED_BY_NODE, clusterLockStatus.getLockedByNode())
                .add(UPDATE_TIME, clusterLockStatus.getUpdateTime());
    }
}
