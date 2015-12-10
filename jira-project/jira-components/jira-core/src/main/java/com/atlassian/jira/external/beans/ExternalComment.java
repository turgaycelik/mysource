package com.atlassian.jira.external.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

public class ExternalComment
{
    // @TODO refactor Remote RPC objects to use this

    private String body;
    private String username;
    private String groupLevel;
    private Long roleLevelId;
    private String id;
    private Date timePerformed;
    private String issueId;
    private String updateAuthor;
    private Date updated;

    public ExternalComment()
    {
    }

    public ExternalComment(String body)
    {
        this.body = body;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getGroupLevel()
    {
        return groupLevel;
    }

    public void setGroupLevel(String groupLevel)
    {
        this.groupLevel = groupLevel;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Date getTimePerformed()
    {
        return timePerformed == null ? null : new Date(timePerformed.getTime());
    }

    public void setTimePerformed(Date timePerformed)
    {
        this.timePerformed = (timePerformed == null ? null : new Date(timePerformed.getTime()));
    }

    public Long getRoleLevelId()
    {
        return roleLevelId;
    }

    public void setRoleLevelId(Long roleLevelId)
    {
        this.roleLevelId = roleLevelId;
    }

    public String getIssueId()
    {
        return issueId;
    }

    public void setIssueId(final String issueId)
    {
        this.issueId = issueId;
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
        return updated;
    }

    public void setUpdated(final Date updated)
    {
        this.updated = updated;
    }

    public String toString()
    {
        return new ToStringBuilder(this)
                .append(StringUtils.abbreviate(StringUtils.replaceChars(getBody(),"\r\n", ""), 50))
                .toString();
    }
}

