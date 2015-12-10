package com.atlassian.jira.cluster.lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.db.ClusterNodeHeartbeatService;
import com.atlassian.beehive.db.DatabaseClusterLockService;
import com.atlassian.beehive.db.spi.ClusterLockDao;
import com.atlassian.util.concurrent.LazyReference;

/**
 * A DatabaseClusterLockService that ensures the getLockForName() method gets called once and only once,
 * prior to any locks being given out.
 *
 * @since 6.3
 */
public class StartableDatabaseClusterLockService extends DatabaseClusterLockService
{
    private final LazyReference<Void> lockCleaned = new LazyReference<Void>()
    {
        @Nullable
        @Override
        protected Void create() throws Exception
        {
            cleanUpOrphanLocks();
            return null;
        }
    };

    public StartableDatabaseClusterLockService(
            final ClusterLockDao clusterLockDao, final ClusterNodeHeartbeatService clusterNodeHeartbeatService)
    {
        super(clusterLockDao, clusterNodeHeartbeatService);
    }

    @Override
    public ClusterLock getLockForName(@Nonnull final String lockName)
    {
        lockCleaned.get();
        return super.getLockForName(lockName);
    }
}
