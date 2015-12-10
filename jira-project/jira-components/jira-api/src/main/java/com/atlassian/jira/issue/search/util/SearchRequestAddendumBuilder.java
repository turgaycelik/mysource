package com.atlassian.jira.issue.search.util;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;

/**
 * Provides convenience methods to build new SearchRequest objects from existing ones, by appending additional clauses.
 *
 * @since v6.0
 */
public class SearchRequestAddendumBuilder
{
    /**
     * Clones the query within the supplied SearchRequest and appends an AND clause to it, delegating to the supplied
     * callback to provide the clause specifics.
     *
     * @param value The value to be added to the AND clause
     * @param searchRequest A SearchRequest containing the original query to be cloned and appended to
     * @param addendumCallback Provides the delegate methods for adding the clauses
     * @param <T> The value type
     * @return A SearchRequest containing the new query
     */
    public static <T> SearchRequest appendAndClause(final T value, final SearchRequest searchRequest, final AddendumCallback<T> addendumCallback)
    {
        if (searchRequest == null)
        {
            return null;
        }
        else
        {
            final JqlQueryBuilder newQueryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
            final JqlClauseBuilder clauseBuilder = newQueryBuilder.where().defaultAnd();

            appendItem(value, addendumCallback, clauseBuilder);

            return new SearchRequest(clauseBuilder.buildQuery());
        }
    }

    /**
     * Clones the query within the supplied SearchRequest and appends an AND NOT clause to it, delegating to the
     * supplied callback to provide the clause specifics.
     *
     * @param values The values to be added to the AND NOT clause
     * @param searchRequest A SearchRequest containing the original query to be cloned and appended to
     * @param addendumCallback Provides the delegate methods for adding the clauses
     * @param <T> The value type
     * @return A SearchRequest containing the new query
     */
    public static <T> SearchRequest appendAndNotClauses(final Iterable<? extends T> values, final SearchRequest searchRequest, final AddendumCallback<T> addendumCallback)
    {
        if (searchRequest == null)
        {
            return null;
        }
        else
        {
            final JqlQueryBuilder newQueryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
            final JqlClauseBuilder clauseBuilder = newQueryBuilder.where().and().not().defaultOr().sub();

            for (T value : values)
            {
                appendItem(value, addendumCallback, clauseBuilder);
            }
            clauseBuilder.endsub();

            return new SearchRequest(clauseBuilder.buildQuery());
        }
    }

    private static <T> void appendItem(T value, AddendumCallback<T> addendumCallback, JqlClauseBuilder clauseBuilder)
    {
        if (value != null)
        {
            addendumCallback.appendNonNullItem(value, clauseBuilder);
        }
        else
        {
            addendumCallback.appendNullItem(clauseBuilder);
        }
    }

    public interface AddendumCallback<T>
    {
        void appendNonNullItem(T value, JqlClauseBuilder clauseBuilder);

        void appendNullItem(JqlClauseBuilder clauseBuilder);
    }
}