package com.atlassian.jira.issue.views.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;

public interface SearchRequestPreviousView
{
    String getLinkToPrevious(final SearchRequest searchRequest, final JiraResourcedModuleDescriptor<?> descriptor);

    String getLinkToPrevious(final Issue issue, final JiraResourcedModuleDescriptor<?> descriptor);
}