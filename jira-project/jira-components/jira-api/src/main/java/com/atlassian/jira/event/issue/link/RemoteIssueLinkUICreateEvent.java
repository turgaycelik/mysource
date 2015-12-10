package com.atlassian.jira.event.issue.link;

import com.atlassian.jira.issue.link.RemoteIssueLink;

/**
 * Fired when remote issue link has been created through the UI.
 *
 * @since v5.0
 */
public class RemoteIssueLinkUICreateEvent extends AbstractRemoteIssueLinkEvent
{
    private final String applicationType;

    /**
     * @param remoteLinkId
     * @param applicationType
     * @deprecated since 6.1.1. Use {@link #RemoteIssueLinkUICreateEvent(com.atlassian.jira.issue.link.RemoteIssueLink)} instead.
     */
    public RemoteIssueLinkUICreateEvent(Long remoteLinkId, String applicationType)
    {
        super(remoteLinkId);
        this.applicationType = applicationType;
    }

    public RemoteIssueLinkUICreateEvent(RemoteIssueLink remoteIssueLink)
    {
        super(remoteIssueLink);
        this.applicationType = remoteIssueLink.getApplicationType();
    }

    /**
     * Returns the type of application that was linked to.
     * <p>
     *    For example: "com.atlassian.confluence" or "com.mycompany.myproduct"
     * </p>
     *
     * @return the type of application that was linked to.
     *
     * @see com.atlassian.jira.issue.link.RemoteIssueLink#getApplicationType()
     */
    public String getApplicationType()
    {
        return applicationType;
    }
}
