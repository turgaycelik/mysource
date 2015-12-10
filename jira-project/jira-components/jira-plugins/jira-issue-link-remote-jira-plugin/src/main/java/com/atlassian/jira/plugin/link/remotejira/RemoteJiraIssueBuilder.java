package com.atlassian.jira.plugin.link.remotejira;

/**
 * Builder for {@link RemoteJiraIssue} instances.
 *
 * @since v5.0
 */
public class RemoteJiraIssueBuilder
{
    private Long id;
    private String key;
    private String summary;
    private String iconUrl;
    private String iconTitle;
    private String statusName;
    private String statusDescription;
    private String statusIconUrl;
    private String statusIconTitle;
    private String statusCategoryKey;
    private String statusCategoryColorName;
    private Boolean resolved;
    private String browseUrl;

    public RemoteJiraIssueBuilder id(final Long id)
    {
        this.id = id;
        return this;
    }

    public RemoteJiraIssueBuilder key(final String key)
    {
        this.key = key;
        return this;
    }

    public RemoteJiraIssueBuilder summary(final String summary)
    {
        this.summary = summary;
        return this;
    }

    public RemoteJiraIssueBuilder iconUrl(final String iconUrl)
    {
        this.iconUrl = iconUrl;
        return this;
    }

    public RemoteJiraIssueBuilder iconTitle(final String iconTitle)
    {
        this.iconTitle = iconTitle;
        return this;
    }

    public RemoteJiraIssueBuilder statusName(final String name)
    {
        this.statusName = name;
        return this;
    }

    public RemoteJiraIssueBuilder statusDescription(final String description)
    {
        this.statusDescription = description;
        return this;
    }

    public RemoteJiraIssueBuilder statusIconUrl(final String statusIconUrl)
    {
        this.statusIconUrl = statusIconUrl;
        return this;
    }

    public RemoteJiraIssueBuilder statusIconTitle(final String statusIconTitle)
    {
        this.statusIconTitle = statusIconTitle;
        return this;
    }

    public RemoteJiraIssueBuilder statusCategoryKey(final String key)
    {
        this.statusCategoryKey = key;
        return this;
    }

    public RemoteJiraIssueBuilder statusCategoryColorName(final String name)
    {
        this.statusCategoryColorName = name;
        return this;
    }

    public RemoteJiraIssueBuilder resolved(final Boolean resolved)
    {
        this.resolved = resolved;
        return this;
    }

    public RemoteJiraIssueBuilder browseUrl(final String browseUrl)
    {
        this.browseUrl = browseUrl;
        return this;
    }

    public RemoteJiraIssue build()
    {
        final boolean resolved = (this.resolved != null && this.resolved.equals(Boolean.TRUE));
        return new RemoteJiraIssue(id, key, summary, iconUrl, iconTitle, statusIconUrl, statusIconTitle, resolved, browseUrl, statusName, statusDescription, statusCategoryKey, statusCategoryColorName);
    }
}
