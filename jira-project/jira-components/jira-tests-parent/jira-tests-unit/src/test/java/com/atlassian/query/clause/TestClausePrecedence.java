package com.atlassian.query.clause;

import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestClausePrecedence
{
    @Test
    public void testGetAnd() throws Exception
    {
        final ClausePrecedence clausePrecedence = ClausePrecedence.getPrecedence(new AndClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blah")));
        assertEquals(ClausePrecedence.AND, clausePrecedence);
    }

    @Test
    public void testGetOr() throws Exception
    {
        final ClausePrecedence clausePrecedence = ClausePrecedence.getPrecedence(new OrClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blah")));
        assertEquals(ClausePrecedence.OR, clausePrecedence);
    }

    @Test
    public void testGetNot() throws Exception
    {
        final ClausePrecedence clausePrecedence = ClausePrecedence.getPrecedence(new NotClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blah")));
        assertEquals(ClausePrecedence.NOT, clausePrecedence);
    }

    @Test
    public void testGetTerminal() throws Exception
    {
        final ClausePrecedence clausePrecedence = ClausePrecedence.getPrecedence(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"));
        assertEquals(ClausePrecedence.TERMINAL, clausePrecedence);
    }

    @Test
    public void testPrecedenceOrder() throws Exception
    {
        assertTrue(ClausePrecedence.TERMINAL.getValue() > ClausePrecedence.NOT.getValue());
        assertTrue(ClausePrecedence.TERMINAL.getValue() > ClausePrecedence.AND.getValue());
        assertTrue(ClausePrecedence.TERMINAL.getValue() > ClausePrecedence.OR.getValue());

        assertTrue(ClausePrecedence.NOT.getValue() > ClausePrecedence.AND.getValue());
        assertTrue(ClausePrecedence.NOT.getValue() > ClausePrecedence.OR.getValue());

        assertTrue(ClausePrecedence.AND.getValue() > ClausePrecedence.OR.getValue());
    }
    
}
