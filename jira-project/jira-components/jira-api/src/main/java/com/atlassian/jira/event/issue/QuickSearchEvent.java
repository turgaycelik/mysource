package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.AbstractEvent;
import com.atlassian.jira.issue.search.SearchRequest;

/**
 * Event that is triggered when a user does a search in quicksearch. Does not record quick search to browse issue events -
 * these are triggered as QuickBrowseEvent
 */
@PublicApi
public final class QuickSearchEvent extends AbstractEvent
{
    private final String searchString;

    public QuickSearchEvent(String searchString)
    {
        this.searchString = searchString;
    }

    public String getSearchString()
    {
        return searchString;
    }
}
