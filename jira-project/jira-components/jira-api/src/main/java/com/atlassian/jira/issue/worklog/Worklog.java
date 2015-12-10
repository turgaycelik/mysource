package com.atlassian.jira.issue.worklog;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Date;

/**
 * Represents an issue worklog.<br>
 */
@PublicApi
public interface Worklog
{
    public Long getId();

    /**
     * @deprecated Use {@link #getAuthorKey()} instead. Since v6.0.
     * @return Worklog author's key or null
     */
    public String getAuthor();


    /**
     * @deprecated Use {@link com.atlassian.jira.user.ApplicationUser#getDisplayName()} of {@link #getAuthorObject()} instead. Since v6.0.
     * @return Worklog author's display name or key if user is not existing
     */
    public String getAuthorFullName();

    /**
     * @deprecated Use {@link #getAuthorKey()} instead. Since v6.0.
     * @return Worklog update author's key  or null
     */
    public String getUpdateAuthor();

    /**
     * @deprecated Use {@link com.atlassian.jira.user.ApplicationUser#getDisplayName()} of {@link #getUpdateAuthorObject()} instead. Since v6.0.
     * @return Worklog update author's display name or key if user is not existing
     */
    public String getUpdateAuthorFullName();

    /**
     * @return Worklog author's key
     */
    public String getAuthorKey();

    /**
     * @return Worklog author's object or null if is not existing
     */
    public ApplicationUser getAuthorObject();

    /**
     * @return Worklog author update's key
     */
    public String getUpdateAuthorKey();

    /**
     * @return Worklog author update's object or null if is not existing
     */
    public ApplicationUser getUpdateAuthorObject();

    public Date getStartDate();

    public Long getTimeSpent();

    public String getGroupLevel();

    public Long getRoleLevelId();

    public ProjectRole getRoleLevel();

    public String getComment();

    public Date getCreated();

    public Date getUpdated();

    public Issue getIssue();

}
