package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;

import java.util.Set;

/**
 * Provides methods for retreiving the Navigator or index representations of the values in a query clause, be they
 * index values, functions or string values.
 *
 * NOTE: this method will give back the values it finds in the named clauses and does not take into account
 * the affects of the surrounding logical search query (i.e. AND, NOT, OR clauses that the clause may be
 * contained in).
 *
 * @since v4.0
 */
public interface IndexedInputHelper
{
    /**
     * Retrieves the index values for the clauses in the {@link SearchRequest}. Function Operands are expanded to their
     * values.
     *
     * @param searcher the user running the search
     * @param jqlClauseNames the names of the clauses on which to retreive the values.
     * @param query the search criteria used to populate the field values holder.
     * @param searchContext the context under which the search is being performed
     * @return a set of strings containing the index values of the clause values. Never null.
     * @deprecated Since 6.3.4. The {@link com.atlassian.jira.issue.search.SearchContext} parameter is no longer needed. Use {@link #getAllIndexValuesForMatchingClauses(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.search.ClauseNames, com.atlassian.query.Query)}.
     */
    @Deprecated
    Set<String> getAllIndexValuesForMatchingClauses(User searcher, ClauseNames jqlClauseNames, Query query, SearchContext searchContext);

    /**
     * Retrieves the index values for the clauses in the {@link SearchRequest}. Function Operands are expanded to their
     * values.
     *
     * @param searcher the user running the search
     * @param jqlClauseNames the names of the clauses on which to retreive the values.
     * @param query the search criteria used to populate the field values holder.
     * @return a set of strings containing the index values of the clause values. Never null.
     */
    Set<String> getAllIndexValuesForMatchingClauses(User searcher, ClauseNames jqlClauseNames, Query query);

    /**
     * Retreives the navigator id values for the values in the clauses. If there is a flag associated with a function operand
     * then that flag is returned, otherwise the function operand is expanded to its index values.
     *
     * @param searcher the user running the search
     * @param jqlClauseNames the names of the clauses on which to retreive the values.
     * @param query the search criteria used to populate the field values holder.
     * @param searchContext the context under which the search is being performed
     * @return a set of strings containing the navigator values of the clause values. Never Null.
     * @deprecated Since 6.3.3. The {@link com.atlassian.jira.issue.search.SearchContext} parameter is no longer needed. Use {@link #getAllNavigatorValuesForMatchingClauses(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.search.ClauseNames, com.atlassian.query.Query)}
     */
    @Deprecated
    Set<String> getAllNavigatorValuesForMatchingClauses(User searcher, ClauseNames jqlClauseNames, Query query, final SearchContext searchContext);

    /**
     * Retreives the navigator id values for the values in the clauses. If there is a flag associated with a function operand
     * then that flag is returned, otherwise the function operand is expanded to its index values.
     *
     * @param searcher the user running the search
     * @param jqlClauseNames the names of the clauses on which to retreive the values.
     * @param query the search criteria used to populate the field values holder.
     * @return a set of strings containing the navigator values of the clause values. Never Null.
     */
    Set<String> getAllNavigatorValuesForMatchingClauses(User searcher, ClauseNames jqlClauseNames, Query query);

    /**
     * Converts a set of Navigator value strings into a Clause that will match at least one of the specified values for
     * the given field.
     * <p/>
     * Note: where possible, the helper should try to create a clause, even when the value strings do not make sense
     * in the given domain. That is, it is preferred that a non-validating clause gets created than no clause at all.
     *
     * @param jqlClauseName the name of the clause to generate
     * @param values a set of Navigator value strings; may contain flag values. May not be null.
     * @return a clause that will match any of the values specified; null if no values were specified.
     */
    Clause getClauseForNavigatorValues(String jqlClauseName, Set<String> values);
}
