package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.resolver.StatusResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestStatusClauseQueryFactory
{
    @Test
    public void testUnsupportedOperator() throws Exception
    {
        final MockControl mockStatusResolverControl = MockClassControl.createControl(StatusResolver.class);
        final StatusResolver mockStatusResolver = (StatusResolver) mockStatusResolverControl.getMock();
        mockStatusResolverControl.replay();

        final SingleValueOperand singleValueOperand = new SingleValueOperand("testOperand");

        StatusClauseQueryFactory statusClauseQueryFactory = new StatusClauseQueryFactory(mockStatusResolver, MockJqlOperandResolver.createSimpleSupport());

        TerminalClause terminalClause = new TerminalClauseImpl("status", Operator.LESS_THAN, singleValueOperand);

        final QueryFactoryResult result = statusClauseQueryFactory.getQuery(null, terminalClause);
        assertEquals(QueryFactoryResult.createFalseResult(), result);

        mockStatusResolverControl.verify();
    }
}
