package com.atlassian.jira.issue.search.searchers.util;

import java.util.Collection;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestTerminalClauseCollectingVisitor
{
    @Test
    public void testAndChildren() throws Exception
    {
        TerminalClause child1 = new TerminalClauseImpl("blah1", Operator.EQUALS, "blah1");
        TerminalClause child2 = new TerminalClauseImpl("blah2", Operator.EQUALS, "blah2");
        final AndClause andClause = new AndClause(child1, child2);

        TerminalClauseCollectingVisitor collector = new TerminalClauseCollectingVisitor();
        andClause.accept(collector);

        Collection<TerminalClause> expectedResult = CollectionBuilder.<TerminalClause>newBuilder(child1, child2).asCollection();

        assertEquals(expectedResult, collector.getClauses());
    }

    @Test
    public void testOrChildren() throws Exception
    {
        TerminalClause child1 = new TerminalClauseImpl("blah1", Operator.EQUALS, "blah1");
        TerminalClause child2 = new TerminalClauseImpl("blah2", Operator.EQUALS, "blah2");
        final OrClause orClause = new OrClause(child1, child2);

        TerminalClauseCollectingVisitor collector = new TerminalClauseCollectingVisitor();
        orClause.accept(collector);

        Collection<TerminalClause> expectedResult = CollectionBuilder.newBuilder(child1, child2).asCollection();

        assertEquals(expectedResult, collector.getClauses());
    }

    @Test
    public void testNotChildren() throws Exception
    {
        TerminalClause child = new TerminalClauseImpl("blah1", Operator.EQUALS, "blah1");
        final NotClause notClause = new NotClause(child);

        TerminalClauseCollectingVisitor collector = new TerminalClauseCollectingVisitor();
        notClause.accept(collector);

        Collection<TerminalClause> expectedResult = CollectionBuilder.newBuilder(child).asCollection();

        assertEquals(expectedResult, collector.getClauses());
    }

    @Test
    public void testComplex() throws Exception
    {
        TerminalClause child = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        final NotClause notClause = new NotClause(child);

        TerminalClause child2 = new TerminalClauseImpl("blah2", Operator.EQUALS, "blah2");
        final AndClause andClause = new AndClause(notClause, child2);

        TerminalClause child3 = new TerminalClauseImpl("blah3", Operator.EQUALS, "blah3");
        TerminalClause child4 = new TerminalClauseImpl("blah4", Operator.EQUALS, "blah4");
        final OrClause orClause = new OrClause(andClause, child3, child4);

        TerminalClauseCollectingVisitor collector = new TerminalClauseCollectingVisitor();
        orClause.accept(collector);

        Collection<TerminalClause> expectedResult = CollectionBuilder.newBuilder(child, child2, child3, child4).asCollection();

        assertEquals(expectedResult, collector.getClauses());
    }
}
