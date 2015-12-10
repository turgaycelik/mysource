package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestInfo;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.plugin.web.descriptors.ConditionalDescriptor;

/**
 * An search request view allows you to view a search request in different ways (eg XML, Word, PDF, Excel)
 *
 * @see SearchRequestView
 */
public interface SearchRequestViewModuleDescriptor extends JiraResourcedModuleDescriptor<SearchRequestView>, OrderableModuleDescriptor, ConditionalDescriptor
{
    SearchRequestView getSearchRequestView();

    String getContentType();

    String getFileExtension();

    String getURLWithoutContextPath(SearchRequest searchRequest);

    String getURLWithoutContextPath(SearchRequestInfo searchRequest);

    boolean isBasicAuthenticationRequired();

    boolean isExcludeFromLimitFilter();
}
