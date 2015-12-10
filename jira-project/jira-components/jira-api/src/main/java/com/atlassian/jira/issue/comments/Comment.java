package com.atlassian.jira.issue.comments;

import java.util.Date;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;

@PublicApi
public interface Comment extends WithId
{
    /**
     * Returns the key for the user that created the comment
     *
     * @return the key for the user that created the comment
     * @deprecated Use {@link #getAuthorKey()} or {@link #getAuthorApplicationUser()} instead. Since v6.0.
     */
    public String getAuthor();

    /**
     * Returns the key for the user that created the comment
     *
     * @return the key for the user that created the comment
     * @deprecated Use {@link #getAuthorKey()} or {@link #getAuthorApplicationUser()} instead. Since v6.0.
     */
    public String getAuthorKey();

    /**
     * Returns the {@link User} that created the comment
     *
     * @return the {@link User} that created the comment.
     * @deprecated Use {@link #getAuthorApplicationUser()} instead. Since v6.0.
     */
    public User getAuthorUser();

    /**
     * Returns the {@link ApplicationUser user} that created the comment
     * @return the {@link ApplicationUser user} that created the comment
     */
    public ApplicationUser getAuthorApplicationUser();

    public String getAuthorFullName();

    public String getBody();

    public Date getCreated();

    public String getGroupLevel();

    public Long getId();

    public Long getRoleLevelId();

    public ProjectRole getRoleLevel();

    public Issue getIssue();

    /**
     * @deprecated Use {@link #getUpdateAuthorApplicationUser()} instead. Since v6.0.
     *
     * @return userkey of the update author
     */
    public String getUpdateAuthor();

    /**
     * @deprecated Use {@link #getUpdateAuthorApplicationUser()} instead. Since v6.0.
     *
     * Get the user that performed the update
     * @return a {@link User} object
     */
    public User getUpdateAuthorUser();

    /**
     * Get the user that performed the update
     * @return an {@link ApplicationUser object}
     */
    public ApplicationUser getUpdateAuthorApplicationUser();

    public String getUpdateAuthorFullName();

    public Date getUpdated();

}
