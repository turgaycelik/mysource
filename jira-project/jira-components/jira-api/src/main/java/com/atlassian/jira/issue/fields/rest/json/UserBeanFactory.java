package com.atlassian.jira.issue.fields.rest.json;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.user.ApplicationUser;

/**
 * This provides a simple, dependency-free, straight forward API to generating the JSON corresponding to a User.
 * @since v5.2
 */
@ExperimentalApi
public interface UserBeanFactory
{
    /**
     * Generate a bean suitable for serialisation by Jackon into JSON.
     * @param createdUser Create UserJsonBean for this user
     * @return
     * @deprecated Use {@link #createBean(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.user.ApplicationUser)}
     */
    @Deprecated
    UserJsonBean createBean(User createdUser);

    /**
     * Generate a bean suitable for serialisation by Jackon into JSON for given user in context of loggedInUser.
     * @param createdUser Create UserJsonBean for createdUser
     * @param loggedInUser UserJsonBean will be created in context of loggedInUser (i.e. escape/hide email address if necessary)
     * @return
     */
    UserJsonBean createBean(User createdUser, ApplicationUser loggedInUser);
}
