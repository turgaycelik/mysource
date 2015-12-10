package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.clause.SimpleEquivalenceComparator;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default Implementation for SearchRequestFactory
 *
 * @since v3.13
 */
public class DefaultSearchRequestFactory implements SearchRequestFactory
{
    private final IssueSearcherManager issueSearcherManager;
    private final SearchSortUtil searchSortUtil;
    private final SearchService searchService;

    public DefaultSearchRequestFactory(final IssueSearcherManager issueSearcherManager, final SearchSortUtil searchSortUtil, final SearchService searchService)
    {
        this.issueSearcherManager = issueSearcherManager;
        this.searchSortUtil = searchSortUtil;
        this.searchService = searchService;
    }

    @Override
    public SearchRequest createFromParameters(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams actionParameters)
    {
        notNull("actionParameters", actionParameters);

        // Lets give the searchers a chance to get the clauses from the passed in parameter map
        List<Clause> clauses = getClausesFromSearchers(searchUser, actionParameters);

        // Either put all the clauses into an AND or use the top-level clause if there is only one
        final Clause clause = getClause(clauses);

        List<SearchSort> combinedSorts = combineSorts(oldSearchRequest, searchUser, actionParameters);

        Query newQuery = new QueryImpl(clause, new OrderByImpl(combinedSorts), null);

        final SearchRequest newSearchRequest = createNewSearchRequest(oldSearchRequest, searchUser);
        newSearchRequest.setQuery(newQuery);

        if (oldSearchRequest != null)
        {
            final boolean modified = !simpleSearchRequestsSameOrQueriesEquivalent(searchUser, oldSearchRequest, newSearchRequest);
            if (!modified)
            {
                newSearchRequest.setQuery(oldSearchRequest.getQuery());
            }
            newSearchRequest.setModified(modified || oldSearchRequest.isModified());
        }
        else
        {
            newSearchRequest.setModified(false);
        }

        return newSearchRequest;
    }

    public SearchRequest createFromQuery(final SearchRequest oldSearchRequest, final User searchUser, final Query query)
    {
        notNull("query", query);

        final SearchRequest searchRequest = createNewSearchRequest(oldSearchRequest, searchUser);

        // Always set the Query onto the SearchRequest, this handles the where clause and order by clause, this
        // will indicate that the SearchRequest is modified but we will fix that up next if it is not true.
        searchRequest.setQuery(query);

        // modified?
        if (oldSearchRequest != null && (oldSearchRequest.isModified() || !searchRequestsSameOrQueriesEquivalent(oldSearchRequest, searchRequest)))
        {
            searchRequest.setModified(true);
        }
        else
        {
            searchRequest.setModified(false);
        }
        return searchRequest;
    }

    /**
     * Combines sorts from the old search request, action parameters and additional JQL query (in the reverse order).
     * If sorts are not specified or empty, they are ignored.
     *
     * @param oldSearchRequest the old search request
     * @param searchUser the user
     * @param actionParameters the action parameters
     * @return the list of combined search sorts; should not be null.
     */
    List<SearchSort> combineSorts(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams actionParameters)
    {
        List<SearchSort> oldSearchRequestSorts = null;
        if (oldSearchRequest != null && oldSearchRequest.getQuery().getOrderByClause() != null && !oldSearchRequest.getQuery().getOrderByClause().getSearchSorts().isEmpty())
        {
            oldSearchRequestSorts = oldSearchRequest.getQuery().getOrderByClause().getSearchSorts();
        }

        // Get the search sorts from the parameters
        final OrderBy paramsOrderBy = searchSortUtil.getOrderByClause(actionParameters.getKeysAndValues());

        // the order of combining the sorts is:
        // oldSearchRequestSorts are last;
        // then Sorts from params;

        List<SearchSort> combinedSorts = paramsOrderBy.getSearchSorts();
        if (oldSearchRequestSorts != null)
        {
            combinedSorts = searchSortUtil.mergeSearchSorts(searchUser, combinedSorts, oldSearchRequestSorts, Integer.MAX_VALUE);
        }
        return combinedSorts;
    }

    SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
    {
        final SearchRequest searchRequest;
        if (oldSearchRequest != null && oldSearchRequest.isLoaded())
        {
            // Construct the new SearchRequest such that we will copy all of the old search requests attributes
            searchRequest = new SearchRequest(oldSearchRequest);
        }
        else
        {
            // Build the SearchRequest fresh
            searchRequest = new SearchRequest();
            searchRequest.setOwner(ApplicationUsers.from(searchUser));
        }
        return searchRequest;
    }

    /**
     * @deprecated Since 6.3.3, due to performance issues when calculating the query contexts on
     * the call to {@link #checkSimpleWhereClauses(com.atlassian.crowd.embedded.api.User, com.atlassian.query.Query, com.atlassian.query.Query)}
     */
    @Deprecated
    boolean simpleSearchRequestsSameOrQueriesEquivalent(final User user, final SearchRequest oldSearchRequest, final SearchRequest newSearchRequest)
    {
       if (!nonQueryAttributesEquals(newSearchRequest, oldSearchRequest))
        {
            return false;
        }
        else
        {
            Query oldQuery = oldSearchRequest.getQuery();
            Query query = newSearchRequest.getQuery();

            return checkOrderByClauses(oldQuery.getOrderByClause(), query.getOrderByClause()) && checkSimpleWhereClauses(user, oldSearchRequest.getQuery(), newSearchRequest.getQuery());
        }
    }

    private boolean checkSimpleWhereClauses(final User user, final Query oldQuery, final Query newQuery)
    {
        // we need to check if both queries imply the same query context, as this will affect population
        final QueryContext newQueryContext = searchService.getQueryContext(user, newQuery);
        final QueryContext oldQueryContext = searchService.getQueryContext(user, oldQuery);

        if (!newQueryContext.equals(oldQueryContext))
        {
            return false;
        }

        // we need to check if the simple search contexts are the same, as this will affect population
        final SearchContext newSearchContext = searchService.getSearchContext(user, newQuery);
        final SearchContext oldSearchContext = searchService.getSearchContext(user, oldQuery);

        if (!newSearchContext.equals(oldSearchContext))
        {
            return false;
        }

        FieldValuesHolder oldHolder = new FieldValuesHolderImpl();
        FieldValuesHolder newHolder = new FieldValuesHolderImpl();
        final Collection<IssueSearcher<?>> issueSearchers = issueSearcherManager.getAllSearchers();

        // Need an OSUser object for now
        for (IssueSearcher<?> issueSearcher : issueSearchers)
        {
            oldHolder.clear();
            newHolder.clear();

            issueSearcher.getSearchInputTransformer().populateFromQuery(user, newHolder, newQuery, newSearchContext);
            issueSearcher.getSearchInputTransformer().populateFromQuery(user, oldHolder, oldQuery, oldSearchContext);

            if (!holdersEqual(oldHolder, newHolder))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check that two {@link SearchRequest}s are the same (not taking into account JQL), or have equivalent JQL clauses.
     *
     * @param oldSearchRequest the old search request
     * @param searchRequest the new search request
     * @return true if they are the same or equivalent; false otherwise.
     */
    boolean searchRequestsSameOrQueriesEquivalent(final SearchRequest oldSearchRequest, final SearchRequest searchRequest)
    {
        if (!nonQueryAttributesEquals(searchRequest, oldSearchRequest))
        {
            return false;
        }
        else if (oldSearchRequest.getQuery().getQueryString() != null && searchRequest.getQuery().getQueryString() != null)
        {
            return oldSearchRequest.getQuery().getQueryString().equals(searchRequest.getQuery().getQueryString());
        }
        else
        {
            return  checkOrderByClauses(oldSearchRequest.getQuery().getOrderByClause(), searchRequest.getQuery().getOrderByClause()) &&
                    checkWhereClauses(oldSearchRequest.getQuery().getWhereClause(), searchRequest.getQuery().getWhereClause());
        }
    }

    /**
     * Checks equality between SearchRequests for every except new JQL logic
     *
     *
     * @param searchRequest the one to compare against
     * @param otherSearchRequest the other guy
     * @return true if the SearchRequests are equal in every way discounting JQL stuff; false otherwise
     */
    boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
    {
        if (searchRequest == null && otherSearchRequest == null)
        {
            return true;
        }
        else if (searchRequest == null || otherSearchRequest == null)
        {
            return false;
        }

        if (searchRequest == otherSearchRequest)
        {
            return true;
        }

        if (otherSearchRequest.getOwner() != null ? !otherSearchRequest.getOwner().equals(searchRequest.getOwner()) : searchRequest.getOwner() != null)
        {
            return false;
        }
        if (otherSearchRequest.getDescription() != null ? !otherSearchRequest.getDescription().equals(searchRequest.getDescription()) : searchRequest.getDescription() != null)
        {
            return false;
        }
        if (otherSearchRequest.isLoaded() != searchRequest.isLoaded())
        {
            return false;
        }
        if (otherSearchRequest.getName() != null ? !otherSearchRequest.getName().equals(searchRequest.getName()) : searchRequest.getName() != null)
        {
            return false;
        }
        return true;
    }

    boolean checkWhereClauses(final Clause oldWhereClause, final Clause newWhereClause)
    {
        if (oldWhereClause == null && newWhereClause == null)
        {
            return true;
        }
        else if (oldWhereClause == null || newWhereClause == null)
        {
            return false;
        }
        else
        {
            return checkClauseEquivalence(oldWhereClause, newWhereClause);
        }
    }

    boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
    {
        if (oldOrderByClause == null && newOrderByClause == null)
        {
            return true;
        }
        else if (oldOrderByClause == null || newOrderByClause == null)
        {
            return false;
        }
        else
        {
            return oldOrderByClause.equals(newOrderByClause);
        }
    }

    ///CLOVER:OFF
    boolean holdersEqual(final FieldValuesHolder oldHolder, final FieldValuesHolder newHolder)
    {
        return newHolder.equals(oldHolder);
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    boolean checkClauseEquivalence(final Clause oldClause, final Clause clause)
    {
        return new SimpleEquivalenceComparator().isEquivalent(oldClause, clause);
    }
    ///CLOVER:ON

    List<Clause> getClausesFromSearchers(final User searchUser, final ActionParams actionParams)
    {
        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        final Collection<IssueSearcher<?>> searchers = issueSearcherManager.getAllSearchers();

        // Need an OSUser object for now
        List<Clause> clauses = new ArrayList<Clause>();
        for (IssueSearcher<?> searcher : searchers)
        {
            final SearchInputTransformer searchInputTransformer = searcher.getSearchInputTransformer();
            searchInputTransformer.populateFromParams(searchUser, fieldValuesHolder, actionParams);
            final Clause clause = searchInputTransformer.getSearchClause(searchUser, fieldValuesHolder);
            if (clause != null)
            {
                clauses.add(clause);
            }
        }
        return clauses;
    }

    Clause getClause(final List<Clause> clauses)
    {
        final Clause clause;
        if (!clauses.isEmpty())
        {
            if (clauses.size() == 1)
            {
                clause = clauses.get(0);
            }
            else
            {
                clause = new AndClause(clauses);
            }
        }
        else
        {
            clause = null;
        }
        return clause;
    }
}
