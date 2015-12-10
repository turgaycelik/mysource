package com.atlassian.query.clause;

import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.fugue.Option;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestTerminalClauseImpl
{
    private final SingleValueOperand singleValueOperand = new SingleValueOperand("test");

    @Test
    public void testNullOperatorConstructorArgument() throws Exception
    {
        try
        {
            new TerminalClauseImpl("testField", null, singleValueOperand);
            fail("Can not create a TerminalClause with a null Operator");
        }
        catch (final Exception e)
        {
            // expected
        }
    }

    @Test
    public void testNullOperandConstructorArgument() throws Exception
    {
        try
        {
            new TerminalClauseImpl("testField", Operator.EQUALS, (SingleValueOperand)null);
            fail("Can not create a TerminalClause with a null Operand");
        }
        catch (final Exception e)
        {
            // expected
        }
    }

    @Test
    public void testNullNameConstructorArgument() throws Exception
    {
        try
        {
            new TerminalClauseImpl(null, Operator.EQUALS, singleValueOperand);
            fail("Can not create a TerminalClause with a null name");
        }
        catch (final Exception e)
        {
            // expected
        }
    }

    @Test
    public void testNeverContainsSubClauses() throws Exception
    {
        assertTrue(new TerminalClauseImpl("testField", Operator.EQUALS, singleValueOperand).getClauses().isEmpty());
    }

    @Test
    public void testToString() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.EQUALS, singleValueOperand);
        assertEquals("{testField = \"test\"}", clause.toString());
    }
    @Test
    public void testToStringWithProperties() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.EQUALS, singleValueOperand,
                Option.<Property>some(new Property(ImmutableList.of("k1","k2"),ImmutableList.of("ref1","ref2"))));
        assertEquals("{testField[k1.k2].ref1.ref2 = \"test\"}", clause.toString());
    }

    @Test
    public void testVisit() throws Exception
    {
        final AtomicBoolean visitCalled = new AtomicBoolean(false);
        final ClauseVisitor<Void> visitor = new ClauseVisitor<Void>()
        {
            public Void visit(final AndClause andClause)
            {
                return failVisitor();
            }

            public Void visit(final NotClause notClause)
            {
                return failVisitor();
            }

            public Void visit(final OrClause orClause)
            {
                return failVisitor();
            }

            public Void visit(final TerminalClause clause)
            {
                visitCalled.set(true);
                return null;
            }

            @Override
            public Void visit(WasClause clause)
            {
                return failVisitor();
            }

            @Override
            public Void visit(ChangedClause clause)
            {
                return failVisitor();
            }
        };
        new TerminalClauseImpl("testField", Operator.EQUALS, singleValueOperand).accept(visitor);
        assertTrue(visitCalled.get());
    }

    private Void failVisitor()
    {
        fail("Should not be called");
        return null;
    }

}
