package com.atlassian.query.clause;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestAndClause
{
    private Clause mockClause;

    @Before
    public void setUp() throws Exception
    {
        final MockControl mockClauseControl = MockControl.createStrictControl(Clause.class);
        mockClause = (Clause) mockClauseControl.getMock();
        mockClauseControl.replay();
    }

    @After
    public void tearDown() throws Exception
    {
        mockClause = null;
    }

    @Test
    public void testNullConstructorArguments() throws Exception
    {
        try
        {
            new AndClause((Clause)null);
            fail("Should not be able to construct an AndClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
        try
        {
            new AndClause(mockClause, null);
            fail("Should not be able to construct an AndClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
        try
        {
            new AndClause(null, mockClause);
            fail("Should not be able to construct an AndClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
        try
        {
            new AndClause(null, null);
            fail("Should not be able to construct an AndClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
        try
        {
            new AndClause((List<Clause>)null);
            fail("Should not be able to construct an AndClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
        try
        {
            new AndClause(Collections.<Clause>emptyList());
            fail("Should not be able to construct an AndClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
    }

    @Test
    public void testName() throws Exception
    {
        assertEquals("AND", new AndClause(mockClause).getName());
    }

    @Test
    public void testToString() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        TerminalClause terminalClause2 = new TerminalClauseImpl("anotherField", Operator.GREATER_THAN, new SingleValueOperand("other"));
        AndClause andClause = new AndClause(terminalClause1, terminalClause2);
        assertEquals("{testField = \"test\"} AND {anotherField > \"other\"}", andClause.toString());
    }

    @Test
    public void testToStringWithPrecedence() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        TerminalClause terminalClause2 = new TerminalClauseImpl("anotherField", Operator.GREATER_THAN, new SingleValueOperand("other"));
        NotClause notClause = new NotClause(new TerminalClauseImpl("thirdField", Operator.GREATER_THAN, new SingleValueOperand("other")));
        OrClause orClause = new OrClause(new TerminalClauseImpl("fourthField", Operator.GREATER_THAN, new SingleValueOperand("other")),
                new TerminalClauseImpl("fifthField", Operator.GREATER_THAN, new SingleValueOperand("other")));
        AndClause andClause = new AndClause(terminalClause1, terminalClause2, notClause, orClause);
        assertEquals("{testField = \"test\"} AND {anotherField > \"other\"} AND NOT {thirdField > \"other\"} AND ( {fourthField > \"other\"} OR {fifthField > \"other\"} )", andClause.toString());
    }

    @Test
    public void testToStringWithPrecedenceNestedAnd() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        AndClause subAndClause = new AndClause(new TerminalClauseImpl("fourthField", Operator.GREATER_THAN, new SingleValueOperand("other")),
                new TerminalClauseImpl("fifthField", Operator.GREATER_THAN, new SingleValueOperand("other")));
        AndClause andClause = new AndClause(terminalClause1, subAndClause);
        assertEquals("{testField = \"test\"} AND {fourthField > \"other\"} AND {fifthField > \"other\"}", andClause.toString());
    }
    
    @Test
    public void testVisit() throws Exception
    {
        final AtomicBoolean visitCalled = new AtomicBoolean(false);
        ClauseVisitor visitor = new ClauseVisitor()
        {
            public Object visit(final AndClause andClause)
            {
                visitCalled.set(true);
                return null;
            }

            public Object visit(final NotClause notClause)
            {
                return failVisitor();
            }

            public Object visit(final OrClause orClause)
            {
                return failVisitor();
            }

            public Object visit(final TerminalClause clause)
            {
                return failVisitor();
            }

            @Override
            public Object visit(WasClause clause)
            {
                return failVisitor();
            }

            @Override
            public Object visit(ChangedClause clause)
            {
                return failVisitor();
            }
        };
        new AndClause(mockClause).accept(visitor);
        assertTrue(visitCalled.get());
    }

    private Object failVisitor()
    {
        fail("Should not be called");
        return null;
    }

    @Test
    public void testHappyPath() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        TerminalClause terminalClause2 = new TerminalClauseImpl("anotherField", Operator.GREATER_THAN, new SingleValueOperand("other"));
        AndClause andClause = new AndClause(EasyList.build(terminalClause1, terminalClause2));
        assertEquals(2, andClause.getClauses().size());
    }

}
