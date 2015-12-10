package com.atlassian.jira.plugin.issueview;

/**
 * This is used by {@link com.atlassian.jira.plugin.issueview.IssueView} plugins to retrieve information about their context.
 */
public interface IssueViewRequestParams
{
    public IssueViewFieldParams getIssueViewFieldParams();
}