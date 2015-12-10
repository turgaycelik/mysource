package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.jql.builder.MultiMutableClause}.
 *
 * @since v4.0
 */
public class TestMultiMutableClause
{
    @Test
    public void testConstructor() throws Exception
    {
        final MutableClause clause = new SingleMutableClause(new TerminalClauseImpl("who", Operator.LESS_THAN, "cares?"));

        try
        {
            new MultiMutableClause(null, clause);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new MultiMutableClause(BuilderOperator.NOT, clause);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new MultiMutableClause(BuilderOperator.AND);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new MultiMutableClause(BuilderOperator.OR, clause, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testCombine() throws Exception
    {
        final MutableClause clause = new SingleMutableClause(new TerminalClauseImpl("who", Operator.LESS_THAN, "cares?"));
        final MutableClause clause2 = new SingleMutableClause(new TerminalClauseImpl("not", Operator.GREATER_THAN, "me"));
        final MutableClause clause3 = new SingleMutableClause(new TerminalClauseImpl("never", Operator.NOT_EQUALS, "true"));
        final MutableClause clause4 = new SingleMutableClause(new TerminalClauseImpl("life", Operator.LIKE, "death"));
        final MutableClause clause5 = new SingleMutableClause(new TerminalClauseImpl("manager", Operator.LESS_THAN, "monkey"));

        MutableClause expectedClause = new MultiMutableClause(BuilderOperator.AND, clause, clause2);

        MutableClause actualClause = new MultiMutableClause(BuilderOperator.AND, clause);
        actualClause = actualClause.combine(BuilderOperator.AND, clause2);
        assertEquals(expectedClause, actualClause);

        expectedClause = new MultiMutableClause(BuilderOperator.AND, clause, clause2, clause3);
        actualClause = actualClause.combine(BuilderOperator.AND, clause3);
        assertEquals(expectedClause, actualClause);

        expectedClause = new MultiMutableClause(BuilderOperator.AND, clause, clause2, clause3);
        expectedClause = new MultiMutableClause(BuilderOperator.OR, expectedClause, clause4);
        actualClause = actualClause.combine(BuilderOperator.OR, clause4);
        assertEquals(expectedClause, actualClause);

        expectedClause = new MultiMutableClause(BuilderOperator.AND, clause, clause2, clause3);
        expectedClause = new MultiMutableClause(BuilderOperator.OR, expectedClause, clause4, clause5);
        actualClause = actualClause.combine(BuilderOperator.OR, clause5);
        assertEquals(expectedClause, actualClause);

        expectedClause = new MultiMutableClause(BuilderOperator.AND, clause, clause2, clause3);
        expectedClause = new NotMutableClause(new MultiMutableClause(BuilderOperator.OR, expectedClause, clause4, clause5));
        actualClause = actualClause.combine(BuilderOperator.NOT, null);
        assertEquals(expectedClause, actualClause);
    }

    @Test
    public void testAsClause() throws Exception
    {
        final Clause whoCaresClause = new TerminalClauseImpl("who", Operator.LESS_THAN, "cares?");
        final Clause lessMe = new TerminalClauseImpl("not", Operator.GREATER_THAN, "me");
        final MutableClause clause = new SingleMutableClause(whoCaresClause);
        final MutableClause clause2 = new SingleMutableClause(lessMe);
        final MutableClause clause3 = new MockMutableClause(null);

        MutableClause mutableClause = new MultiMutableClause(BuilderOperator.AND, clause);
        assertSame(whoCaresClause, mutableClause.asClause());

        mutableClause = new MultiMutableClause(BuilderOperator.OR, clause);
        assertSame(whoCaresClause, mutableClause.asClause());

        mutableClause = new MultiMutableClause(BuilderOperator.AND, clause, clause2);
        assertEquals(new AndClause(whoCaresClause, lessMe), mutableClause.asClause());

        mutableClause = new MultiMutableClause(BuilderOperator.OR, clause, clause2);
        assertEquals(new OrClause(whoCaresClause, lessMe), mutableClause.asClause());

        mutableClause = new MultiMutableClause(BuilderOperator.OR, clause3);
        assertNull(mutableClause.asClause());

        mutableClause = new MultiMutableClause(BuilderOperator.AND, clause3, clause2);
        assertEquals(lessMe, mutableClause.asClause());

        mutableClause = new MultiMutableClause(BuilderOperator.AND, clause3, clause2, clause);
        assertEquals(new AndClause(lessMe, whoCaresClause), mutableClause.asClause());
    }

    @Test
    public void testCopyWithAnd() throws Exception
    {
        IMocksControl control = createControl();

        final MockMutableClause stringClause1 = new MockMutableClause(null);
        final MutableClause clause1 = control.createMock(MutableClause.class);
        expect(clause1.copy()).andReturn(stringClause1).times(2);

        final MutableClause clause2 = control.createMock(MutableClause.class);
        final MockMutableClause stringClause2 = new MockMutableClause(null);
        expect(clause2.copy()).andReturn(stringClause2);

        final MutableClause clause3 = control.createMock(MutableClause.class);
        final MockMutableClause stringClause3 = new MockMutableClause(null);
        expect(clause3.copy()).andReturn(stringClause3);

        control.replay();

        MultiMutableClause multiClause = new MultiMutableClause(BuilderOperator.AND, clause1, clause2, clause3);
        MutableClause actualCopy = multiClause.copy();
        MutableClause expectedCopy = new MultiMutableClause(BuilderOperator.AND, stringClause1, stringClause2, stringClause3);

        assertEquals(expectedCopy, actualCopy);

        multiClause = new MultiMutableClause(BuilderOperator.OR, clause1);
        actualCopy = multiClause.copy();
        expectedCopy = new MultiMutableClause(BuilderOperator.OR, stringClause1);

        assertEquals(expectedCopy, actualCopy);
        
        control.verify();
    }
}
