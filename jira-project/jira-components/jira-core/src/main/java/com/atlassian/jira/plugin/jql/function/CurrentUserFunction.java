package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Collections;
import java.util.List;

/**
 * Creates a value that is the current search user.
 *
 * @since v4.0
 */
public class CurrentUserFunction extends AbstractJqlFunction
{
    public static final String FUNCTION_CURRENT_USER = "currentUser";
    private static final int EXPECTED_ARGS = 0;

    public MessageSet validate(User searcher, FunctionOperand operand, final TerminalClause terminalClause)
    {
        return validateNumberOfArgs(operand, EXPECTED_ARGS);
    }

    public List<QueryLiteral> getValues(QueryCreationContext queryCreationContext, FunctionOperand operand, final TerminalClause terminalClause)
    {
        if (queryCreationContext == null || queryCreationContext.getQueryUser() == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.singletonList(new QueryLiteral(operand, queryCreationContext.getQueryUser().getName()));
        }
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return 0;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.USER;
    }
}
