package com.atlassian.jira.jql.query;

import java.util.Date;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.operand.PredicateOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.ChangeHistoryFieldIdResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.jql.util.DateRange;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.query.history.AndHistoryPredicate;
import com.atlassian.query.history.HistoryPredicate;
import com.atlassian.query.history.TerminalHistoryPredicate;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Lists;

import org.apache.lucene.search.BooleanQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * @since v4.4
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHistoryPredicateQueryFactory
{
    @Mock private AndHistoryPredicate mockAndHistoryPredicate;
    @Mock private TerminalHistoryPredicate mockTerminalHistoryPredicate;
    @Mock private PredicateOperandResolver mockPredicateOperandResolver;
    @Mock private JqlDateSupport mockJqlDateSupport;
    @Mock private User mockSearcher;
    @Mock private UserResolver mockUserResolver;
    @Mock private ChangeHistoryFieldIdResolver mockChangeHistoryFieldIdResolver;
    private HistoryPredicateQueryFactory historyPredicateQueryFactory;


    @Before
    public void setUp()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        historyPredicateQueryFactory = new HistoryPredicateQueryFactory(
                mockPredicateOperandResolver, mockJqlDateSupport,
                mockUserResolver, mockChangeHistoryFieldIdResolver);
    }

    @After
    public void tearDown()
    {
        mockAndHistoryPredicate = null;
        mockTerminalHistoryPredicate = null;
        mockPredicateOperandResolver = null;
        mockJqlDateSupport = null;
        mockSearcher = null;
        mockChangeHistoryFieldIdResolver = null;
        historyPredicateQueryFactory = null;
    }


    @Test (expected = IllegalArgumentException .class)
    public void testNullPredicates()
    {
        assertInvalidPredicateQuery(null);
    }

    @Test
    public void testNullAndHistoryPredicate()
    {
        when(mockAndHistoryPredicate.getPredicates()).thenReturn(Lists.<HistoryPredicate>newArrayList());
        assertInvalidPredicateQuery(mockAndHistoryPredicate);
    }

    @Test
    public void testNullTerminalHistoryPredicate()
    {
        assertInvalidPredicateQuery(mockTerminalHistoryPredicate);
    }

    @Test
    public void testValidTerminalPredicate()
    {
        setupTerminalPredicate();

        BooleanQuery query = historyPredicateQueryFactory.makePredicateQuery(mockSearcher, "field", mockTerminalHistoryPredicate, false);
        assertTrue("Query should have 2 clauses", query.clauses().size() == 2);
    }

    @Test
    public void testValidAndPredicate()
    {
        setupTerminalPredicate();
        when(mockAndHistoryPredicate.getPredicates()).thenReturn(Lists.<HistoryPredicate>newArrayList(mockTerminalHistoryPredicate));

        BooleanQuery query = historyPredicateQueryFactory.makePredicateQuery(mockSearcher, "field",  mockTerminalHistoryPredicate, false);
        assertTrue("Query should have 2 clauses", query.clauses().size() == 2);
    }

    private void setupTerminalPredicate()
    {
        final Date now = new Date();
        final DateRange dateRange = new DateRange(now,now);
        final Operand operand = new SingleValueOperand(now.getTime());
        when(mockTerminalHistoryPredicate.getOperator()).thenReturn(Operator.BEFORE);
        when(mockTerminalHistoryPredicate.getOperand()).thenReturn(operand);
        when(mockPredicateOperandResolver.getValues(mockSearcher, "field", operand)).thenReturn(Lists.newArrayList(new QueryLiteral(operand, now.getTime())));
        when(mockJqlDateSupport.convertToDateRange(now.getTime())).thenReturn(dateRange);
        when(mockJqlDateSupport.getIndexedValue(isA(Date.class))).thenReturn("1");
    }

    private void assertInvalidPredicateQuery(final HistoryPredicate historyPredicate)
    {
        BooleanQuery query = historyPredicateQueryFactory.makePredicateQuery(mockSearcher, "field", historyPredicate, false);
        assertTrue("Query should be empty", query.clauses().isEmpty());
    }
}

