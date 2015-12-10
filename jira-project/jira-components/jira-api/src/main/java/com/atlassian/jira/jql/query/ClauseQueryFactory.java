package com.atlassian.jira.jql.query;

import javax.annotation.Nonnull;

import com.atlassian.query.clause.TerminalClause;

/**
 * Provides the logic to build a lucene query for a {@link TerminalClause clause}.
 *
 * @since v4.0
 */
public interface ClauseQueryFactory
{
    /**
     * Generates a lucene query for the passed {@link TerminalClause}. It is the responsibility of the factory
     * to look at the Operator and Operand in the Termincal and generate a Lucene search for it. This method
     * is only called after JIRA works out that the TermincalClause is relevant to this ClauseQueryFactory.
     *
     * A ClauseFactory needs to be careful when implementing the NOT_LIKE, NOT_EQUALS or NOT_IN operators. These
     * negative operators should not match what is considered EMPTY. For example, the query
     * "resolution is EMPTY" will return all unresolved issues in JIRA. The query "resolution != fixed" will only return
     * all resolved issues that have not been resolved as "fixed", that is, it will not return any unresolved issues.
     * The user would have to enter the query "resolution != fixed or resolution is EMPTY" to find all issues that are
     * either unresolved or not resolved as "fixed".
     *
     * The ClauseQueryFactory must handle the situation when an invalid TerminalClause is passed. The
     * ClauseQueryFactory must return an empty Lucene Search if the passed TerminalClause is invalid.
     * Most importantly, ClauseQueryFactory may not throw an exception on the input of an invalid TermincalClause.
     *
     * @param queryCreationContext the context of the query creation call; used to indicate that permissions should be
     * ignored for "admin queries"
     * @param terminalClause the clause for which this factory is generating a query.
     * @return QueryFactoryResult contains the query that lucene can use to search and metadata about the query. Null
     *  cannot be returned.
     */
    @Nonnull
    QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause);
}
