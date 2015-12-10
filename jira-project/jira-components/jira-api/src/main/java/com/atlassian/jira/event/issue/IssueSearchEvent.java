package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.AbstractEvent;
import com.atlassian.jira.issue.search.SearchRequest;

/**
 * Event that is triggered when a user searches for issues.
 */
@PublicApi
public final class IssueSearchEvent extends AbstractEvent
{
    private final SearchRequest searchRequest;
    private final String type;

    public IssueSearchEvent(SearchRequest searchRequest)
    {
        this.searchRequest = searchRequest;
        this.type = null;
    }

    public IssueSearchEvent(SearchRequest searchRequest, String type)
    {
        this.searchRequest = searchRequest;
        this.type = type;
    }

    public SearchRequest getSearchRequest()
    {
        return searchRequest;
    }

    public String getType()
    {
        return type;
    }

    public String getQuery()
    {
        return searchRequest != null ? searchRequest.getQuery().toString() : null;
    }
    
    public String getSavedFilterName()
    {
        return searchRequest != null ? searchRequest.getName() : null;
    }
}
