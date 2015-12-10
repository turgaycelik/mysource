package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.query.Query;

/**
 * Factory for constructing SearchRequests.  The resulting SearchRequests are not persisted.
 *
 * @since v3.13
 */
public interface SearchRequestFactory
{
    /**
     * Takes a user and a raw map of request parameters
     * that are used to create an object representation of the search request.
     *
     * @param oldSearchRequest The original SearchRequest, if provided will be cloned as the basis for the new
     * search request. The search requests {@link com.atlassian.query.Query} will always be populated from
     * the passed in parameters. If this is the same as the oldSearchRequest then the new search requests modified
     * flag will be false. This can be null.
     * @param searchUser    The user that is searching
     * @param parameterMap  The raw request parameters that will be passed through the
     * {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer#populateFromParams(User,com.atlassian.jira.issue.transport.FieldValuesHolder,com.atlassian.jira.issue.transport.ActionParams)}
     * and {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer#getSearchClause(User,com.atlassian.jira.issue.transport.FieldValuesHolder)}
     * methods to create a search clause. The parameters will also be used to create an {@link com.atlassian.query.order.OrderBy}
     * clause via the {@link com.atlassian.jira.issue.search.util.SearchSortUtil#getOrderByClause(java.util.Map)} call.
     * Must not be null.
     * @return a new SearchRequest based off given parameters.
     * @deprecated Since 6.3.3, use {@link #createFromQuery(SearchRequest, com.atlassian.crowd.embedded.api.User, com.atlassian.query.Query)} instead.
     */
    @Deprecated
    SearchRequest createFromParameters(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams parameterMap);

    /**
     * Takes a user, a SearchQuery and an original search request that are used to create an object
     * representation of the search request.
     *
     * @param oldSearchRequest The original SearchRequest, if provided will be cloned as the basis for the new
     * search request. The search requests {@link com.atlassian.query.Query} will always be populated from
     * the passed in parameters. If this is the same as the oldSearchRequest then the new search requests modified
     * flag will be false. This can be null.
     * @param searchUser    The user that is searching
     * @param query the query that defines the search requests where and order by clauses.
     *
     * @return a new SearchRequest based off given parameters.
     */
    SearchRequest createFromQuery(final SearchRequest oldSearchRequest, final User searchUser, final Query query);
}
