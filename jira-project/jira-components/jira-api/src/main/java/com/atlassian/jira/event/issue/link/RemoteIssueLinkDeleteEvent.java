package com.atlassian.jira.event.issue.link;

import com.atlassian.jira.issue.link.RemoteIssueLink;

/**
 * Fired when remote issue link has been deleted.
 *
 * @since v5.0
 */
public class RemoteIssueLinkDeleteEvent extends AbstractRemoteIssueLinkEvent
{
    /**
     * @param remoteLinkId
     * @deprecated since 6.1.1. Use {@link #RemoteIssueLinkDeleteEvent(com.atlassian.jira.issue.link.RemoteIssueLink)}
     */
    public RemoteIssueLinkDeleteEvent(Long remoteLinkId)
    {
        super(remoteLinkId);
    }

    public RemoteIssueLinkDeleteEvent(RemoteIssueLink remoteIssueLink)
    {
        super(remoteIssueLink);
    }
}
