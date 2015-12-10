package com.atlassian.jira.dev.reference.plugin.issue.views;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issueview.IssueView;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;

public class ReferenceIssueView implements IssueView
{
    public void init(IssueViewModuleDescriptor issueViewModuleDescriptor)
    {
    }

    public void writeHeaders(Issue issue, RequestHeaders requestHeaders, IssueViewRequestParams issueViewRequestParams)
    {
        // Do nothing.
    }

    public String getContent(Issue issue, IssueViewRequestParams issueViewRequestParams)
    {
        return "<info> Upgraded Reference plugin doesn't tell you anything about an issue. Its just for reference. </info>";
    }
}
