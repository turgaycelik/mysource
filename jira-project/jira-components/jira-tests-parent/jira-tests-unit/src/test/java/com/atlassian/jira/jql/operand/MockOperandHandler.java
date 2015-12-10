package com.atlassian.jira.jql.operand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;

/**
 * Mock handler that can serve a list of values.
 *
 * @since v4.0
 */
public class MockOperandHandler<T extends Operand> implements OperandHandler<T>
{
    ///CLOVER:OFF

    private boolean listOperand;
    private final boolean emptyOperand;
    private final boolean functionOperand;
    private final List<QueryLiteral> literals = new ArrayList<QueryLiteral>();

    public MockOperandHandler()
    {
        this(false, false, false);
    }

    public MockOperandHandler(final boolean listOperand, final boolean emptyOperand, final boolean functionOperand)
    {
        this.listOperand = listOperand;
        this.emptyOperand = emptyOperand;
        this.functionOperand = functionOperand;
    }

    public MessageSet validate(final User searcher, final T operand, final TerminalClause terminalClause)
    {
        throw new UnsupportedOperationException("Not implemented in the mock");
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final T operand, final TerminalClause terminalClause)
    {
        return Collections.unmodifiableList(literals);
    }

    public MockOperandHandler add(String ... strings)
    {
        for (String string : strings)
        {
            literals.add(new QueryLiteral(new SingleValueOperand(string), string));
        }
        return this;
    }

    public MockOperandHandler add(long ... longs)
    {
        for (long l : longs)
        {
            literals.add(new QueryLiteral(new SingleValueOperand(l), l));
        }

        return this;
    }

    public MockOperandHandler add(QueryLiteral ... lits)
    {
        literals.addAll(Arrays.asList(lits));
        return this;
    }

    public MockOperandHandler clear()
    {
        literals.clear();
        return this;
    }

    public boolean isList()
    {
        return listOperand;
    }

    public boolean isEmpty()
    {
        return emptyOperand;
    }

    public boolean isFunction()
    {
        return functionOperand;
    }

    public MockOperandHandler setList(boolean listOperand)
    {
        this.listOperand = listOperand;
        return this;
    }

    @Override
    public String toString()
    {
        return "Mock literals " + literals;
    }

    ///CLOVER:ON
}
