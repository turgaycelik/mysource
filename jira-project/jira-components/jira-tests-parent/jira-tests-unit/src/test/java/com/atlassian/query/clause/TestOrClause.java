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
public class TestOrClause
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
            new OrClause((Clause)null);
            fail("Should not be able to construct an OrClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
        try
        {
            new OrClause(mockClause, null);
            fail("Should not be able to construct an OrClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
        try
        {
            new OrClause(null, mockClause);
            fail("Should not be able to construct an OrClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
        try
        {
            new OrClause(null, null);
            fail("Should not be able to construct an OrClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
        try
        {
            new OrClause((List<Clause>)null);
            fail("Should not be able to construct an OrClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
        try
        {
            new OrClause(Collections.<Clause>emptyList());
            fail("Should not be able to construct an OrClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
    }

    @Test
    public void testName() throws Exception
    {
        assertEquals("OR", new OrClause(mockClause).getName());
    }

    @Test
    public void testToString() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        TerminalClause terminalClause2 = new TerminalClauseImpl("anotherField", Operator.GREATER_THAN, new SingleValueOperand("other"));
        OrClause OrClause = new OrClause(terminalClause1, terminalClause2);
        assertEquals("{testField = \"test\"} OR {anotherField > \"other\"}", OrClause.toString());
    }

    @Test
    public void testToStringWithPrecedence() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        TerminalClause terminalClause2 = new TerminalClauseImpl("anotherField", Operator.GREATER_THAN, new SingleValueOperand("other"));
        NotClause notClause = new NotClause(new TerminalClauseImpl("thirdField", Operator.GREATER_THAN, new SingleValueOperand("other")));
        AndClause andClause = new AndClause(new TerminalClauseImpl("fourthField", Operator.GREATER_THAN, new SingleValueOperand("other")),
                new TerminalClauseImpl("fifthField", Operator.GREATER_THAN, new SingleValueOperand("other")));
        OrClause OrClause = new OrClause(terminalClause1, terminalClause2, notClause, andClause);
        assertEquals("{testField = \"test\"} OR {anotherField > \"other\"} OR NOT {thirdField > \"other\"} OR {fourthField > \"other\"} AND {fifthField > \"other\"}", OrClause.toString());
    }

    @Test
    public void testToStringWithPrecedenceNestedOr() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        OrClause subOrClause = new OrClause(new TerminalClauseImpl("fourthField", Operator.GREATER_THAN, new SingleValueOperand("other")),
                new TerminalClauseImpl("fifthField", Operator.GREATER_THAN, new SingleValueOperand("other")));
        OrClause orClause = new OrClause(terminalClause1, subOrClause);
        assertEquals("{testField = \"test\"} OR {fourthField > \"other\"} OR {fifthField > \"other\"}", orClause.toString());
    }
    
    @Test
    public void testVisit() throws Exception
    {
        final AtomicBoolean visitCalled = new AtomicBoolean(false);
        ClauseVisitor visitor = new ClauseVisitor()
        {
            public Object visit(final AndClause andClause)
            {
                return failVisitor();
            }

            public Object visit(final NotClause notClause)
            {
                return failVisitor();
            }

            public Object visit(final OrClause orClause)
            {
                visitCalled.set(true);
                return null;
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
        new OrClause(mockClause).accept(visitor);
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
        OrClause orClause = new OrClause(EasyList.build(terminalClause1, terminalClause2));
        assertEquals(2, orClause.getClauses().size());
    }

}
