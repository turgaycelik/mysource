package com.atlassian.jira.issue.search;

/**
 * This interface describes operations for appending clauses to existing SearchRequest queries.
 *
 * @since v6.0
 */
public interface SearchRequestAppender<T>
{
    /**
     * Append a single AND clause to the given SearchRequest, specifying that the given single value should be
     * included.
     *
     * @param value The value used to populate the new clause.
     * @param searchRequest The existing query to be used as the basis for the new query.   Will not be modified.
     * @return A new SearchRequest containing the modified query, or null if the new clause cannot be added.
     */
    SearchRequest appendInclusiveSingleValueClause(T value, SearchRequest searchRequest);

    /**
     * Append a multi-value clause to the given SearchRequest, specifying that the given values should not be included.
     *
     * @param values The values used to populate the new clause
     * @param searchRequest The existing query to be used as the basis for the new query.   Will not be modified.
     * @return A new SearchRequest containing the modified query, or null if the new clause cannot be added.
     */
    SearchRequest appendExclusiveMultiValueClause(Iterable<? extends T> values, SearchRequest searchRequest);


    interface Factory<T>
    {
        SearchRequestAppender<T> getSearchRequestAppender();
    }
}