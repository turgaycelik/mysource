package com.atlassian.jira.bc.issue.search;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.SortOrder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.bc.issue.search.HistoryIssuePickerSearchProvider}.
 */
public class TestHistoryIssuePickerSearchProvider
{
    @Rule public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);
    
    @Test
    public void testCreateQuery() throws Exception
    {
        final HistoryIssuePickerSearchProvider searchProvider = new HistoryIssuePickerSearchProvider(null, null, null);
        final SearchRequest request = searchProvider.getRequest(null);
        assertNotNull(request);
        final Clause expectedClause = JqlQueryBuilder.newBuilder().where().issueInHistory().buildClause();
        final OrderBy expectedOrderBy = JqlQueryBuilder.newOrderByBuilder().lastViewedDate(SortOrder.DESC, true).buildOrderBy();
        assertEquals(expectedClause, request.getQuery().getWhereClause());
        assertEquals(expectedOrderBy, request.getQuery().getOrderByClause());
    }

    @Test
    public void testHandlesParameters() throws Exception
    {
        final HistoryIssuePickerSearchProvider searchProvider = new HistoryIssuePickerSearchProvider(null, null, null);
        assertTrue(searchProvider.handlesParameters(null, new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, false, false, 10)));
        assertTrue(searchProvider.handlesParameters(null, null));
    }

    @Test
    public void testGetId() throws Exception
    {
        final HistoryIssuePickerSearchProvider searchProvider = new HistoryIssuePickerSearchProvider(null, null, null);
        assertEquals("hs", searchProvider.getId());
    }

    @Test
    public void testGetKey() throws Exception
    {
        final HistoryIssuePickerSearchProvider searchProvider = new HistoryIssuePickerSearchProvider(null, null, null);
        assertEquals("jira.ajax.autocomplete.history.search", searchProvider.getLabelKey());
    }
}
