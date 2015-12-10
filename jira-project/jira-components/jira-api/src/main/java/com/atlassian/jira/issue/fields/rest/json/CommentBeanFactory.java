package com.atlassian.jira.issue.fields.rest.json;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;

/**
 * This provides a simple, dependency-free, straight forward API to generating the JSON corresponding to a Comment.
 * @since v5.2
 */
@ExperimentalApi
public interface CommentBeanFactory
{
    /**
     * Generate a bean suitable for serialisation by Jackon into JSON.
     * @param comment
     * @return
     * @deprecated Use {@link #createBean(com.atlassian.jira.issue.comments.Comment)}
     */
    @Deprecated
    CommentJsonBean createBean(Comment comment);

    /**
     * Generate a bean suitable for serialisation by Jackon into JSON.
     * Render the body of the comment according to the field configuration. Mostly this means convert wiki
     * markup to HTML.
     * @deprecated Use {@link #createRenderedBean(com.atlassian.jira.issue.comments.Comment)}
     */
    @Deprecated
    CommentJsonBean createRenderedBean(Comment comment);

    /**
     * Generate a bean suitable for serialisation by Jackon into JSON.
     * @param comment
     */
    CommentJsonBean createBean(Comment comment, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter);

    /**
     * Generate a bean suitable for serialisation by Jackon into JSON.
     * Render the body of the comment according to the field configuration. Mostly this means convert wiki
     * markup to HTML.
     */
    CommentJsonBean createRenderedBean(Comment comment, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter);
}
