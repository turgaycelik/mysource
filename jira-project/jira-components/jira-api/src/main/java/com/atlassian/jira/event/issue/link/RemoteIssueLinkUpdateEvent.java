package com.atlassian.jira.event.issue.link;

import com.atlassian.jira.issue.link.RemoteIssueLink;

/**
 * Fired when remote issue link has been updated.
 *
 * @since v5.0
 */
public class RemoteIssueLinkUpdateEvent extends AbstractRemoteIssueLinkEvent
{
    private final String applicationType;

    /**
     * @param remoteLinkId
     * @param applicationType
     * @deprecated since 6.1.1. Use {@link #RemoteIssueLinkUpdateEvent(com.atlassian.jira.issue.link.RemoteIssueLink)} instead.
     */
    public RemoteIssueLinkUpdateEvent(Long remoteLinkId, String applicationType)
    {
        super(remoteLinkId);
        this.applicationType = applicationType;
    }

    public RemoteIssueLinkUpdateEvent(RemoteIssueLink remoteIssueLink)
    {
        super(remoteIssueLink);
        this.applicationType = remoteIssueLink.getApplicationType();
    }

    public String getApplicationType()
    {
        return applicationType;
    }
}
