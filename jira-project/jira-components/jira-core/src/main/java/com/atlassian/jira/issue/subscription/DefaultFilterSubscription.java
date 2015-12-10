package com.atlassian.jira.issue.subscription;

import java.util.Date;

import javax.annotation.Nullable;

/**
 * A Filter Subscription bean
 *
 * @since v6.2
 */
public class DefaultFilterSubscription implements FilterSubscription
{
    private boolean emailOnEmpty;
    private long id;
    private long filterId;
    private String userKey;
    private String groupName;
    private Date lastRunTime;

    public DefaultFilterSubscription(final long id, final long filterId, final String userKey, final String groupName, final Date lastRunTime, final boolean emailOnEmpty)
    {
        this.id = id;
        this.filterId = filterId;
        this.userKey = userKey;
        this.groupName = groupName;
        this.lastRunTime = lastRunTime;
        this.emailOnEmpty = emailOnEmpty;
    }

    @Override
    public long getId()
    {
        return id;
    }

    @Override
    public long getFilterId()
    {
        return filterId;
    }

    @Override
    public String getUserKey()
    {
        return userKey;
    }

    @Nullable
    @Override
    public String getGroupName()
    {
        return groupName;
    }

    @Nullable
    @Override
    public Date getLastRunTime()
    {
        return lastRunTime;
    }

    @Override
    public boolean isEmailOnEmpty()
    {
        return emailOnEmpty;
    }
}
