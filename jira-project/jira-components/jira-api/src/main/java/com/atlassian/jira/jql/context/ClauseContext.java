package com.atlassian.jira.jql.context;

import java.util.Set;

/**
 * Used to specify the context for an individual clause. This will be a set of ProjectIssueTypeContexts.
 *
 * @since v4.0
 */
public interface ClauseContext
{
    /**
     * @return the project/issue type contexts that are defined by a clause.
     */
    Set<ProjectIssueTypeContext> getContexts();

    /**
     * @return true if one of the contexts is the same as ProjectIssueTypeContextImpl.createGlobalContext().
     */
    boolean containsGlobalContext();
}
