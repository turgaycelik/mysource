package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Test class for {@link com.atlassian.jira.jql.builder.SingleMutableClause}.
 *
 * @since v4.0
 */
public class TestSingleMutableClause
{
    @Test
    public void testCombine() throws Exception
    {
        SingleMutableClause clause1 = new SingleMutableClause(new TerminalClauseImpl("one", Operator.GREATER_THAN, "one"));
        SingleMutableClause clause2 = new SingleMutableClause(new TerminalClauseImpl("two", Operator.GREATER_THAN, "two"));
        SingleMutableClause clause3 = new SingleMutableClause(new TerminalClauseImpl("three", Operator.GREATER_THAN, "three"));

        assertEquals(clause1.combine(BuilderOperator.OR, clause2), new MultiMutableClause(BuilderOperator.OR, clause1, clause2));
        assertEquals(clause1.combine(BuilderOperator.AND, clause3), new MultiMutableClause(BuilderOperator.AND, clause1, clause3));
        assertEquals(clause1.combine(BuilderOperator.NOT, null), new NotMutableClause(clause1));
    }

    @Test
    public void testAsClause() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("one", Operator.GREATER_THAN, "one");
        SingleMutableClause mutableClause = new SingleMutableClause(clause);
        assertSame(clause, mutableClause.asClause());
    }

    @Test
    public void testCopy() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("one", Operator.GREATER_THAN, "one");
        final MutableClause mutableClause = new SingleMutableClause(clause);

        assertSame(mutableClause, mutableClause.copy());
    }

    @Test
    public void testToString() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("one", Operator.GREATER_THAN, 1L);
        final MutableClause mutableClause = new SingleMutableClause(clause);

        assertEquals(clause.toString(), mutableClause.toString());
    }
}
