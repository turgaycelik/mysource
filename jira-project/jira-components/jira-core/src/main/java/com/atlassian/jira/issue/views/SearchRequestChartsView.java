package com.atlassian.jira.issue.views;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.searchrequestview.AbstractSearchRequestView;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;

import java.io.Writer;

/**
 * Displays the charts popup in the issue navigator.
 */
public class SearchRequestChartsView extends AbstractSearchRequestView
{
    public void init(SearchRequestViewModuleDescriptor moduleDescriptor)
    {
        super.init(moduleDescriptor);
    }

    public void writeSearchResults(SearchRequest searchRequest, SearchRequestParams searchRequestParams, Writer writer)
    {
        //essentially a noop.  All the UI tasks are handled in javascript and by REST but needs to be
        //here to enable the plugin module for this view
    }
}