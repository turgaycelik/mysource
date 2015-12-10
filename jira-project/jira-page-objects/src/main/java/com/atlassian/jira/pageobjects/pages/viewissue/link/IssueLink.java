package com.atlassian.jira.pageobjects.pages.viewissue.link;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents an issue link.
 */
public class IssueLink
{
    private final String elementId;
    private final String relationship;
    private final String title;
    private final String url;
    private final String summary;
    private final String iconUrl;
    private final String deleteUrl;
    private final String priorityIconUrl;
    private final String status;
    private final boolean resolved;

    private IssueLink(String elementId, String relationship, String title, String url, String summary, String iconUrl, String deleteUrl, String priorityIconUrl, String status, boolean resolved)
    {
        this.elementId = elementId;
        this.relationship = relationship;
        this.title = title;
        this.url = url;
        this.summary = summary;
        this.iconUrl = iconUrl;
        this.deleteUrl = deleteUrl;
        this.priorityIconUrl = priorityIconUrl;
        this.status = status;
        this.resolved = resolved;
    }

    /**
     * Returns the element ID of the link. Not exposed for public consumption.
     *
     * @return element ID of the link
     */
    String getElementId()
    {
        return elementId;
    }

    /**
     * Returns the relationship of the link to the issue. E.g. "relates to", "blocked by"
     *
     * @return relationship of the link to the issue, never null
     */
    public String getRelationship()
    {
        return relationship;
    }

    /**
     * Returns the title.
     *
     * @return title, never null
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns the URL of the link.
     *
     * @return URL of the link, <tt>null</tt> if there is no URL for the link
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Returns the summary of the link.
     *
     * @return summary of the link if one exists, otherwise empty string
     */
    public String getSummary()
    {
        return summary;
    }

    /**
     * Returns the icon URL.
     *
     * @return icon URL
     */
    public String getIconUrl()
    {
        return iconUrl;
    }

    /**
     * Returns the URL to delete the link.
     *
     * @return URL to delete the link, never null
     */
    public String getDeleteUrl()
    {
        return deleteUrl;
    }

    /**
     * Returns <tt>true</tt>if the link has a priority.
     *
     * @return <tt>true</tt> if link has a priority
     */
    public boolean hasPriority()
    {
        return priorityIconUrl != null;
    }

    /**
     * Returns the URL of the priority icon.
     *
     * @return URL of the priority icon, or <tt>null</tt> if the link does not have a priority
     */
    public String getPriorityIconUrl()
    {
        return priorityIconUrl;
    }

    /**
     * Returns <tt>true</tt> if the link has a status.
     *
     * @return <tt>true</tt> if the link has a status
     */
    public boolean hasStatus()
    {
        return status != null;
    }

    /**
     * Returns the URL of the status icon.
     *
     * @return URL of the status icon, or <tt>null</tt> if the link does not have a status
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * Returns <tt>true</tt> if the link target has been resolved
     *
     * @return <tt>true</tt> if the link target has been resolved
     */
    public boolean isResolved()
    {
        return resolved;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("elementId", elementId).
                append("relationship", relationship).
                append("title", title).
                append("url", url).
                append("summary", summary).
                append("iconUrl", iconUrl).
                append("deleteUrl", deleteUrl).
                append("priorityIconUrl", priorityIconUrl).
                append("status", status).
                append("resolved", resolved).
                toString();
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String elementId;
        private String relationship;
        private String title;
        private String url;
        private String summary = "";
        private String iconUrl;
        private String deleteUrl;
        private String priorityIconUrl;
        private String status;
        private boolean resolved;

        Builder elementId(String elementId)
        {
            this.elementId = elementId;
            return this;
        }

        public Builder relationship(String relationship)
        {
            this.relationship = relationship;
            return this;
        }

        public Builder title(String title)
        {
            this.title = title;
            return this;
        }

        public Builder url(String url)
        {
            this.url = url;
            return this;
        }

        public Builder summary(String summary)
        {
            this.summary = summary;
            return this;
        }

        public Builder iconUrl(String iconUrl)
        {
            this.iconUrl = iconUrl;
            return this;
        }

        public Builder deleteUrl(String deleteUrl)
        {
            this.deleteUrl = deleteUrl;
            return this;
        }

        public Builder priorityIconUrl(String priorityIconUrl)
        {
            this.priorityIconUrl = priorityIconUrl;
            return this;
        }

        public Builder status(String statusIconUrl)
        {
            this.status = statusIconUrl;
            return this;
        }

        public Builder resolved(boolean resolved)
        {
            this.resolved = resolved;
            return this;
        }

        public IssueLink build()
        {
            return new IssueLink(elementId, relationship, title, url, summary, iconUrl, deleteUrl, priorityIconUrl, status, resolved);
        }
    }
}
