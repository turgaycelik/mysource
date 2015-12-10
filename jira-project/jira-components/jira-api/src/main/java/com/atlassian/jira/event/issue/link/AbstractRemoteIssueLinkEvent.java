package com.atlassian.jira.event.issue.link;

import com.atlassian.jira.issue.link.RemoteIssueLink;

/**
 * Abstract base class for remote issue linking related events.
 *
 * @since v5.0
 */
public class AbstractRemoteIssueLinkEvent
{
    private final Long remoteIssueLinkId;
    private final String globalId;

    /**
     * @param remoteIssueLinkId
     * @deprecated since 6.1.1. Use {@link #AbstractRemoteIssueLinkEvent(com.atlassian.jira.issue.link.RemoteIssueLink)} instead.
     */
    public AbstractRemoteIssueLinkEvent(Long remoteIssueLinkId) {
        this.remoteIssueLinkId = remoteIssueLinkId;
        this.globalId = "";
    }

    public AbstractRemoteIssueLinkEvent(RemoteIssueLink remoteIssueLink)
    {
        this.remoteIssueLinkId = remoteIssueLink.getId();
        this.globalId = remoteIssueLink.getGlobalId();
    }

    public Long getRemoteIssueLinkId()
    {
        return remoteIssueLinkId;
    }

    public String getGlobalId()
    {
        return globalId;
    }
}
