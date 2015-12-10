package com.atlassian.jira.jql.context;

/**
 * Represents an IssueType that is part of a search context.
 *
 * @since v4.0
 */
public interface IssueTypeContext
{
    /**
     * @return the issue type id for this context element.
     */
    String getIssueTypeId();

    /**
     * Indicates the special case of all issue types that are not enumerated. If this is true then the value for
     * issueTypeId will be null.
     *
     * @return true if all, false otherwise.
     */
    boolean isAll();
}