package com.atlassian.jira.index;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.issue.comments.Comment;

/**
 * Interface for extractors adding fields based on comments
 * @since 6.2
 */
@ExperimentalApi
public interface CommentSearchExtractor extends EntitySearchExtractor<Comment>
{

}
