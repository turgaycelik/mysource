package com.atlassian.jira.issue.worklog;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.JiraDateUtils;

import java.util.Date;

/**
 * Represents an issue worklog.<br>
 */
public class WorklogImpl implements Worklog
{
    private final WorklogManager worklogManager;

    private final Long id;
    private final String authorKey;
    private final String updateAuthorKey;
    private final String comment;
    private final String groupLevel;
    private final Long roleLevelId;
    private final Date created;
    private final Date updated;
    private final Date startDate;
    private final Long timeSpent;
    private final Issue issue;

    public WorklogImpl(WorklogManager worklogManager, Issue issue, Long id, String authorKey, String comment, Date startDate, String groupLevel, Long roleLevelId, Long timeSpent)
    {
        if (timeSpent == null)
        {
            throw new IllegalArgumentException("timeSpent must be set!");
        }
        this.worklogManager = worklogManager;
        this.authorKey = authorKey;
        this.updateAuthorKey = authorKey;
        this.comment = comment;
        this.groupLevel = groupLevel;
        this.roleLevelId = roleLevelId;
        this.timeSpent = timeSpent;
        Date createDate = new Date();
        this.startDate = (startDate == null) ? createDate : startDate;
        this.created = createDate;
        this.updated = createDate;
        this.issue = issue;
        this.id = id;
    }

    public WorklogImpl(WorklogManager worklogManager, Issue issue, Long id, String authorKey, String comment, Date startDate, String groupLevel, Long roleLevelId, Long timeSpent, String updateAuthorKey, Date created, Date updated)
    {
        if (timeSpent == null)
        {
            throw new IllegalArgumentException("timeSpent must be set!");
        }
        this.worklogManager = worklogManager;
        this.authorKey = authorKey;
        if(updateAuthorKey == null)
        {
            updateAuthorKey = this.authorKey;
        }
        this.updateAuthorKey = updateAuthorKey;
        this.comment = comment;
        this.groupLevel = groupLevel;
        this.roleLevelId = roleLevelId;
        this.timeSpent = timeSpent;
        Date createdDate = JiraDateUtils.copyOrCreateDateNullsafe(created);
        this.startDate = (startDate == null) ? createdDate : startDate;
        this.created = createdDate;
        this.updated = (updated == null) ? createdDate : updated;
        this.issue = issue;
        this.id = id;
    }

    public Long getId()
    {
        return this.id;
    }

    public String getAuthor()
    {
        return authorKey;
    }

    public String getAuthorFullName()
    {
        ApplicationUser user = getAuthorObject();
        if(user != null){
            return user.getDisplayName();
        }
        return authorKey;
    }

    public String getUpdateAuthor()
    {
        return updateAuthorKey;
    }

    public String getUpdateAuthorFullName()
    {
        ApplicationUser user = getUpdateAuthorObject();
        if(user != null){
            return user.getDisplayName();
        }
        return updateAuthorKey;
    }

    @Override
    public String getAuthorKey()
    {
        return authorKey;
    }

    @Override
    public ApplicationUser getAuthorObject()
    {
        return ApplicationUsers.byKey(authorKey);
    }

    @Override
    public String getUpdateAuthorKey()
    {
        return updateAuthorKey;
    }

    @Override
    public ApplicationUser getUpdateAuthorObject()
    {
        return ApplicationUsers.byKey(updateAuthorKey);
    }

    public Date getStartDate()
    {
        return JiraDateUtils.copyDateNullsafe(startDate);
    }

    public Long getTimeSpent()
    {
        return timeSpent;
    }

    public String getGroupLevel()
    {
        return groupLevel;
    }

    public Long getRoleLevelId()
    {
        return roleLevelId;
    }

    public ProjectRole getRoleLevel()
    {
        return roleLevelId == null ? null : worklogManager.getProjectRole(roleLevelId);
    }

    public String getComment()
    {
        return this.comment;
    }


    public Date getCreated()
    {
        return created;
    }

    public Date getUpdated()
    {
        return updated;
    }

    public Issue getIssue()
    {
        return issue;
    }

}
