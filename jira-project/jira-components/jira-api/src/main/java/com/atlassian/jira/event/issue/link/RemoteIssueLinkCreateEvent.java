package com.atlassian.jira.event.issue.link;

import com.atlassian.jira.issue.link.RemoteIssueLink;

/**
 * Fired when remote issue link has been created.
 *
 * @since v5.0
 */
public class RemoteIssueLinkCreateEvent extends AbstractRemoteIssueLinkEvent
{
    private final String applicationType;

    /**
     * @param remoteLinkId
     * @param applicationType
     * @deprecated since 6.1.1. Use {@link #RemoteIssueLinkCreateEvent(com.atlassian.jira.issue.link.RemoteIssueLink)} instead.
     */
    public RemoteIssueLinkCreateEvent(Long remoteLinkId, String applicationType)
    {
        super(remoteLinkId);
        this.applicationType = applicationType;
    }

    public RemoteIssueLinkCreateEvent(RemoteIssueLink remoteIssueLink)
    {
        super(remoteIssueLink);
        this.applicationType = remoteIssueLink.getApplicationType();
    }

    public String getApplicationType()
    {
        return applicationType;
    }
}
