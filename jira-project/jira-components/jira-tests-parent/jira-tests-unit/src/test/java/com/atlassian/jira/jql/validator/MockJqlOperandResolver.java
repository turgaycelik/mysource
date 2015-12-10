package com.atlassian.jira.jql.validator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.EmptyOperandHandler;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.MultiValueOperandHandler;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operand.SingleValueOperandHandler;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;

/**
 * Simple mock of {@link com.atlassian.jira.jql.operand.JqlOperandResolver} for the tests.
 *
 * @since v4.0
 */
public class MockJqlOperandResolver implements JqlOperandResolver
{
    private final Map<String, OperandHandler<?>> handlers;

    public MockJqlOperandResolver()
    {
        this(new LinkedHashMap<String, OperandHandler<?>>());
    }

    public MockJqlOperandResolver(Map<String, OperandHandler<?>>  handlers)
    {
        this.handlers = handlers;
    }

    public MockJqlOperandResolver addHandlers(final Map<String, OperandHandler<?>> handlers)
    {
        this.handlers.putAll(handlers);
        return this;
    }

    public MockJqlOperandResolver addHandler(String name, OperandHandler<?> handler)
    {
        this.handlers.put(name,  handler);
        return this;
    }

    @Override
    public MessageSet validate(User searcher, Operand operand, WasClause clause)
    {
        final OperandHandler operandHandler = handlers.get(operand.getName());
        if (operandHandler != null)
        {
            //return operandHandler.validate(searcher, operand, clause.asTerminalClause());
            return new MessageSetImpl();
        }
        else
        {
            return new MessageSetImpl();
        }
    }

    public List<QueryLiteral> getValues(final User searcher, final Operand operand, final TerminalClause terminalClause)
    {
        return getValues(new QueryCreationContextImpl(searcher), operand, terminalClause);
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final Operand operand, final TerminalClause terminalClause)
    {
        final OperandHandler operandHandler = handlers.get(operand.getName());
        if (operandHandler != null)
        {
            return operandHandler.getValues(queryCreationContext, operand, terminalClause);
        }
        else
        {
            return null;
        }
    }

    public MessageSet validate(final User user, final Operand operand, final TerminalClause terminalClause)
    {
        final OperandHandler operandHandler = handlers.get(operand.getName());
        if (operandHandler != null)
        {
            return operandHandler.validate(user, operand, terminalClause);
        }
        else
        {
            return new MessageSetImpl();
        }
    }

    public FunctionOperand sanitiseFunctionOperand(final User searcher, final FunctionOperand operand)
    {
        return operand;
    }

    public QueryLiteral getSingleValue(final User user, final Operand operand, final TerminalClause clause)
    {
        final List<QueryLiteral> list = getValues(user, operand, clause);
        if (list == null || list.isEmpty())
        {
            return null;
        }
        else if (list.size() > 1)
        {
            throw new IllegalArgumentException("Found more than one value in operand '" + operand + "'; values were: " + list);
        }
        else
        {
            return list.get(0);
        }
    }

    public boolean isEmptyOperand(final Operand operand)
    {
        final OperandHandler operandHandler = handlers.get(operand.getName());
        return operandHandler != null && operandHandler.isEmpty();
    }

    public boolean isFunctionOperand(final Operand operand)
    {
        final OperandHandler operandHandler = handlers.get(operand.getName());
        return operandHandler != null && operandHandler.isFunction();
    }

    public boolean isListOperand(final Operand operand)
    {
        final OperandHandler operandHandler = handlers.get(operand.getName());
        return operandHandler != null && operandHandler.isList();
    }

    public boolean isValidOperand(final Operand operand)
    {
        final OperandHandler operandHandler = handlers.get(operand.getName());
        return operandHandler != null;
    }

    public static MockJqlOperandResolver createSimpleSupport()
    {
        final MockJqlOperandResolver support = new MockJqlOperandResolver();
        final Map<String, OperandHandler<?>> handlers = new LinkedHashMap<String, OperandHandler<?>>();
        handlers.put("SingleValueOperand", new SingleValueOperandHandler());
        handlers.put("MultiValueOperand", new MultiValueOperandHandler(support));
        handlers.put("EMPTY", new EmptyOperandHandler());

        support.addHandlers(handlers);

        return support;
    }

}
