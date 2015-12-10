package com.atlassian.jira.jql.query;

import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestComponentClauseQueryFactory extends MockControllerTestCase
{
    @Test
    public void testUnsupportedOperators() throws Exception
    {
        final Operator[] invalidOperators = { Operator.GREATER_THAN, Operator.GREATER_THAN_EQUALS, Operator.LESS_THAN, Operator.LESS_THAN_EQUALS, Operator.LIKE };

        final ProjectComponentManager componentManager = mockController.getMock(ProjectComponentManager.class);
        mockController.replay();

        final ComponentResolver componentResolver = new ComponentResolver(componentManager);

        final SingleValueOperand singleValueOperand = new SingleValueOperand("testOperand");

        for (Operator invalidOperator : invalidOperators)
        {
            ComponentClauseQueryFactory componentClauseQueryFactory = new ComponentClauseQueryFactory(componentResolver, MockJqlOperandResolver.createSimpleSupport());

            TerminalClause terminalClause = new TerminalClauseImpl("component", invalidOperator, singleValueOperand);

            final QueryFactoryResult result = componentClauseQueryFactory.getQuery(null, terminalClause);
            assertEquals(QueryFactoryResult.createFalseResult(), result);
        }
        
        mockController.verify();
    }
}
