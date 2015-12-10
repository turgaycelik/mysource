package com.atlassian.jira.plugin.searchrequestview.auth;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

class SearchResultSizeAuthorizer implements Authorizer
{

    private final SearchProvider searchProvider;
    private final long maxAllowed;
    private final Authorizer delegate;

    SearchResultSizeAuthorizer(final SearchProvider searchProvider, final long maxAllowed, final Authorizer delegate)
    {
        notNull("searchProvider", searchProvider);
        notNull("delegate", delegate);
        if (maxAllowed < 0)
        {
            throw new IllegalArgumentException(this.getClass().getName() + " cannot be configured with a negative maxAllowed: '" + maxAllowed + "'");
        }

        this.searchProvider = searchProvider;
        this.maxAllowed = maxAllowed;
        this.delegate = delegate;
    }

    @Override
    public Result isSearchRequestAuthorized(final User user, final SearchRequest searchRequest, final SearchRequestParams params)
    {
        if (!params.isReturnMax() && getSearchCount(user, searchRequest, params) > maxAllowed)
        {
            return new Failure(
                "You are not allowed to get a result set of more than " + maxAllowed + " results. Current search returns " + getSearchCount(user,
                        searchRequest, params) + " results");
        }
        return delegate.isSearchRequestAuthorized(user, searchRequest, params);
    }

    /**
     * Retrieve the search count from the search request parameters if available. If not, continue retrieving it from
     * the search provider.
     *
     * @param user who is trying to perform the
     * @param searchRequest they are attempting
     * @param params the parameters for the search (such as tempMax etc)
     * @return search count
     */
    private long getSearchCount(final User user, final SearchRequest searchRequest, final SearchRequestParams params)
    {
        if (params != null)
        {
            final long resultCount;
            final String searchCount = String.valueOf(params.getSession().get(SearchRequestURLHandler.Parameter.SEARCH_COUNT));
            if (StringUtils.isNumeric(searchCount))
            {
                resultCount = Long.parseLong(searchCount);
            }
            else
            {
                resultCount = getSearchCountFromSearchProvider(user, searchRequest);
            }
            if (params.getPagerFilter() != null)
            {
                return Math.min(resultCount, params.getPagerFilter().getMax());
            }
            return resultCount;
        }
        return getSearchCountFromSearchProvider(user, searchRequest);
    }

    private long getSearchCountFromSearchProvider(final User user, final SearchRequest searchRequest)
    {
        try
        {
            return searchProvider.searchCount((searchRequest == null) ? null : searchRequest.getQuery(), user);
        }
        catch (final SearchException e)
        {
            throw new RuntimeException(e);
        }
    }

    Authorizer getDelegate()
    {
        return delegate;
    }

    long getMaxAllowed()
    {
        return maxAllowed;
    }
}