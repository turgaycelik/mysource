package com.atlassian.jira.external.beans;

import java.util.Date;

/**
 * Used to represent a worklog when importing data.
 *
 * @since v3.13
 */
public class ExternalWorklog
{
    private String id;
    private String issueId;
    private String author;
    private String updateAuthor;
    private String comment;
    private String groupLevel;
    private Long roleLevelId;
    private Date created;
    private Date updated;
    private Date startDate;
    private Long timeSpent;

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(final String author)
    {
        this.author = author;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(final String comment)
    {
        this.comment = comment;
    }

    public Date getCreated()
    {
        return newDateNullSafe(created);
    }

    public void setCreated(final Date created)
    {
        this.created = newDateNullSafe(created);
    }

    public String getGroupLevel()
    {
        return groupLevel;
    }

    public void setGroupLevel(final String groupLevel)
    {
        this.groupLevel = groupLevel;
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

    public Long getRoleLevelId()
    {
        return roleLevelId;
    }

    public void setRoleLevelId(final Long roleLevelId)
    {
        this.roleLevelId = roleLevelId;
    }

    public Date getStartDate()
    {
        return newDateNullSafe(startDate);
    }

    public void setStartDate(final Date startDate)
    {
        this.startDate = newDateNullSafe(startDate);
    }

    public Long getTimeSpent()
    {
        return timeSpent;
    }

    public void setTimeSpent(final Long timeSpent)
    {
        this.timeSpent = timeSpent;
    }

    public String getUpdateAuthor()
    {
        return updateAuthor;
    }

    public void setUpdateAuthor(final String updateAuthor)
    {
        this.updateAuthor = updateAuthor;
    }

    public Date getUpdated()
    {
        return newDateNullSafe(updated);
    }

    public void setUpdated(final Date updated)
    {
        this.updated = newDateNullSafe(updated);
    }

    private Date newDateNullSafe(final Date d)
    {
        return d == null ? null : new Date(d.getTime());
    }

}
