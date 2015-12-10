package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;

import java.io.Writer;

/**
 * A specific view of a Search Request.  Generally this is a different content type (eg. XML, Word or PDF versions
 * of a search request).
 *
 * @see com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor
 * @see com.atlassian.jira.issue.search.SearchRequest
 */
@PublicSpi
public interface SearchRequestView
{
    /**
     * A lifecycle method that will be called by the plugin system that gives access to the {@link com.atlassian.plugin.ModuleDescriptor}
     * that controls this plugin.
     * @param moduleDescriptor the controller of this plugin.
     */
    public void init(SearchRequestViewModuleDescriptor moduleDescriptor);

    /**
     * Responsible for writing out the searchResults including Headers and Footers of the implementing view type.
     *
     * @param searchRequest the original search request submitted by the user
     * @param searchRequestParams stores a shallow copy of the session and a pagerfilter to determine how many results to display
     * @param writer The writer used to stream the response.
     * @throws com.atlassian.jira.issue.search.SearchException Exceptions occured while trying to peform a search on the {@link com.atlassian.jira.issue.search.SearchRequest}
     */
    public void writeSearchResults(SearchRequest searchRequest, SearchRequestParams searchRequestParams, Writer writer)
            throws SearchException;

    /**
     * Prints the HTML headers for non-typial HTML such as Word or Excel views. (e.g.: requestHeaders.addHeader("content-disposition", "attachment;filename="sample.doc";");)
     *
     * @param searchRequest the original search request submitted by the user
     * @param requestHeaders subset of HttpServletResponse responsible for setting headers only
     * @param searchRequestParams context about the current search request
     */
    public void writeHeaders(SearchRequest searchRequest, RequestHeaders requestHeaders, SearchRequestParams searchRequestParams);
}
