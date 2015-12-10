package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.query.clause.TerminalClause;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link com.atlassian.jira.jql.context.ClauseContextFactory} that wraps another ClauseContextFactory to ensure
 * that the {@link #getClauseContext(User, com.atlassian.query.clause.TerminalClause)} method
 * on the wrapped object is only called when this passed TerminalClause passes usage validation.
 *
 * @since v4.0
 */
public final class ValidatingDecoratorContextFactory implements ClauseContextFactory
{
    private final OperatorUsageValidator usageValidator;
    private final ClauseContextFactory delegatingContextFactory;

    public ValidatingDecoratorContextFactory(final OperatorUsageValidator usageValidator, final ClauseContextFactory delegatingContextFactory)
    {
        this.usageValidator = notNull("usageValidator", usageValidator);
        this.delegatingContextFactory = notNull("delegatingContextFactory", delegatingContextFactory);
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        if (!usageValidator.check(searcher, terminalClause))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }
        else
        {
            return delegatingContextFactory.getClauseContext(searcher, terminalClause);
        }
    }
}
