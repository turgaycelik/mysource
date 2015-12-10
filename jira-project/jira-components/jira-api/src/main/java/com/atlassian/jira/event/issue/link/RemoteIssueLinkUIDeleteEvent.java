package com.atlassian.jira.event.issue.link;

import com.atlassian.jira.issue.link.RemoteIssueLink;

/**
 * Fired when remote issue link has been deleted through the UI.
 *
 * @since v5.0
 */
public class RemoteIssueLinkUIDeleteEvent extends AbstractRemoteIssueLinkEvent
{
    /**
     * @param remoteLinkId
     * @deprecated since 6.1.1. Use {@link #RemoteIssueLinkUIDeleteEvent(com.atlassian.jira.issue.link.RemoteIssueLink)} instead.
     */
    public RemoteIssueLinkUIDeleteEvent(Long remoteLinkId)
    {
        super(remoteLinkId);
    }

    public RemoteIssueLinkUIDeleteEvent(RemoteIssueLink remoteIssueLink)
    {
        super(remoteIssueLink);
    }
}
