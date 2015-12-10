package com.atlassian.jira.issue.views.util;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.web.bean.PagerFilter;

public interface SearchRequestHeader
{
    String getHeader(final SearchRequest searchRequest, final PagerFilter pagerFilter, final JiraResourcedModuleDescriptor<?> descriptor);
}