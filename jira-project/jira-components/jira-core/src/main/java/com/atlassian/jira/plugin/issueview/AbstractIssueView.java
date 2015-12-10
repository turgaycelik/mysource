package com.atlassian.jira.plugin.issueview;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;

/**
 *
 */
public abstract class AbstractIssueView implements IssueView
{
    protected IssueViewModuleDescriptor descriptor;
    protected static final String ACTION_ORDER_DESC = "desc";

    public abstract String getContent(Issue issue, IssueViewRequestParams issueViewRequestParams);

    public abstract String getBody(Issue issue, IssueViewRequestParams issueViewRequestParams);

    public void init(IssueViewModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    /**
     * A default implementation that does nothing
     */
    public void writeHeaders(Issue issue, RequestHeaders requestHeaders, IssueViewRequestParams issueViewRequestParams)
    {
        // do nothing
    }
}
