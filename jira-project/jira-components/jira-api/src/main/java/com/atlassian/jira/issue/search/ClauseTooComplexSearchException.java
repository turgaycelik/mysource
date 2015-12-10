package com.atlassian.jira.issue.search;

import com.atlassian.query.clause.Clause;

/**
 * Thrown when a lucene is attempted to be built from a JQL query which is too complex.
 *
 * @since v4.0
 */
public class ClauseTooComplexSearchException extends SearchException
{
    private final Clause clause;

    public ClauseTooComplexSearchException(final Clause clause)
    {
        super("A the following query was too complex to generate a query from: " + clause.toString());
        this.clause = clause;
    }

    public Clause getClause()
    {
        return clause;
    }
}
