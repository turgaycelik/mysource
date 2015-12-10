package com.atlassian.jira.jql.resolver;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Looks up a saved filter via either id or name taking into account the user who is looking up the filter.
 *
 * @since v4.0
 */
public class SavedFilterResolver
{
    private final SearchRequestManager searchRequestManager;

    public SavedFilterResolver(final SearchRequestManager searchRequestManager)
    {
        this.searchRequestManager = notNull("searchRequestManager", searchRequestManager);
    }

    /**
     * Resolves {@link com.atlassian.jira.jql.operand.QueryLiteral}s into the {@link com.atlassian.jira.issue.search.SearchRequest}
     * objects that the user has permission to see.
     *
     * @param searcher the user performing the search
     * @param rawValues the query literals representing the search requests
     * @return the list of SearchRequests that the searcher can see that correspond to the literals. Never null, and
     * should not contain any null elements.
     */
    public List<SearchRequest> getSearchRequest(User searcher, List<QueryLiteral> rawValues)
    {
        return _getSearchRequests(ApplicationUsers.from(searcher), false, rawValues);
    }

    /**
     * Resolves {@link com.atlassian.jira.jql.operand.QueryLiteral}s into the {@link com.atlassian.jira.issue.search.SearchRequest}
     * objects. Permissions are ignored.
     *
     * @param rawValues the query literals representing the search requests
     * @return the list of SearchRequests that the searcher can see that correspond to the literals. Never null, and
     * should not contain any null elements.
     */
    public List<SearchRequest> getSearchRequestOverrideSecurity(List<QueryLiteral> rawValues)
    {
        return _getSearchRequests(null, true, rawValues);
    }

    private List<SearchRequest> _getSearchRequests(final ApplicationUser searcher, final boolean overrideSecurity, final List<QueryLiteral> rawValues)
    {
        List<SearchRequest> matchingFilters = new ArrayList<SearchRequest>();
        if (rawValues != null)
        {
            for (QueryLiteral rawValue : rawValues)
            {
                if (rawValue.getStringValue() != null)
                {
                    matchingFilters.addAll(getSearchRequestsForString(searcher, overrideSecurity, rawValue.getStringValue()));
                }
                else if (rawValue.getLongValue() != null)
                {
                    matchingFilters.addAll(getSearchRequestsForLong(searcher, overrideSecurity, rawValue.getLongValue()));
                }
                // else - we somehow got an Empty literal - empty is not allowed so ignore
            }
        }
        return matchingFilters;
    }

    private List<SearchRequest> getSearchRequestsForLong(final ApplicationUser searcher, final boolean overrideSecurity, final Long rawValue)
    {
        final List<SearchRequest> valuesMatchingFilters = new ArrayList<SearchRequest>();
        final SearchRequest request = getSearchRequestById(searcher, overrideSecurity, rawValue);
        if (request != null)
        {
            valuesMatchingFilters.add(request);
        }
        else
        {
            // Try to look up the filter by name
            valuesMatchingFilters.addAll(getSearchRequestsByName(searcher, overrideSecurity, rawValue.toString()));
        }
        return valuesMatchingFilters;
    }

    private List<SearchRequest> getSearchRequestsForString(final ApplicationUser searcher, final boolean overrideSecurity, final String rawValue)
    {
        List<SearchRequest> valuesMatchingFilters = getSearchRequestsByName(searcher, overrideSecurity, rawValue);
        if (valuesMatchingFilters.isEmpty())
        {
            final Long valueAsLong = getValueAsLong(rawValue);
            if (valueAsLong != null)
            {
                // Try to look up the filter by id
                final SearchRequest request = getSearchRequestById(searcher, overrideSecurity, valueAsLong);
                if (request != null)
                {
                    valuesMatchingFilters.add(request);
                }
            }
        }
        return valuesMatchingFilters;
    }

    private SearchRequest getSearchRequestById(final ApplicationUser searcher, final boolean overrideSecurity, final Long idValue)
    {
        if (!overrideSecurity)
        {
            return searchRequestManager.getSearchRequestById(searcher, idValue);
        }
        else
        {
            return searchRequestManager.getSearchRequestById(idValue);
        }
    }

    private List<SearchRequest> getSearchRequestsByName(final ApplicationUser searcher, final boolean overrideSecurity, final String nameValue)
    {
        final String trimName = StringUtils.trimToNull(nameValue);

        if (trimName != null)
        {
            if (!overrideSecurity)
            {
                // Make sure that we search for an exact name match
                final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().
                                setName(nameValue).
                                setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT).
                                setEntitySearchContext(SharedEntitySearchContext.USE);

                final SharedEntitySearchResult<SearchRequest> sharedEntitySearchResult =
                        searchRequestManager.search(builder.toSearchParameters(), searcher, 0, Integer.MAX_VALUE);

                return sharedEntitySearchResult.getResults();
            }
            else
            {
                return searchRequestManager.findByNameIgnoreCase(nameValue);
            }
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private Long getValueAsLong(final String singleValueOperand)
    {
        try
        {
            return new Long(singleValueOperand);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
