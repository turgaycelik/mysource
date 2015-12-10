package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.util.profiling.UtilTimerStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.0
 */
public class MultiValueOperandHandler implements OperandHandler<MultiValueOperand>
{
    private final JqlOperandResolver operandResolver;

    public MultiValueOperandHandler(JqlOperandResolver operandResolver)
    {
        this.operandResolver = operandResolver;
    }

    public MessageSet validate(final User searcher, final MultiValueOperand operand, final TerminalClause terminalClause)
    {
        MessageSet messages = new MessageSetImpl();
        for (Operand subOperand : operand.getValues())
        {
            final MessageSet subMessageSet = operandResolver.validate(searcher, subOperand, terminalClause);
            if (subMessageSet.hasAnyErrors())
            {
                messages.addMessageSet(subMessageSet);
            }
        }
        return messages;
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final MultiValueOperand operand, final TerminalClause terminalClause)
    {
        UtilTimerStack.push("MultiValueOperandHandler.getValues()");
        List<QueryLiteral> valuesList = new ArrayList<QueryLiteral>();
        for (Operand subOperand : operand.getValues())
        {
            final List<QueryLiteral> vals = operandResolver.getValues(queryCreationContext, subOperand, terminalClause);
            if (vals != null)
            {
                valuesList.addAll(vals);
            }
        }
        UtilTimerStack.pop("MultiValueOperandHandler.getValues()");
        return valuesList;
    }

    public boolean isList()
    {
        return true;
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
