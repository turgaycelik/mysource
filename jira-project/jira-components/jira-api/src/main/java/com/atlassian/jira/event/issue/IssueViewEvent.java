package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.AbstractEvent;

/**
 * Event that is triggered when an issue is viewed.
 */
@PublicApi
public final class IssueViewEvent extends AbstractEvent
{
    private long issueId;

    public IssueViewEvent(long issueId)
    {
        this.issueId = issueId;
    }

    /**
     * @return the issue ID
     */
    public long getId()
    {
        return issueId;
    }
}
