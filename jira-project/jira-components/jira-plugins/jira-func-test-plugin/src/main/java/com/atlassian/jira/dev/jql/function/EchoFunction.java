package com.atlassian.jira.dev.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple function that simply echos its input parameters as its output parameters.
 *
 * @since v4.0
 */
public class EchoFunction extends AbstractJqlFunction implements JqlFunction
{
    public static final String EMPTY_FLAG = "none";
    public static final String EMPTY_ESCAPED = "\"none\"";

    public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        return new MessageSetImpl();
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        List<QueryLiteral> literals = new ArrayList<QueryLiteral>(operand.getArgs().size());
        for (String argument : operand.getArgs())
        {
            if (argument.equals(EMPTY_FLAG))
            {
                literals.add(new QueryLiteral(operand));
            }
            else
            {
                literals.add(new QueryLiteral(operand, cleanArg(argument)));
            }
        }
        return literals;
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return 0;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.ALL;
    }

    private String cleanArg(String arg)
    {
        if (arg.equals(EMPTY_ESCAPED))
        {
            return EMPTY_FLAG;
        }
        else
        {
            return arg;
        }
    }
}

