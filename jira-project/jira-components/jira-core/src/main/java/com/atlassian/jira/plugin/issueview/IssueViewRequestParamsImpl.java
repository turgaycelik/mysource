package com.atlassian.jira.plugin.issueview;

/**
 * Default implmentation of @see com.atlassian.jira.plugin.issueview.IssueViewRequestParams.
 */
public class IssueViewRequestParamsImpl implements IssueViewRequestParams
{
    private final IssueViewFieldParams issueViewFieldParams;

    public IssueViewRequestParamsImpl(IssueViewFieldParams issueViewFieldParams)
    {
        this.issueViewFieldParams = issueViewFieldParams;
    }

    public IssueViewFieldParams getIssueViewFieldParams()
    {
        return issueViewFieldParams;
    }
}