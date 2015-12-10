package com.atlassian.jira.jql.operand;

import java.util.Arrays;
import java.util.List;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;

import org.mockito.Mockito;

/**
 * Mock for {@link com.atlassian.jira.jql.operand.FunctionOperandHandler}.
 *
 * @since v4.0
 */
public class MockFunctionOperandHandler extends FunctionOperandHandler
{
    private List<Long> values;

    public MockFunctionOperandHandler(Long... values)
    {
        super((JqlFunction) DuckTypeProxy.getProxy(JqlFunction.class, new Object()), Mockito.mock(I18nHelper.class));
        this.values = Arrays.asList(values);
    }

    @Override
    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        return CollectionUtil.transform(values, new Function<Long, QueryLiteral>()
        {
            public QueryLiteral get(final Long input)
            {
                return new QueryLiteral(new SingleValueOperand(input), input);
            }
        });
    }

    @Override
    public boolean isList()
    {
        return super.isList();
    }
}
