package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.query.operand.EmptyOperand.EMPTY;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.IS;
import static org.junit.Assert.assertEquals;

/**
 * @since v6.0
 */
public class TestLabelsStatisticsMapper
{
    private LabelsStatisticsMapper statisticsMapper;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        statisticsMapper = new LabelsStatisticsMapper(false);
    }

    @Test
    public void testGetUrlSuffixNullLabel() throws Exception
    {
        final Clause originalClause = new TerminalClauseImpl("blah", EQUALS, "blah");
        final QueryImpl originalQuery = new QueryImpl(originalClause);
        SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final SearchRequest newSearchRequest = statisticsMapper.getSearchUrlSuffix(null, originalSearchRequest);

        final AndClause expectedAndClause = new AndClause(originalClause, new TerminalClauseImpl("labels", IS, EMPTY));
        assertEquals(expectedAndClause, newSearchRequest.getQuery().getWhereClause());
    }

    @Test
    public void testGetUrlSuffixNonNullLabel() throws Exception
    {
        final Clause originalClause = new TerminalClauseImpl("blah", EQUALS, "blah");
        final QueryImpl originalQuery = new QueryImpl(originalClause);
        SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final Label label = new Label(1L, 2L, "somelabel");
        final SearchRequest newSearchRequest = statisticsMapper.getSearchUrlSuffix(label, originalSearchRequest);

        final AndClause expectedAndClause = new AndClause(originalClause, new TerminalClauseImpl("labels", EQUALS, "somelabel"));
        assertEquals(expectedAndClause, newSearchRequest.getQuery().getWhereClause());
    }
}