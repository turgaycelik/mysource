package com.atlassian.jira.issue.link;

import com.atlassian.annotations.PublicApi;

/**
 * Builder for {@link RemoteIssueLink remote issue links}.
 *
 * @since v5.0
 */
@PublicApi
public class RemoteIssueLinkBuilder
{
    private Long id;
    private Long issueId;
    private String globalId;
    private String title;
    private String summary;
    private String url;
    private String iconUrl;
    private String iconTitle;
    private String relationship;
    private Boolean resolved;
    private String statusName;
    private String statusDescription;
    private String statusIconUrl;
    private String statusIconTitle;
    private String statusIconLink;
    private String statusCategoryKey;
    private String statusCategoryColorName;
    private String applicationType;
    private String applicationName;

    public RemoteIssueLinkBuilder()
    {

    }

    public RemoteIssueLinkBuilder(final RemoteIssueLink remoteIssueLink)
    {
        id(remoteIssueLink.getId());
        issueId(remoteIssueLink.getIssueId());
        globalId(remoteIssueLink.getGlobalId());
        title(remoteIssueLink.getTitle());
        summary(remoteIssueLink.getSummary());
        url(remoteIssueLink.getUrl());
        iconUrl(remoteIssueLink.getIconUrl());
        iconTitle(remoteIssueLink.getIconTitle());
        relationship(remoteIssueLink.getRelationship());
        resolved(remoteIssueLink.isResolved());
        statusName(remoteIssueLink.getStatusName());
        statusDescription(remoteIssueLink.getStatusDescription());
        statusIconUrl(remoteIssueLink.getStatusIconUrl());
        statusIconTitle(remoteIssueLink.getStatusIconTitle());
        statusIconLink(remoteIssueLink.getStatusIconLink());
        statusCategoryKey(remoteIssueLink.getStatusCategoryKey());
        statusCategoryColorName(remoteIssueLink.getStatusCategoryColorName());
        applicationType(remoteIssueLink.getApplicationType());
        applicationName(remoteIssueLink.getApplicationName());
    }

    public RemoteIssueLinkBuilder id(final Long id)
    {
        this.id = id;
        return this;
    }

    public RemoteIssueLinkBuilder issueId(final Long issueId)
    {
        this.issueId = issueId;
        return this;
    }

    public RemoteIssueLinkBuilder globalId(final String globalId)
    {
        this.globalId = globalId;
        return this;
    }

    public RemoteIssueLinkBuilder title(final String title)
    {
        this.title = title;
        return this;
    }

    public RemoteIssueLinkBuilder summary(final String summary)
    {
        this.summary = summary;
        return this;
    }

    public RemoteIssueLinkBuilder url(final String url)
    {
        this.url = url;
        return this;
    }

    public RemoteIssueLinkBuilder iconUrl(final String iconUrl)
    {
        this.iconUrl = iconUrl;
        return this;
    }

    public RemoteIssueLinkBuilder iconTitle(final String iconTitle)
    {
        this.iconTitle = iconTitle;
        return this;
    }

    public RemoteIssueLinkBuilder relationship(final String relationship)
    {
        this.relationship = relationship;
        return this;
    }

    public RemoteIssueLinkBuilder resolved(final Boolean resolved)
    {
        this.resolved = resolved;
        return this;
    }

    public RemoteIssueLinkBuilder statusName(final String statusName)
    {
        this.statusName = statusName;
        return this;
    }

    public RemoteIssueLinkBuilder statusDescription(final String statusDescription)
    {
        this.statusDescription = statusDescription;
        return this;
    }

    public RemoteIssueLinkBuilder statusIconUrl(final String statusIconUrl)
    {
        this.statusIconUrl = statusIconUrl;
        return this;
    }

    public RemoteIssueLinkBuilder statusIconTitle(final String statusIconTitle)
    {
        this.statusIconTitle = statusIconTitle;
        return this;
    }

    public RemoteIssueLinkBuilder statusIconLink(final String statusIconLink)
    {
        this.statusIconLink = statusIconLink;
        return this;
    }

    public RemoteIssueLinkBuilder statusCategoryKey(final String key)
    {
        this.statusCategoryKey = key;
        return this;
    }

    public RemoteIssueLinkBuilder statusCategoryColorName(final String name)
    {
        this.statusCategoryColorName = name;
        return this;
    }

    public RemoteIssueLinkBuilder applicationType(final String applicationType)
    {
        this.applicationType = applicationType;
        return this;
    }

    public RemoteIssueLinkBuilder applicationName(final String applicationName)
    {
        this.applicationName = applicationName;
        return this;
    }

    public RemoteIssueLink build()
    {
        return new RemoteIssueLink(
                id,
                issueId,
                globalId,
                title,
                summary,
                url,
                iconUrl,
                iconTitle,
                relationship,
                resolved,
                statusIconUrl,
                statusIconTitle,
                statusIconLink,
                applicationType,
                applicationName,
                statusName,
                statusDescription,
                statusCategoryKey,
                statusCategoryColorName);
    }
}