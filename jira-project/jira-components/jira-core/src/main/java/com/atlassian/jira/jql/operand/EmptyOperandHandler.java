package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.EmptyOperand;

import java.util.Collections;
import java.util.List;

/**
 * Handles the {@link com.atlassian.query.operand.EmptyOperand}.
 *
 * @since v4.0
 */
public class EmptyOperandHandler implements OperandHandler <EmptyOperand>
{
    public MessageSet validate(final User searcher, final EmptyOperand operand, final TerminalClause terminalClause)
    {
        // We don't need to do any validation
        return new MessageSetImpl();
    }

    /*
     * Returns a single empty query literal.
     */
    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final EmptyOperand operand, final TerminalClause terminalClause)
    {
        return Collections.singletonList(new QueryLiteral(operand));
    }

    public boolean isList()
    {
        return false;
    }

    public boolean isEmpty()
    {
        return true;
    }

    public boolean isFunction()
    {
        return false;
    }
}
