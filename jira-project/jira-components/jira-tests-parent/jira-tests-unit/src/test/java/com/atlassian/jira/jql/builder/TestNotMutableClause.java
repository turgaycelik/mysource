package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Test for {@link com.atlassian.jira.jql.builder.NotMutableClause}.
 *
 * @since v4.0
 */
public class TestNotMutableClause
{
    @Test
    public void testCombine() throws Exception
    {
        NotMutableClause clause1 = new NotMutableClause(new SingleMutableClause(new TerminalClauseImpl("one", Operator.GREATER_THAN, "one")));
        NotMutableClause clause2 = new NotMutableClause(new SingleMutableClause(new TerminalClauseImpl("two", Operator.GREATER_THAN, "two")));
        NotMutableClause clause3 = new NotMutableClause(new SingleMutableClause(new TerminalClauseImpl("three", Operator.GREATER_THAN, "three")));

        assertEquals(clause1.combine(BuilderOperator.OR, clause2), new MultiMutableClause(BuilderOperator.OR, clause1, clause2));
        assertEquals(clause1.combine(BuilderOperator.AND, clause3), new MultiMutableClause(BuilderOperator.AND, clause1, clause3));
        assertEquals(clause1.combine(BuilderOperator.NOT, null), new NotMutableClause(clause1));
    }

    @Test
    public void testAsClause() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("one", Operator.GREATER_THAN, "one");
        NotMutableClause mutableClause = new NotMutableClause(new SingleMutableClause(clause));
        assertEquals(new NotClause(clause), mutableClause.asClause());

        mutableClause = new NotMutableClause(new MockMutableClause(null));
        assertNull(mutableClause.asClause());
    }

    @Test
    public void testCopyNoCopy() throws Exception
    {
        final MutableClause mockClause = createMock(MutableClause.class);
        expect(mockClause.copy()).andReturn(mockClause);

        replay(mockClause);

        NotMutableClause notMutableClause = new NotMutableClause(mockClause);
        assertSame(notMutableClause, notMutableClause.copy());

        verify(mockClause);
    }

    @Test
    public void testCopyRealCopy() throws Exception
    {
        final MutableClause mockClause = createMock(MutableClause.class);
        expect(mockClause.copy()).andReturn(new MockMutableClause(null));

        replay(mockClause);

        final NotMutableClause notMutableClause = new NotMutableClause(mockClause);
        final MutableClause actualCopy = notMutableClause.copy();

        assertNotSame(notMutableClause, actualCopy);
        assertEquals(new NotMutableClause(new MockMutableClause(null)), actualCopy);

        verify(mockClause);
    }

    @Test
    public void testToString() throws Exception
    {
        final MutableClause clause = new NotMutableClause(new MockMutableClause(null, "qwerty"));

        assertEquals("NOT(qwerty)", clause.toString());
    }
}
