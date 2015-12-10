package com.atlassian.jira.functest.framework.assertions;

/**
 * Responsible for holding assertions about a specified group of comments.
 *
 * @since v4.2
 */
public interface CommentAssertions
{
    /**
     * Asserts whether the specified comments are visible to a user in a specific issue.
     * @param userName The user-name of the user to check visibility for.
     * @param issueKey The issue key of the issue in play.
     */
    void areVisibleTo(String userName, String issueKey);

    /**
     * Asserts whether the specified comments are not visible to a user in a specific issue.
     * @param userName The user-name of the user to check visibility for.
     * @param issueKey The issue key of the issue in play.
     */
    void areNotVisibleTo(String userName, String issueKey);
}
