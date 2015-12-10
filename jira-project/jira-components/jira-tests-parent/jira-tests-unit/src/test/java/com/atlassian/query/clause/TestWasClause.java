package com.atlassian.query.clause;

import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.local.Junit3ListeningTestCase;
import com.atlassian.query.history.TerminalHistoryPredicate;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Assert;

/**
 * @since v4.0
 */
public class TestWasClause extends Junit3ListeningTestCase
{
    private SingleValueOperand singleValueOperand = new SingleValueOperand("test");

    public void testNullPredicateConstructorArgument() throws Exception
    {
        try
        {
            WasClauseImpl wasClause = new WasClauseImpl("testField", Operator.WAS, singleValueOperand,null);
            Assert.assertNotNull("Can  create a WasClause with a null predicate",wasClause);
        }
        catch (Exception e)
        {
            // expected
        }
    }

    public void testNullOperandConstructorArgument() throws Exception
    {
        try
        {
            new WasClauseImpl("testField", null, null, null);
            fail("Can not create a WasClause with a null Operand");
        }
        catch (Exception e)
        {
            // expected
        }
    }

    public void testNullFieldConstructorArgument() throws Exception
    {
        try
        {
            new WasClauseImpl(null,null, singleValueOperand, null);
            fail("Can not create a WasClause with a null field");
        }
        catch (Exception e)
        {
            // expected
        }
    }

    public void testNeverContainsSubClauses() throws Exception
    {
        assertTrue(new WasClauseImpl("testField", Operator.WAS, singleValueOperand, null).getClauses().isEmpty());
    }

    public void testToString() throws Exception
    {
        final WasClauseImpl clause = new WasClauseImpl("testField", Operator.WAS, singleValueOperand, null);
        assertEquals("{testField was \"test\"}", clause.toString());
    }

    public void testVisit() throws Exception
    {
        final AtomicBoolean visitCalled = new AtomicBoolean(false);
        ClauseVisitor<Void> visitor = new ClauseVisitor<Void>()
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
                return failVisitor();
            }

            @Override
            public Void visit(WasClause clause)
            {
                visitCalled.set(true);
                return null;
            }

            @Override
            public Void visit(ChangedClause clause)
            {
                return failVisitor();
            }
        };
        new WasClauseImpl("testField",Operator.WAS, singleValueOperand, null).accept(visitor);
        assertTrue(visitCalled.get());
    }

    /**
     * JRA-36813: Regression test for this bug.
     */
    public void testToStringSeparatesOperandAndPredicateBySpace()
    {
        final WasClauseImpl wasClause = new WasClauseImpl("status", Operator.WAS, new SingleValueOperand("closed"),
                    new TerminalHistoryPredicate(Operator.ON, new SingleValueOperand(3500000L)));
        assertEquals("{status was \"closed\" on 3500000}", wasClause.toString());
    }

    private Void failVisitor()
    {
        fail("Should not be called");
        return null;
    }

}
