package com.atlassian.jira.mock.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.context.ClauseContext;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.query.clause.TerminalClause;

/**
 * @since v4.0
 */
public class MockClauseContextFactory implements ClauseContextFactory
{
    private final MockClauseContext clauseContext;

    public MockClauseContextFactory()
    {
        this.clauseContext = new MockClauseContext();
    }

    public MockClauseContextFactory(MockClauseContext clauseContext)
    {
        this.clauseContext = clauseContext;
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        return this.clauseContext;
    }
}
