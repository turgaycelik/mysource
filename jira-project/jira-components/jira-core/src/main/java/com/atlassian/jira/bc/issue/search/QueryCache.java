package com.atlassian.jira.bc.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import java.util.Collection;
import java.util.List;

/**
 * The query cache is a request level cache that stores the result of expensive query operations.
 *
 * The cache is indexed with Query User pairs.
 *
 * @since v4.0
 */
@InjectableComponent
public interface QueryCache
{
    /**
     * Retrieve the result of the last doesQueryFitFiterForm operation in the current thread.
     * for the {@link User} {@link com.atlassian.query.Query} pair.
     *
     * @param searcher the user who is performing the search
     * @param query the query for which to find the result for; cannot be null.
     * @return the last result of the doesQueryFitFiterForm operation for the
     * {@link User} {@link com.atlassian.query.Query} pair in the current thread, or null if
     * the operation has yet to be performed.
     */
    Boolean getDoesQueryFitFilterFormCache(User searcher, Query query);

    /**
     * Set the cached result of a doesQueryFitFiterForm operation on the
     * {@link User} {@link com.atlassian.query.Query} pair. The cache result
     * is only held for the current thread.
     *
     * @param searcher the user who is performing the search
     * @param query the query for which to store the result under; cannot be null
     * @param doesItFit the result of a doesSearchRequestFitNavigator operation for the.
     * {@link User} {@link com.atlassian.query.Query}
     */
    void setDoesQueryFitFilterFormCache(User searcher, Query query, boolean doesItFit);

    /**
     * Retrieve the result of the last getQueryContext operation in the current thread
     * for the {@link com.atlassian.crowd.embedded.api.User} {@link com.atlassian.query.Query} pair.
     *
     * @param searcher the user who is performing the search
     * @param query the query for which to find the result for; cannot be null.
     * @return the last result of the getQueryContext operation for the
     * {@link User} {@link com.atlassian.query.Query} pair in the current thread, or null if
     * the operation has yet to be performed.
     */
    QueryContext getQueryContextCache(User searcher, Query query);

    /**
     * Set the cached result of a getQueryContext operation on the
     * {@link User} {@link com.atlassian.query.Query} pair. The cache result
     * is only held for the current thread.
     *
     * @param searcher the user who is performing the search
     * @param query the query for which to store the result under; cannot be null.
     * @param queryContext the queryContext result to store
     * {@link User} {@link com.atlassian.query.Query}
     */
    void setQueryContextCache(User searcher, Query query, QueryContext queryContext);

    /**
     * Retrieve the result of the last getSimpleQueryContext operation in the current thread
     * for the {@link User} {@link com.atlassian.query.Query} pair.
     *
     * @param searcher the user who is performing the search
     * @param query the query for which to find the result for; cannot be null.
     * @return the last result of the getSimpleQueryContext operation for the
     * {@link User} {@link com.atlassian.query.Query} pair in the current thread, or null if
     * the operation has yet to be performed.
     */
    QueryContext getSimpleQueryContextCache(User searcher, Query query);

    /**
     * Set the cached result of a getSimpleQueryContext operation on the
     * {@link User} {@link com.atlassian.query.Query} pair. The cache result
     * is only held for the current thread.
     *
     * @param searcher the user who is performing the search
     * @param query the query for which to store the result under; cannot be null.
     * @param queryContext the querySimpleContext result to store
     * {@link User} {@link com.atlassian.query.Query}
     */
    void setSimpleQueryContextCache(User searcher, Query query, QueryContext queryContext);

    /**
     * Retrieve the collection of {@link com.atlassian.jira.jql.ClauseHandler}s registered
     * for the {@link User} jqlClauseName pair.
     *
     * @param searcher the user who is performing the search
     * @param jqlClauseName the jQLClauseName for which to find the result for; cannot be null.
     * @return the collection of {@link com.atlassian.jira.jql.ClauseHandler}s registered
     * for the {@link User} jqlClauseName pair.
     */
    Collection<ClauseHandler> getClauseHandlers(User searcher,  String jqlClauseName);

    /**
     * Set the cached result of a getSimpleQueryContext operation on the
     * {@link User} {@link com.atlassian.query.Query} pair. The cache result
     * is only held for the current thread.
     *
     * @param searcher the user who is performing the search
     * @param jqlClauseName the jQLClauseName for which to store the result under; cannot be null.
     * @param clauseHandlers the collection of {@link com.atlassian.jira.jql.ClauseHandler}s
     * {@link User} {@link com.atlassian.jira.jql.ClauseHandler}
     */
    void setClauseHandlers(User searcher,  String jqlClauseName, Collection<ClauseHandler> clauseHandlers);

    /**
     * Retrieve the list of {@link QueryLiteral}s registered
     * for the {@link QueryCreationContext} {@link Operand} jqlClause triplet.
     *
     *
     * @param context the query context of the search, which cannot be null.
     * @param operand the Operand which cannot be null
     * @param jqlClause the jQLClause for which to find the result for; cannot be null.
     * @return the list of {@link QueryLiteral}s registered
     * for the {@link QueryCreationContext} jqlClause pair.
     */
    List<QueryLiteral> getValues(QueryCreationContext context, Operand operand, TerminalClause jqlClause);

    /**
     * Set the cached result of a getValues operation on the
     * for the {@link QueryCreationContext} {@link Operand} jqlClause triplet. The cache result
     * is only held for the current thread.
     *
     * @param context the query context the search is being performed in
     * @param operand the Operand which cannot be null
     * @param jqlClause the jQLClause for which to store the result under; cannot be null.
     * @param values the collection of {@link QueryLiteral}s
     */
    void setValues(QueryCreationContext context, Operand operand, TerminalClause jqlClause, List<QueryLiteral> values);
}
