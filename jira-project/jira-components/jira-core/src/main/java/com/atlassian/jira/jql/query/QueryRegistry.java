package com.atlassian.jira.jql.query;

import com.atlassian.query.clause.TerminalClause;

import java.util.Collection;

/**
 * Used to map a {@link com.atlassian.query.clause.TerminalClause} to its associated
 * {@link com.atlassian.jira.jql.query.ClauseQueryFactory}s.
 *
 * @since v4.0
 */
public interface QueryRegistry
{
    /**
     * Fetches all associated ClauseQueryFactory objects for the provided TerminalClause. The returned value is based on
     * the clauses name the {@link com.atlassian.query.operator.Operator} that is associated with the
     * provided clause. Multiple values may be returned for custom fields.
     *
     * @param queryCreationContext the context for creating the query
     * @param clause that defines the name and operator for which we want to find the query factories, must not be null.
     * @return the query factories associated with this clause. The empty list will be returned to indicate failure.
     */
    Collection<ClauseQueryFactory> getClauseQueryFactory(QueryCreationContext queryCreationContext, TerminalClause clause);
}
