package com.atlassian.jira.plugin.jql.function;

import com.atlassian.core.util.Clock;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Collections;
import java.util.List;

/**
 * Function that produces the current date as the value.
 *
 * @since v4.0
 */
public class NowFunction extends AbstractDateFunction
{
    public static final String FUNCTION_NOW = "now";
    
    NowFunction(final Clock clock, final TimeZoneManager timeZoneManager)
    {
        super(clock, timeZoneManager);
    }

    public NowFunction(final TimeZoneManager timeZoneManager)
    {
        super(timeZoneManager);
    }

    public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        // Now is always now so there is not much to validate
        return validateNumberOfArgs(operand, 0);
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        return Collections.singletonList(new QueryLiteral(operand, clock.getCurrentDate().getTime()));
    }

}
