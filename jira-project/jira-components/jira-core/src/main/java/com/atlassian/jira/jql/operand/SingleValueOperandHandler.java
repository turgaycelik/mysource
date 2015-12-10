package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.SingleValueOperand;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class SingleValueOperandHandler implements OperandHandler <SingleValueOperand>
{
    public MessageSet validate(final User searcher, final SingleValueOperand operand, final TerminalClause terminalClause)
    {
        return new MessageSetImpl();
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final SingleValueOperand operand, final TerminalClause terminalClause)
    {
        if (operand.getLongValue() == null)
        {
            return Collections.singletonList(new QueryLiteral(operand, operand.getStringValue()));
        }
        else
        {
            return Collections.singletonList(new QueryLiteral(operand, operand.getLongValue()));
        }
    }

    public boolean isList()
    {
        return false;
    }

    public boolean isEmpty()
    {
        return false;
    }

    public boolean isFunction()
    {
        return false;
    }
}
