package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.query.clause.TerminalClause;
import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link com.atlassian.jira.jql.context.ClauseContextFactory} that wraps another ClauseContextFactory to ensure
 * that the {@link #getQuery(QueryCreationContext, com.atlassian.query.clause.TerminalClause)} method
 * on the wrapped object is only called when this passed TerminalClause passes usage validation.
 *
 * @since v4.0
 */
public class ValidatingDecoratorQueryFactory implements ClauseQueryFactory
{
    private final OperatorUsageValidator validator;
    private final ClauseQueryFactory delegate;

    public ValidatingDecoratorQueryFactory(final OperatorUsageValidator validator, final ClauseQueryFactory delegate)
    {
        this.delegate = notNull("delegate", delegate);
        this.validator = notNull("validator", validator);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        if (validator.check(queryCreationContext.getQueryUser(), terminalClause))
        {
            return delegate.getQuery(queryCreationContext, terminalClause);
        }
        else
        {
            final Logger log = Logger.getLogger(getClass());
            log.debug("Unable to create query for clause '" + terminalClause + "': Invalid operator and argument combination.");
            return QueryFactoryResult.createFalseResult();
        }
    }
}
