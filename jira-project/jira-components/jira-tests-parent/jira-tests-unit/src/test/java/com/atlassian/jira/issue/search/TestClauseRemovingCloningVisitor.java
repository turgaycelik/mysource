package com.atlassian.jira.issue.search;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestClauseRemovingCloningVisitor
{
    @Test
    public void testRemoveSomeClausesRemoveOr() throws Exception
    {
        Clause origClause = new AndClause(
                new OrClause(
                        new TerminalClauseImpl("test1", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new NotClause(new TerminalClauseImpl("test3", Operator.EQUALS, 5L)),
                new TerminalClauseImpl("test1", Operator.EQUALS, 7L));

        final ClauseRemovingCloningVisitor clauseRemovingCloningVisitor = new ClauseRemovingCloningVisitor(EasyList.build("test1", "test2"));
        Clause expectedClause = new AndClause(new NotClause(new TerminalClauseImpl("test3", Operator.EQUALS, 5L)));

        assertEquals(expectedClause, origClause.accept(clauseRemovingCloningVisitor));
    }

    @Test
    public void testRemoveSomeClausesRemoveAnd() throws Exception
    {
        Clause origClause = new OrClause(
                new AndClause(
                        new TerminalClauseImpl("test1", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new TerminalClauseImpl("test3", Operator.EQUALS, 5L),
                new TerminalClauseImpl("test1", Operator.EQUALS, 7L));

        final ClauseRemovingCloningVisitor clauseRemovingCloningVisitor = new ClauseRemovingCloningVisitor(EasyList.build("test1", "test2"));
        Clause expectedClause = new OrClause(new TerminalClauseImpl("test3", Operator.EQUALS, 5L));

        assertEquals(expectedClause, origClause.accept(clauseRemovingCloningVisitor));
    }

    @Test
    public void testRemoveSomeClausesRemoveNot() throws Exception
    {
        Clause origClause = new OrClause(
                new AndClause(
                        new TerminalClauseImpl("test2", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new TerminalClauseImpl("test3", Operator.EQUALS, 5L),
                new NotClause(new TerminalClauseImpl("test1", Operator.EQUALS, 7L)));

        final ClauseRemovingCloningVisitor clauseRemovingCloningVisitor = new ClauseRemovingCloningVisitor(EasyList.build("test1"));
        Clause expectedClause = new OrClause(
                new AndClause(
                        new TerminalClauseImpl("test2", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new TerminalClauseImpl("test3", Operator.EQUALS, 5L));

        assertEquals(expectedClause, origClause.accept(clauseRemovingCloningVisitor));
    }
    
    @Test
    public void testRemoveOneClause() throws Exception
    {
        Clause origClause = new AndClause(
                new OrClause(
                        new TerminalClauseImpl("test1", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new TerminalClauseImpl("test3", Operator.EQUALS, 5L),
                new TerminalClauseImpl("test1", Operator.EQUALS, 7L));

        final ClauseRemovingCloningVisitor clauseRemovingCloningVisitor = new ClauseRemovingCloningVisitor(EasyList.build("test3"));
        Clause expectedClause = new AndClause(
                new OrClause(
                        new TerminalClauseImpl("test1", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new TerminalClauseImpl("test1", Operator.EQUALS, 7L));

        assertEquals(expectedClause, origClause.accept(clauseRemovingCloningVisitor));
    }

    @Test
    public void testRemoveOneClauseCaseInsensitve() throws Exception
    {
        Clause origClause = new AndClause(
                new OrClause(
                        new TerminalClauseImpl("test1", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new TerminalClauseImpl("test3", Operator.EQUALS, 5L),
                new TerminalClauseImpl("test1", Operator.EQUALS, 7L));

        final ClauseRemovingCloningVisitor clauseRemovingCloningVisitor = new ClauseRemovingCloningVisitor(EasyList.build("TEST3"));
        Clause expectedClause = new AndClause(
                new OrClause(
                        new TerminalClauseImpl("test1", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new TerminalClauseImpl("test1", Operator.EQUALS, 7L));

        assertEquals(expectedClause, origClause.accept(clauseRemovingCloningVisitor));
    }

    @Test
    public void testRemoveAllClauses() throws Exception
    {
        Clause origClause = new AndClause(
                new OrClause(
                        new TerminalClauseImpl("test1", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new TerminalClauseImpl("test3", Operator.EQUALS, 5L),
                new TerminalClauseImpl("test1", Operator.EQUALS, 7L));

        final ClauseRemovingCloningVisitor clauseRemovingCloningVisitor = new ClauseRemovingCloningVisitor(EasyList.build("test1", "test2", "test3"));

        assertNull(origClause.accept(clauseRemovingCloningVisitor));
    }

    @Test
    public void testRemoveNone() throws Exception
    {
        Clause origClause = new AndClause(
                new OrClause(
                        new TerminalClauseImpl("test1", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new TerminalClauseImpl("test3", Operator.EQUALS, 5L),
                new TerminalClauseImpl("test1", Operator.EQUALS, 7L));

        final ClauseRemovingCloningVisitor clauseRemovingCloningVisitor = new ClauseRemovingCloningVisitor(EasyList.build("test5", "test6", "test7"));

        assertEquals(origClause, origClause.accept(clauseRemovingCloningVisitor));
    }

    @Test
    public void testRemoveNoneNullRemovalList() throws Exception
    {
        Clause origClause = new AndClause(
                new OrClause(
                        new TerminalClauseImpl("test1", Operator.EQUALS, 2L),
                        new TerminalClauseImpl("test2", Operator.EQUALS, 4L)),
                new TerminalClauseImpl("test3", Operator.EQUALS, 5L),
                new TerminalClauseImpl("test1", Operator.EQUALS, 7L));

        final ClauseRemovingCloningVisitor clauseRemovingCloningVisitor = new ClauseRemovingCloningVisitor(null);

        assertEquals(origClause, origClause.accept(clauseRemovingCloningVisitor));
    }


}
