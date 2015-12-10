package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * Base Class for all UserClauseQuery tests
 *
 * @since v6.2
 */
public abstract class TestUserClauseQueryFactory
{
    protected String fieldNameUnderTest;
    protected ClauseQueryFactory clauseQueryFactory;
    protected final UserResolver userResolver = Mockito.mock(UserResolver.class);


    @Test
    public void testUnsupportedOperators() throws Exception
    {
        final Operator[] invalidOperators = { Operator.GREATER_THAN, Operator.GREATER_THAN_EQUALS, Operator.LESS_THAN, Operator.LESS_THAN_EQUALS, Operator.LIKE };
        final SingleValueOperand singleValueOperand = new SingleValueOperand("testOperand");
        for (Operator invalidOperator : invalidOperators)
        {
            final TerminalClause terminalClause = new TerminalClauseImpl(fieldNameUnderTest, invalidOperator, singleValueOperand);
            final QueryFactoryResult result = clauseQueryFactory.getQuery(null, terminalClause);
            assertEquals(QueryFactoryResult.createFalseResult(), result);
        }
    }

    @Test
    public void testEmptyOperator() throws Exception
    {
        final TerminalClause terminalClause = new TerminalClauseImpl(fieldNameUnderTest, Operator.IN, new SingleValueOperand("EMPTY"));
        final QueryFactoryResult result = clauseQueryFactory.getQuery(null, terminalClause);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }
}
