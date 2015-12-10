package com.atlassian.jira.plugin.issueview;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;

/**
 * A specific view of an Issue.  Generally this is a different content type (eg. XML, Word or PDF versions
 * of an issue).
 *
 * @see IssueViewModuleDescriptor
 */
@PublicSpi
public interface IssueView
{
    public void init(IssueViewModuleDescriptor issueViewModuleDescriptor);

    public String getContent(Issue issue, IssueViewRequestParams issueViewRequestParams);

    public void writeHeaders(Issue issue, RequestHeaders requestHeaders, IssueViewRequestParams issueViewRequestParams);
}
