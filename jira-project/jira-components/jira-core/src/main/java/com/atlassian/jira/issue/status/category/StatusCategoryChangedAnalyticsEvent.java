package com.atlassian.jira.issue.status.category;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Analytics event to indicate that a StatusCategory of a Status has changed
 */
@EventName ("administration.issuestatuses.issuestatus.category.changed")
public class StatusCategoryChangedAnalyticsEvent
{
    private final String oldCategoryKey;
    private final String newCategoryKey;
    private final String oldStatusName;
    private final String newStatusName;

    public StatusCategoryChangedAnalyticsEvent(String oldCategoryKey, String newCategoryKey, String oldStatusName, String newStatusName)
    {
        this.oldCategoryKey = oldCategoryKey;
        this.newCategoryKey = newCategoryKey;
        this.oldStatusName = oldStatusName;
        this.newStatusName = newStatusName;
    }

    public String getOldCategoryKey()
    {
        return oldCategoryKey;
    }

    public String getNewCategoryKey()
    {
        return newCategoryKey;
    }

    public String getOldStatusName()
    {
        return oldStatusName;
    }

    public String getNewStatusName()
    {
        return newStatusName;
    }
}
