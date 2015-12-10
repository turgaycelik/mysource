package com.atlassian.query.clause;

import java.util.concurrent.atomic.AtomicBoolean;

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
 * Unit test for {@link com.atlassian.query.clause.NotClause}.
 * 
 * @since v4.0
 */
public class TestNotClause
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
            new NotClause(null);
            fail("Should not be able to construct an NotClause with null parameters.");
        }
        catch (Exception e)
        {
            // expected
        }
    }

    @Test
    public void testName() throws Exception
    {
        assertEquals("NOT", new NotClause(mockClause).getName());
    }

    @Test
    public void testToString() throws Exception
    {
        TerminalClause terminalClause = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        NotClause notClause = new NotClause(terminalClause);
        assertEquals("NOT {testField = \"test\"}", notClause.toString());
    }

    @Test
    public void testToStringWithOrPrecedence() throws Exception
    {
        OrClause orClause = new OrClause(new TerminalClauseImpl("fourthField", Operator.GREATER_THAN, new SingleValueOperand("other")),
                new TerminalClauseImpl("fifthField", Operator.GREATER_THAN, new SingleValueOperand("other")));
        NotClause notClause = new NotClause(orClause);
        assertEquals("NOT ( {fourthField > \"other\"} OR {fifthField > \"other\"} )", notClause.toString());
    }

    @Test
    public void testToStringWithAndPrecedence() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl("fourthField", Operator.GREATER_THAN, new SingleValueOperand("other")),
                new TerminalClauseImpl("fifthField", Operator.GREATER_THAN, new SingleValueOperand("other")));
        NotClause notClause = new NotClause(andClause);
        assertEquals("NOT ( {fourthField > \"other\"} AND {fifthField > \"other\"} )", notClause.toString());
    }

    @Test
    public void testToStringWithNotPrecedence() throws Exception
    {
        NotClause subNotClause = new NotClause(new TerminalClauseImpl("fourthField", Operator.GREATER_THAN, new SingleValueOperand("other")));
        NotClause notClause = new NotClause(subNotClause);
        assertEquals("NOT NOT {fourthField > \"other\"}", notClause.toString());
    }
    
    @Test
    public void testGetClause() throws Exception
    {
        TerminalClause terminalClause = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        NotClause notClause = new NotClause(terminalClause);
        assertEquals(terminalClause, notClause.getSubClause());
        assertEquals(1, notClause.getClauses().size());
        assertEquals(terminalClause, notClause.getClauses().get(0));
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
                visitCalled.set(true);
                return null;
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
        new NotClause(mockClause).accept(visitor);
        assertTrue(visitCalled.get());
    }

    private Object failVisitor()
    {
        fail("Should not be called");
        return null;
    }
}
