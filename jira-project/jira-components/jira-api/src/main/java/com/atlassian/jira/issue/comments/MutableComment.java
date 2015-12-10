package com.atlassian.jira.issue.comments;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Date;

/**
 * Represents a comment's in JIRA.
 * After calling any 'setter' method, you will need to call
 * {@link com.atlassian.jira.bc.issue.comment.CommentService#update} which does permission checking or
 * {@link CommentManager#update} which will just store the provided object, to persist the change to
 * the database.
 */
@PublicApi
public interface MutableComment extends Comment
{
    /**
     * @param author {@link ApplicationUser} to be set as author.
     */
    public void setAuthor(ApplicationUser author);

    /**
     * @deprecated Use {@link #setAuthor(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     * @param author userkey of the user to be set as author.
     */
    public void setAuthor(String author);

    public void setBody(String body);

    public void setCreated(Date created);

    public void setGroupLevel(String groupLevel);

    public void setRoleLevelId(Long roleLevelId);

    /**
     * @param updateAuthor {@link ApplicationUser} to be set as update author (i.e. the comment editor).
     */
    public void setUpdateAuthor(ApplicationUser updateAuthor);

    /**
     * @deprecated Use {@link #setUpdateAuthor(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     * @param updateAuthor userkey of the user to be set as update author (i.e. the comment editor).
     */
    public void setUpdateAuthor(String updateAuthor);

    public void setUpdated(Date updated);

}
