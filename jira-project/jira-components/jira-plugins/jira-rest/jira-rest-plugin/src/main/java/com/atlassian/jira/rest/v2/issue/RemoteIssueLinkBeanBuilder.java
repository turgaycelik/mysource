package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;

import java.net.URI;

/**
 * Builder for {@link RemoteIssueLinkBean} instances.
 *
 * @since v5.0
 */
public class RemoteIssueLinkBeanBuilder
{
    private final ContextUriInfo contextUriInfo;
    private final IssueManager issueManager;

    private final RemoteIssueLink remoteIssueLink;

    public RemoteIssueLinkBeanBuilder(final ContextUriInfo contextUriInfo, final IssueManager issueManager, final RemoteIssueLink remoteIssueLink)
    {
        this.contextUriInfo = contextUriInfo;
        this.issueManager = issueManager;
        this.remoteIssueLink = remoteIssueLink;
    }

    public RemoteIssueLinkBean build()
    {
        final URI self = createSelfLink(remoteIssueLink);

        return new RemoteIssueLinkBean(
                remoteIssueLink.getId(),
                self,
                remoteIssueLink.getGlobalId(),
                remoteIssueLink.getApplicationType(),
                remoteIssueLink.getApplicationName(),
                remoteIssueLink.getRelationship(),
                remoteIssueLink.getUrl(),
                remoteIssueLink.getTitle(),
                remoteIssueLink.getSummary(),
                remoteIssueLink.getIconUrl(),
                remoteIssueLink.getIconTitle(),
                remoteIssueLink.isResolved(),
                remoteIssueLink.getStatusIconUrl(),
                remoteIssueLink.getStatusIconTitle(),
                remoteIssueLink.getStatusIconLink()
        );
    }

    private URI createSelfLink(final RemoteIssueLink remoteIssueLink)
    {
        final Issue issue = issueManager.getIssueObject(remoteIssueLink.getIssueId());
        return createSelfLink(remoteIssueLink, issue, contextUriInfo);
    }

    static URI createSelfLink(final RemoteIssueLink remoteIssueLink, final Issue issue, final ContextUriInfo contextUriInfo)
    {
        return contextUriInfo.getBaseUriBuilder()
                .path(IssueResource.class)
                .path(issue.getKey())
                .path("remotelink")
                .path(remoteIssueLink.getId().toString())
                .build();
    }
}
