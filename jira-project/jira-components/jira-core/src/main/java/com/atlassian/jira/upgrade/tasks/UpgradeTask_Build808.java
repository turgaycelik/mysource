package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.startup.NotificationInstanceKiller;
import com.atlassian.jira.upgrade.UpgradeTask;

import java.util.Collection;
import java.util.Collections;

/**
 * Sets a date-time property that the instance was upgraded so we can know when to delete the NotificationInstance table.
 *
 * @since v5.2
 */
public class UpgradeTask_Build808 implements UpgradeTask
{
    @Override
    public String getBuildNumber()
    {
        return "808";
    }

    @Override
    public String getShortDescription()
    {
        return "Setting up a count down until we delete the NotificationInstance table";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        ComponentAccessor.getApplicationProperties().setString(NotificationInstanceKiller.NOTIFICATION_INSTANCE_UPGRADE_DATE, String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public Collection<String> getErrors()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isReindexRequired()
    {
        return false;
    }
}
