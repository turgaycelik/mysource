package com.atlassian.jira.cluster.lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.beehive.db.spi.ClusterLockDao;
import com.atlassian.beehive.db.spi.ClusterLockStatus;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.exception.DataAccessException;

import static com.atlassian.jira.entity.ClusterLockStatusEntity.LOCKED_BY_NODE;
import static com.atlassian.jira.entity.ClusterLockStatusEntity.LOCK_NAME;
import static com.atlassian.jira.entity.ClusterLockStatusEntity.UPDATE_TIME;
import static com.atlassian.jira.entity.Delete.from;
import static com.atlassian.jira.entity.Entity.CLUSTER_LOCK_STATUS;

/**
 * JIRA implementation of the ClusterLockDao from the beehive clustering library.
 *
 * @since 6.3
 */
public class JiraClusterLockDao implements ClusterLockDao
{
    private final EntityEngine entityEngine;

    public JiraClusterLockDao(final EntityEngine entityEngine)
    {
        this.entityEngine = entityEngine;
    }

    @Nullable
    @Override
    public ClusterLockStatus getClusterLockStatusByName(@Nonnull final String lockName)
    {
        return entityEngine.selectFrom(CLUSTER_LOCK_STATUS)
                .whereEqual(LOCK_NAME, lockName)
                .singleValue();
    }

    @Override
    public boolean tryUpdateAcquireLock(@Nonnull final String lockName, @Nonnull final String nodeId, final long updateTime)
    {
        //    UPDATE CLUSTER_LOCK
        //      SET LOCKED_BY_NODE = ?
        //      WHERE LOCK_NAME = ?
        //      AND LOCKED_BY_NODE IS NULL
        final int rows = Update.into(CLUSTER_LOCK_STATUS)
                .set(LOCKED_BY_NODE, nodeId)
                .set(UPDATE_TIME, updateTime)
                .whereEqual(LOCK_NAME, lockName)
                .andEqual(LOCKED_BY_NODE, (String) null)
                .execute(entityEngine);
        if (rows == 0)
            return false;
        if (rows == 1)
            return true;
        throw new IllegalStateException("Too many rows updated in JiraClusterLockDao: " + rows + " for lock name: " + lockName);
    }

    @Override
    public void insertEmptyClusterLock(@Nonnull final String lockName, final long updateTime)
    {
        try
        {
            entityEngine.createValue(CLUSTER_LOCK_STATUS, new ClusterLockStatus(lockName, null, updateTime));
        }
        catch (DataAccessException ex)
        {
            // OfBiz does not help us to distinguish if this DataAccessException is a unique constraint violation.
            // Its simpler and safer just to check the DB if the row exists
            if (getClusterLockStatusByName(lockName) == null)
            {
                // nope - I guess it was a real error
                throw ex;
            }
        }
    }

    @Override
    public void unlock(@Nonnull final String lockName, @Nonnull final String nodeId, final long updateTime)
    {
        //    UPDATE CLUSTER_LOCK
        //      SET LOCKED_BY_NODE = NULL
        //      WHERE LOCK_NAME = ?
        //      AND LOCKED_BY_NODE IS ?
        final int rowsUpdated = Update.into(CLUSTER_LOCK_STATUS)
                .set(LOCKED_BY_NODE, (String) null)
                .set(UPDATE_TIME, updateTime)
                .whereEqual(LOCK_NAME, lockName)
                .andEqual(LOCKED_BY_NODE, nodeId)
                .execute(entityEngine);
        if (rowsUpdated == 0)
        {
            // this indicates some bad ju-ju
            throw new IllegalMonitorStateException("Attempt to unlock '" + lockName +
                    "' but it was not held by this node ('" + nodeId + "').");
        }
    }

    @Override
    public void deleteLocksHeldByNode(final String nodeId)
    {
        entityEngine.delete(from(CLUSTER_LOCK_STATUS).whereEqual(LOCKED_BY_NODE, nodeId));
    }
}
