package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestSimpleNavigatorCollectorVisitor
{
    @Test
    public void testSingleTerminal() throws Exception
    {
        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.EQUALS, "operand");

        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor("field");
        terminalClause.accept(collector);

        assertTrue(collector.isValid());
        assertEquals(1, collector.getClauses().size());
        assertEquals(terminalClause, collector.getClauses().get(0));
    }

    @Test
    public void testNotSingleTerminal() throws Exception
    {
        final TerminalClauseImpl terminalClause = new TerminalClauseImpl("field", Operator.EQUALS, "operand");
        NotClause notClause = new NotClause(terminalClause);

        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor("field");
        notClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(1, collector.getClauses().size());
        assertEquals(terminalClause, collector.getClauses().get(0));
    }

    @Test
    public void testOneLevelAnd() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("field", Operator.EQUALS, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl("field", Operator.EQUALS, "operand2");

        AndClause andClause = new AndClause(terminalClause1, terminalClause2);

        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor("field");
        andClause.accept(collector);

        assertTrue(collector.isValid());
        assertEquals(2, collector.getClauses().size());
        assertEquals(terminalClause1, collector.getClauses().get(0));
        assertEquals(terminalClause2, collector.getClauses().get(1));
    }

    @Test
    public void testOneLevelAndWithNot() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("field", Operator.EQUALS, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl("field", Operator.EQUALS, "operand2");

        AndClause andClause = new AndClause(terminalClause1, new NotClause(terminalClause2));

        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor("field");
        andClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(2, collector.getClauses().size());
        assertEquals(terminalClause1, collector.getClauses().get(0));
        assertEquals(terminalClause2, collector.getClauses().get(1));
    }

    @Test
    public void testTwoLevelAndOr() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("field", Operator.EQUALS, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl("field", Operator.EQUALS, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl("field", Operator.EQUALS, "operand3");

        OrClause orClause = new OrClause(terminalClause2, terminalClause3);
        AndClause andClause = new AndClause(terminalClause1, orClause);

        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor("field");
        andClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(3, collector.getClauses().size());
        assertEquals(terminalClause1, collector.getClauses().get(0));
        assertEquals(terminalClause2, collector.getClauses().get(1));
        assertEquals(terminalClause3, collector.getClauses().get(2));
    }

    @Test
    public void testTwoLevelAndOrCaseInsensitive() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("FIELD", Operator.EQUALS, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl("fiELD", Operator.EQUALS, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl("field", Operator.EQUALS, "operand3");

        OrClause orClause = new OrClause(terminalClause2, terminalClause3);
        AndClause andClause = new AndClause(terminalClause1, orClause);

        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor("field");
        andClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(3, collector.getClauses().size());
        assertEquals(terminalClause1, collector.getClauses().get(0));
        assertEquals(terminalClause2, collector.getClauses().get(1));
        assertEquals(terminalClause3, collector.getClauses().get(2));
    }

    @Test
    public void testTwoLevelAnd() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("field", Operator.EQUALS, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl("field", Operator.EQUALS, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl("field", Operator.EQUALS, "operand3");

        AndClause andClause1 = new AndClause(terminalClause2, terminalClause3);
        AndClause andClause2 = new AndClause(terminalClause1, andClause1);

        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor("field");
        andClause2.accept(collector);

        assertTrue(collector.isValid());
        assertEquals(3, collector.getClauses().size());
        assertEquals(terminalClause1, collector.getClauses().get(0));
        assertEquals(terminalClause2, collector.getClauses().get(1));
        assertEquals(terminalClause3, collector.getClauses().get(2));
    }

    @Test
    public void testThreeLevelAnd() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("field", Operator.EQUALS, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl("field", Operator.EQUALS, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl("field", Operator.EQUALS, "operand3");
        TerminalClause terminalClause4 = new TerminalClauseImpl("field", Operator.EQUALS, "operand3");

        AndClause andClause1 = new AndClause(terminalClause1, terminalClause2);
        AndClause andClause2 = new AndClause(terminalClause3, andClause1);
        AndClause andClause3 = new AndClause(terminalClause4, andClause2);

        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor("field");
        andClause3.accept(collector);

        assertTrue(collector.isValid());
        assertEquals(4, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
        assertTrue(collector.getClauses().contains(terminalClause4));
    }

    @Test
    public void testOneLevelOr() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("field", Operator.EQUALS, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl("field", Operator.EQUALS, "operand2");

        OrClause orClause = new OrClause(terminalClause1, terminalClause2);

        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor("field");
        orClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(2, collector.getClauses().size());
        assertEquals(terminalClause1, collector.getClauses().get(0));
        assertEquals(terminalClause2, collector.getClauses().get(1));
    }

    @Test
    public void testThreeLevelAndOrNot() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("field", Operator.EQUALS, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl("field", Operator.EQUALS, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl("field", Operator.EQUALS, "operand3");

        OrClause orClause = new OrClause(new NotClause(terminalClause2), terminalClause3);
        AndClause andClause = new AndClause(terminalClause1, orClause);

        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor("field");
        andClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(3, collector.getClauses().size());
        assertEquals(terminalClause1, collector.getClauses().get(0));
        assertEquals(terminalClause2, collector.getClauses().get(1));
        assertEquals(terminalClause3, collector.getClauses().get(2));
    }    
}
