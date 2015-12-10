package com.atlassian.jira.jql.context;

/**
 * Represents an IssueType that is part of a search context.
 *
 * @since v4.0
 */
public interface ProjectContext
{
    /**
     * @return the project id for this context element.
     */
    Long getProjectId();

    /**
     * Indicates the special case of all projects that are not enumerated. If this is true then the value for
     * projectId will be null.
     *
     * @return true if all, false otherwise.
     */
    boolean isAll();
}
