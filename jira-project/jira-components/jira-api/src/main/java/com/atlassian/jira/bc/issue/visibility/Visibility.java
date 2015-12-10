package com.atlassian.jira.bc.issue.visibility;

/**
 * This interface represent visibility of a worklog or a comment. Visibilities can be matched by {@link VisibilityVisitor}.
 * They are used in {@link com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters} and in
 * {@link com.atlassian.jira.bc.issue.worklog.WorklogInputParameters} which have to be validated before use.
 *
 * @since v6.4
 */
public interface Visibility
{
    <T> T accept(VisibilityVisitor<T> visitor);
}