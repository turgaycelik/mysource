package com.atlassian.jira.web.action.issue;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.search.SearchResults;

/**
* Value container for search results helper
*
* @since v5.2
*/
@Internal
public class SearchResultsInfo
{
    protected SearchResults searchResults;
    protected Long selectedIssueId;

    public SearchResultsInfo()
    {
    }

    public SearchResults getSearchResults()
    {
        return searchResults;
    }

    public Long getSelectedIssueId()
    {
        return selectedIssueId;
    }
}
