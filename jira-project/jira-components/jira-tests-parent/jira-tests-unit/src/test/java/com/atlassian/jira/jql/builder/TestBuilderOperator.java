package com.atlassian.jira.jql.builder;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link BuilderOperator}.
 *
 * @since v4.0
 */
public class TestBuilderOperator
{
    @Test
    public void testOrder() throws Exception
    {
        List<BuilderOperator> expectedOrder = CollectionBuilder.newBuilder(BuilderOperator.LPAREN, BuilderOperator.RPAREN, BuilderOperator.OR,
            BuilderOperator.AND, BuilderOperator.NOT).asList();

        //Even though this is a set, its order is defined by the declaration order of the Enum.
        Set<BuilderOperator> actualOrder = EnumSet.allOf(BuilderOperator.class);
        assertEquals(expectedOrder.size(), actualOrder.size());

        final Iterator<BuilderOperator> actualIterator = actualOrder.iterator();
        for (BuilderOperator operator : expectedOrder)
        {
            assertEquals(operator, actualIterator.next());
        }
    }

    @Test
    public void testAndCombine() throws Exception
    {
        MutableClause left = new SingleMutableClause(new TerminalClauseImpl("dont", Operator.EQUALS, "care"));
        MutableClause right = new SingleMutableClause(new TerminalClauseImpl("carefactor", Operator.EQUALS, "0"));

        final MutableClause actualClause = BuilderOperator.AND.createClauseForOperator(left, right);
        assertEquals(new MultiMutableClause(BuilderOperator.AND, left, right), actualClause);
    }

    @Test
    public void testOrCombine() throws Exception
    {
        MutableClause left = new SingleMutableClause(new TerminalClauseImpl("dont", Operator.EQUALS, "care"));
        MutableClause right = new SingleMutableClause(new TerminalClauseImpl("carefactor", Operator.EQUALS, "0"));

        final MutableClause actualClause = BuilderOperator.OR.createClauseForOperator(left, right);
        assertEquals(new MultiMutableClause(BuilderOperator.OR, left, right), actualClause);
    }

    @Test
    public void testNotCombine() throws Exception
    {
        MutableClause left = new SingleMutableClause(new TerminalClauseImpl("dont", Operator.EQUALS, "care"));
        MutableClause right = new SingleMutableClause(new TerminalClauseImpl("carefactor", Operator.EQUALS, "0"));

        MutableClause actualClause = BuilderOperator.NOT.createClauseForOperator(left, right);
        assertEquals(new NotMutableClause(left), actualClause);

        actualClause = BuilderOperator.NOT.createClauseForOperator(left, null);
        assertEquals(new NotMutableClause(left), actualClause);
    }
}
