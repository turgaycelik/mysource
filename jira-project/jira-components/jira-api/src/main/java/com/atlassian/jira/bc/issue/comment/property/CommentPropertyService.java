package com.atlassian.jira.bc.issue.comment.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.issue.comments.Comment;

/**
 * The service used to add, update, retrieve and delete properties from {@link com.atlassian.jira.issue.comments.Comment}'s. Each method of this service
 * ensures that the user has permission to perform the operation. For each operation an appropriate event is published.
 *
 * @since v6.2
 */
@ExperimentalApi
public interface CommentPropertyService extends EntityPropertyService<Comment>
{
}
