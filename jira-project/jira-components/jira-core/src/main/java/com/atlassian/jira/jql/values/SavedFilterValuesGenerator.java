package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Gets all matching filters.
 *
 * @since v4.0
 */
public class SavedFilterValuesGenerator implements ClauseValuesGenerator
{
    private final SearchRequestService searchRequestService;

    public SavedFilterValuesGenerator(SearchRequestService searchRequestService)
    {
        this.searchRequestService = searchRequestService;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        List<Result> savedFilterValues = new ArrayList<Result>();
        // Make sure that we search for an exact name match
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().
                setName(StringUtils.isBlank(valuePrefix) ? "" : valuePrefix).
                setEntitySearchContext(SharedEntitySearchContext.USE);

        final SharedEntitySearchResult<SearchRequest> sharedEntitySearchResult = searchRequestService.search(new JiraServiceContextImpl(searcher), builder.toSearchParameters(), 0, maxNumResults);
        final List<SearchRequest> searchRequests = sharedEntitySearchResult.getResults();
        for (SearchRequest searchRequest : searchRequests)
        {
            // Should not need this since we should not get back more than this from the service call
            if (savedFilterValues.size() == maxNumResults)
            {
                break;
            }
            savedFilterValues.add(new Result(searchRequest.getName()));
        }
        return new Results(savedFilterValues);
    }
}
