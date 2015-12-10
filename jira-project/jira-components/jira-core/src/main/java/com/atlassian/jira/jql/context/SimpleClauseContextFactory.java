package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;

/**
 * A Clause context factory that returns a {@link com.atlassian.jira.jql.context.ClauseContext} with
 * all issue types and all projects.
 *
 * @since v4.0
 */
@InjectableComponent
public class SimpleClauseContextFactory implements ClauseContextFactory
{
    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        return ClauseContextImpl.createGlobalClauseContext();
    }
}
