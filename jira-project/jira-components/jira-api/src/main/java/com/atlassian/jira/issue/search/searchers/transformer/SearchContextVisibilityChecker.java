package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.search.SearchContext;

import java.util.Collection;
import java.util.Set;

/**
 * A utility class for checking if values are visible under a given {@link SearchContext}.
 *
 * @since v4.0
 * @deprecated
 */
@Deprecated
public interface SearchContextVisibilityChecker
{
    /**
     * Filters out any ids in the given collection that are not visible under the {@link com.atlassian.jira.issue.search.SearchContext}
     *
     * @param searchContext the context to check to see if the domain object represented by the id is visible under
     * @param ids the collection of ids to filter
     * @return a new set which contains ids from the input collection that are visible under the given {@link com.atlassian.jira.issue.search.SearchContext} 
     */
    Set<String> FilterOutNonVisibleInContext(SearchContext searchContext, Collection<String> ids);
}
