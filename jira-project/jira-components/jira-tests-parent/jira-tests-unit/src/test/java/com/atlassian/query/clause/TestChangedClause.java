package com.atlassian.query.clause;

import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.local.Junit3ListeningTestCase;
import com.atlassian.query.history.TerminalHistoryPredicate;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

/**
 * @since v4.0
 */
public class TestChangedClause extends Junit3ListeningTestCase
{
    private SingleValueOperand singleValueOperand = new SingleValueOperand("test");



    public void testNeverContainsSubClauses() throws Exception
    {
        assertTrue(new ChangedClauseImpl("testField", Operator.CHANGED, null).getClauses().isEmpty());
    }

    public void testToString() throws Exception
    {
        final ChangedClauseImpl clause = new ChangedClauseImpl("testField", Operator.CHANGED, null);
        assertEquals("{testField changed}", clause.toString());
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
                return failVisitor();
            }

            @Override
            public Void visit(ChangedClause clause)
            {
                visitCalled.set(true);
                return null;
            }
        };
        new ChangedClauseImpl("testField", Operator.CHANGED, null).accept(visitor);
        assertTrue(visitCalled.get());
    }

    /**
     * JRA-36813: Regression test for this bug.
     */
    public void testToStringSeparatesOperandAndPredicateBySpace()
    {
        final ChangedClauseImpl changedClause = new ChangedClauseImpl("FieldX", Operator.CHANGED,
                new TerminalHistoryPredicate(Operator.ON, new SingleValueOperand(3500000L)));
        assertEquals("{FieldX changed on 3500000}", changedClause.toString());
    }

    private Void failVisitor()
    {
        fail("Should not be called");
        return null;
    }

}
