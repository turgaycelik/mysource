package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.ClusterLockStatusEntity;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import org.apache.log4j.Logger;

/**
 * @since v6.3
 */
public class UpgradeTask_Build6326 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6326.class);

    public UpgradeTask_Build6326()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6326";
    }

    @Override
    public String getShortDescription()
    {
        return "Default value for the new upgrade_time field of database cluster lock.";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        if (setupMode)
        {
            // nothing to do - the table will be empty
            return;
        }

        int rows = Update.into(Entity.CLUSTER_LOCK_STATUS)
                .set(ClusterLockStatusEntity.UPDATE_TIME, new Long(0))
                .whereEqual(ClusterLockStatusEntity.UPDATE_TIME, (Long) null)
                .execute(getEntityEngine());
        log.info("Updated " + rows + " rows in " + Entity.CLUSTER_LOCK_STATUS.getEntityName());
    }
}
