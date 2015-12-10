package com.atlassian.jira.jql.context;

/**
 * Specifies if a query context element is of a specific type.
 */
public enum QueryContextElementType
{
    /**
     * Describes when a query context element is referenced directly in a query (e.g. a project is specified in a query
     * then it is an explicit context element).
     */
    EXPLICIT,

    /**
     * Describes when a query context element is referenced indirectly in a query (e.g. the description field is
     * specified in a query and based on its visibility it has an implied project/issue-type context).
     */
    IMPLICIT
}
