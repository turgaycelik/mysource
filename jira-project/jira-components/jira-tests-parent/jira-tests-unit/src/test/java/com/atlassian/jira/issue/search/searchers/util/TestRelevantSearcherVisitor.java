package com.atlassian.jira.issue.search.searchers.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.MockCustomFieldSearcher;
import com.atlassian.jira.issue.search.searchers.MockIssueSearcher;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
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
public class TestRelevantSearcherVisitor extends MockControllerTestCase
{
    private static final User ANY_USER = null;

    @Test
    public void testSingleTerminal() throws Exception
    {
        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.EQUALS, "value");
        final IssueSearcher issueSearcher = new MockCustomFieldSearcher("1");

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field");
        mockController.setReturnValue(createResult(issueSearcher));
        mockController.replay();

        RelevantSearcherVisitor relevantSearcherVisitor = new RelevantSearcherVisitor(searchHandlerManager, null);
        assertTrue(terminalClause.accept(relevantSearcherVisitor));
        Set<IssueSearcher> relevant = relevantSearcherVisitor.getRelevantSearchers();
        assertEquals(1, relevant.size());
        assertTrue(relevant.contains(issueSearcher));

        mockController.verify();
    }

    @Test
    public void testSingleTerminalWithNoRelevant() throws Exception
    {
        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.EQUALS, "value");
        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field");
        mockController.setReturnValue(createResult(null));
        mockController.replay();

        RelevantSearcherVisitor relevantSearcherVisitor = new RelevantSearcherVisitor(searchHandlerManager, null);
        assertFalse(terminalClause.accept(relevantSearcherVisitor));
        Set<IssueSearcher> relevant = relevantSearcherVisitor.getRelevantSearchers();
        assertEquals(0, relevant.size());

        mockController.verify();
    }

    @Test
    public void testSingleTerminalWithNoRelevantBecauseThereAreTwo() throws Exception
    {
        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.EQUALS, "value");
        final List<IssueSearcher<?>> results = CollectionBuilder.<IssueSearcher<?>>newBuilder(new MockIssueSearcher("sweet"), new MockIssueSearcher("dude")).asList();
        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field");
        mockController.setReturnValue(results);
        mockController.replay();

        RelevantSearcherVisitor relevantSearcherVisitor = new RelevantSearcherVisitor(searchHandlerManager, null);
        assertFalse(terminalClause.accept(relevantSearcherVisitor));
        Set<IssueSearcher> relevant = relevantSearcherVisitor.getRelevantSearchers();
        assertEquals(0, relevant.size());

        mockController.verify();
    }

    @Test
    public void testOneLevelAndWithTwoRelevants() throws Exception
    {
        final TerminalClauseImpl terminal1 = new TerminalClauseImpl("field1", Operator.EQUALS, "value");
        final TerminalClauseImpl terminal2 = new TerminalClauseImpl("field2", Operator.EQUALS, "value");
        AndClause andClause = new AndClause(terminal1, terminal2);

        final IssueSearcher issueSearcher1 = new MockCustomFieldSearcher("1");
        final IssueSearcher issueSearcher2 = new MockCustomFieldSearcher("2");

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field1");
        mockController.setReturnValue(createResult(issueSearcher1));
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field2");
        mockController.setReturnValue(createResult(issueSearcher2));
        mockController.replay();

        RelevantSearcherVisitor relevantSearcherVisitor = new RelevantSearcherVisitor(searchHandlerManager, null);
        assertTrue(andClause.accept(relevantSearcherVisitor));
        Set<IssueSearcher> relevant = relevantSearcherVisitor.getRelevantSearchers();
        assertEquals(2, relevant.size());
        assertTrue(relevant.contains(issueSearcher1));
        assertTrue(relevant.contains(issueSearcher2));

        mockController.verify();
    }

    @Test
    public void testOneLevelAndWithOneRelevantForBoth() throws Exception
    {
        final TerminalClauseImpl terminal1 = new TerminalClauseImpl("field1", Operator.EQUALS, "value");
        final TerminalClauseImpl terminal2 = new TerminalClauseImpl("field2", Operator.EQUALS, "value");
        AndClause andClause = new AndClause(terminal1, terminal2);

        final IssueSearcher issueSearcher1 = new MockCustomFieldSearcher("1");

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field1");
        mockController.setReturnValue(createResult(issueSearcher1));
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field2");
        mockController.setReturnValue(createResult(issueSearcher1));
        mockController.replay();

        RelevantSearcherVisitor relevantSearcherVisitor = new RelevantSearcherVisitor(searchHandlerManager, null);
        assertTrue(andClause.accept(relevantSearcherVisitor));
        Set<IssueSearcher> relevant = relevantSearcherVisitor.getRelevantSearchers();
        assertEquals(1, relevant.size());
        assertTrue(relevant.contains(issueSearcher1));

        mockController.verify();
    }

    @Test
    public void testOneLevelAndWithOneRelevantAndOneNull() throws Exception
    {
        final TerminalClauseImpl terminal1 = new TerminalClauseImpl("field1", Operator.EQUALS, "value");
        final TerminalClauseImpl terminal2 = new TerminalClauseImpl("field2", Operator.EQUALS, "value");
        AndClause andClause = new AndClause(terminal1, terminal2);

        final IssueSearcher issueSearcher1 = new MockCustomFieldSearcher("1");

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field1");
        mockController.setReturnValue(createResult(issueSearcher1));
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field2");
        mockController.setReturnValue(createResult(null));
        mockController.replay();

        RelevantSearcherVisitor relevantSearcherVisitor = new RelevantSearcherVisitor(searchHandlerManager, null);
        assertFalse(andClause.accept(relevantSearcherVisitor));
        Set<IssueSearcher> relevant = relevantSearcherVisitor.getRelevantSearchers();
        assertEquals(1, relevant.size());
        assertTrue(relevant.contains(issueSearcher1));

        mockController.verify();
    }

    @Test
    public void testOneLevelOrWithSameFieldTwice() throws Exception
    {
        final TerminalClauseImpl terminal1 = new TerminalClauseImpl("field", Operator.EQUALS, "value1");
        final TerminalClauseImpl subClause = new TerminalClauseImpl("field", Operator.EQUALS, "value2");
        final NotClause terminal2 = new NotClause(subClause);
        OrClause orClause = new OrClause(terminal1, terminal2);

        final IssueSearcher issueSearcher1 = new MockCustomFieldSearcher("1");

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field");
        mockController.setReturnValue(createResult(issueSearcher1));
        searchHandlerManager.getSearchersByClauseName(ANY_USER, "field");
        mockController.setReturnValue(createResult(issueSearcher1));

        mockController.replay();

        RelevantSearcherVisitor relevantSearcherVisitor = new RelevantSearcherVisitor(searchHandlerManager, null);
        assertTrue(orClause.accept(relevantSearcherVisitor));
        Set<IssueSearcher> relevant = relevantSearcherVisitor.getRelevantSearchers();
        assertEquals(1, relevant.size());
        assertTrue(relevant.contains(issueSearcher1));

        mockController.verify();
    }

    private Collection<IssueSearcher<?>> createResult(IssueSearcher searcher)
    {
        if (searcher == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.<IssueSearcher<?>>singleton(searcher);
        }
    }
}
