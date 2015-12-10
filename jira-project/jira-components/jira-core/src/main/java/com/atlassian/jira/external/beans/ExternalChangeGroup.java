package com.atlassian.jira.external.beans;

import java.util.Date;

/**
 * Used to represent a ChangeGroup when importing data.
 *
 * @since v3.13
 */
public class ExternalChangeGroup
{
    private String id;
    private String issueId;
    private String author;
    private Date created;

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(final String author)
    {
        this.author = author;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(final Date created)
    {
        this.created = created;
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getIssueId()
    {
        return issueId;
    }

    public void setIssueId(final String issueId)
    {
        this.issueId = issueId;
    }
}
