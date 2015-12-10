package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.AbstractEvent;

/**
 * Event that is triggered when a user does a quicksearch that is interpreted as an issue key
 */
@PublicApi
public final class QuickBrowseEvent extends AbstractEvent
{
    private final String issueKey;

    public QuickBrowseEvent(String issueKey)
    {
        this.issueKey = issueKey;
    }

    public String getIssueKey()
    {
        return issueKey;
    }
}
