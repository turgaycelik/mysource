package com.atlassian.jira.jql.context;

/**
 * Specifies a project to issue types context.
 *
 * @since v4.0
 */
public interface ProjectIssueTypeContext
{
    ProjectContext getProjectContext();

    IssueTypeContext getIssueTypeContext();
}
