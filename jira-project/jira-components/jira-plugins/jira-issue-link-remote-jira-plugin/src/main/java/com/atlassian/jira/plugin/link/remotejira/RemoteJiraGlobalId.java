package com.atlassian.jira.plugin.link.remotejira;

import com.atlassian.applinks.api.ApplicationLink;
import com.google.common.base.Objects;

/**
 * Represents the globalId for remote issue links between JIRA instances.
 *
 * @since v5.0
 */
public class RemoteJiraGlobalId
{
    private final ApplicationLink applicationLink;
    private final Long remoteIssueId;

    public RemoteJiraGlobalId(final ApplicationLink applicationLink, final Long remoteIssueId)
    {
        this.applicationLink = applicationLink;
        this.remoteIssueId = remoteIssueId;
    }

    public ApplicationLink getApplicationLink()
    {
        return applicationLink;
    }

    public Long getRemoteIssueId()
    {
        return remoteIssueId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(applicationLink, remoteIssueId);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof RemoteJiraGlobalId)
        {
            final RemoteJiraGlobalId other = (RemoteJiraGlobalId) obj;
            return Objects.equal(applicationLink, other.applicationLink)
                && Objects.equal(remoteIssueId, other.remoteIssueId);
        }
        else
        {
            return false;
        }
    }
}
