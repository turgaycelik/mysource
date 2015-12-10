package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;

/**
 * A No-Op sanitiser that simply returns the input clause.
 *
 * @since v4.0
 */
public final class NoOpClauseSanitiser implements ClauseSanitiser
{
    public static final NoOpClauseSanitiser NOOP_CLAUSE_SANITISER = new NoOpClauseSanitiser();

    // shouldn't need construction
    private NoOpClauseSanitiser()
    {
    }

    public Clause sanitise(final User user, final TerminalClause clause)
    {
        return clause;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }
}
