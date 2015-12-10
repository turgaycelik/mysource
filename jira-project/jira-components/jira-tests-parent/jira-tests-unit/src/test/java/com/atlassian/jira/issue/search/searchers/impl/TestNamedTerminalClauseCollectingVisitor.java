package com.atlassian.jira.issue.search.searchers.impl;

import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestNamedTerminalClauseCollectingVisitor
{
    @Test
    public void testNamedTerminalClauseVisitor() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("anotherValue"));
        AndClause andClause = new AndClause(clause1, clause2);
        final TerminalClauseImpl clause3 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        NotClause notClause = new NotClause(clause3);
        OrClause firstOrClause = new OrClause(andClause, notClause);
        final TerminalClauseImpl clause4 = new TerminalClauseImpl("differentField", Operator.EQUALS, new SingleValueOperand("fourthValue"));
        OrClause orClause = new OrClause(clause4, firstOrClause);

        final QueryImpl query = new QueryImpl(orClause, null);

        NamedTerminalClauseCollectingVisitor namedTerminalClauseVisitor = new NamedTerminalClauseCollectingVisitor("testField");
        assertNull(query.getWhereClause().accept(namedTerminalClauseVisitor));
        final List<TerminalClause> matchingClauses = namedTerminalClauseVisitor.getNamedClauses();
        assertEquals(3, matchingClauses.size());
        assertTrue(namedTerminalClauseVisitor.containsNamedClause());
        assertTrue(matchingClauses.contains(clause1));
        assertTrue(matchingClauses.contains(clause2));
        assertTrue(matchingClauses.contains(clause3));
    }

    @Test
    public void testNamedTerminalClauseVisitorMultipleNamedClauses() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("anotherValue"));
        AndClause andClause = new AndClause(clause1, clause2);
        final TerminalClauseImpl clause3 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        NotClause notClause = new NotClause(clause3);
        OrClause firstOrClause = new OrClause(andClause, notClause);
        final TerminalClauseImpl clause4 = new TerminalClauseImpl("differentField", Operator.EQUALS, new SingleValueOperand("fourthValue"));
        OrClause orClause = new OrClause(clause4, firstOrClause);

        final QueryImpl query = new QueryImpl(orClause, null);

        NamedTerminalClauseCollectingVisitor namedTerminalClauseVisitor = new NamedTerminalClauseCollectingVisitor(EasyList.build("testField", "differentField"));
        assertNull(query.getWhereClause().accept(namedTerminalClauseVisitor));
        final List<TerminalClause> matchingClauses = namedTerminalClauseVisitor.getNamedClauses();
        assertEquals(4, matchingClauses.size());
        assertTrue(namedTerminalClauseVisitor.containsNamedClause());
        assertTrue(matchingClauses.contains(clause1));
        assertTrue(matchingClauses.contains(clause2));
        assertTrue(matchingClauses.contains(clause3));
        assertTrue(matchingClauses.contains(clause4));
    }

    @Test
    public void testNamedTerminalClauseVisitorCaseInsensitiveComparison() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("anotherValue"));
        AndClause andClause = new AndClause(clause1, clause2);
        final TerminalClauseImpl clause3 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        NotClause notClause = new NotClause(clause3);
        OrClause firstOrClause = new OrClause(andClause, notClause);
        final TerminalClauseImpl clause4 = new TerminalClauseImpl("differentField", Operator.EQUALS, new SingleValueOperand("fourthValue"));
        OrClause orClause = new OrClause(clause4, firstOrClause);

        final QueryImpl query = new QueryImpl(orClause, null);

        NamedTerminalClauseCollectingVisitor namedTerminalClauseVisitor = new NamedTerminalClauseCollectingVisitor(EasyList.build("testFIELD", "differentFIELD"));
        assertNull(query.getWhereClause().accept(namedTerminalClauseVisitor));
        final List<TerminalClause> matchingClauses = namedTerminalClauseVisitor.getNamedClauses();
        assertEquals(4, matchingClauses.size());
        assertTrue(namedTerminalClauseVisitor.containsNamedClause());
        assertTrue(matchingClauses.contains(clause1));
        assertTrue(matchingClauses.contains(clause2));
        assertTrue(matchingClauses.contains(clause3));
        assertTrue(matchingClauses.contains(clause4));
    }

}
