package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.AbstractEvent;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.transport.FieldValuesHolder;

/**
 * Event that is triggered when a user changes project or issue type in simple search and they click "Refresh search to update form fields".
 */
@PublicApi
public final class RefreshIssueSearchEvent extends AbstractEvent
{
    private final SearchRequest searchRequest;

    public RefreshIssueSearchEvent(SearchRequest searchRequest)
    {
        this.searchRequest = searchRequest;
    }

    public SearchRequest getSearchRequest()
    {
        return searchRequest;
    }
    
    public String getFromQuery()
    {
        return searchRequest != null ? searchRequest.getQuery().toString() : null;
    }
}
