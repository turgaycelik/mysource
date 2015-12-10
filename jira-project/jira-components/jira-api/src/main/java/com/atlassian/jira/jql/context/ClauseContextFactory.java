package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.query.clause.TerminalClause;

/**
 * Creates a {@link com.atlassian.jira.jql.context.ClauseContext} for the associated clause.
 *
 * @since v4.0
 */
public interface ClauseContextFactory
{
    /**
     * Generates a clause context for the associated handler. If the clause context could not be determined for any
     * reason, this will return the Global Clause Context.
     *
     * @param searcher the user who is performing the search
     * @param terminalClause the clause for which this factory is generating a context.
     * @return ClauseContext that contains the implied and explicit project and issue types that this
     * clause is in context for.
     */
    ClauseContext getClauseContext(User searcher, TerminalClause terminalClause);
}
