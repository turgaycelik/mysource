package com.atlassian.jira.bc.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ListOrderedMessageSetImpl;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.order.SortOrder;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import junit.framework.Assert;

/**
 * Test for {@link com.atlassian.jira.bc.issue.search.LuceneCurrentSearchIssuePickerSearchProvider}.
 *
 * @since v4.2
 */
public class TestLuceneCurrentSearchIssuePickerSearchProvider
{
    @Rule public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);
   
    @Test
    public void testGetId()
    {
        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(null, null, null, null, null);
        Assert.assertEquals("cs", searchProvider.getId());
    }

    @Test
    public void testGetKey()
    {
        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(null, null, null, null, null);
        Assert.assertEquals("jira.ajax.autocomplete.current.search", searchProvider.getLabelKey());
    }

    @Test
    public void testGetRequestNullJql()
    {
        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(null, null, null, null, null);
        Assert.assertNull(searchProvider.getRequest(createParameters()));
    }

    /*
     * Test when the JQL parses correctly.
     */
    @Test
    public void testGetRequestNoErrors()
    {
        final String jql = "this should pass through";
        final Query fullQuery = JqlQueryBuilder.newBuilder().where().assignee().eq("jack").endWhere().orderBy().assignee(SortOrder.DESC).buildQuery();
        final Query expectedQuery = new QueryImpl(fullQuery.getWhereClause(), null, null);
        final User user = new MockUser("test");
        final JiraAuthenticationContext ctx  = new MockSimpleAuthenticationContext(user);

        final IMocksControl mockControl = EasyMock.createControl();

        final SearchService service = mockControl.createMock(SearchService.class);
        EasyMock.expect(service.parseQuery(user, jql)).andReturn(new SearchService.ParseResult(fullQuery, new ListOrderedMessageSetImpl()));

        mockControl.replay();

        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(ctx, null, null, service, null);
        final SearchRequest request = searchProvider.getRequest(createParameters(null, jql));

        Assert.assertNotNull(request);
        Assert.assertEquals(expectedQuery, request.getQuery());

        mockControl.verify();
    }

    /*
     * Test when the JQL does not parse correctly.
     */
    @Test
    public void testGetRequestWithErrors()
    {
        final String jql = "this should pass through";
        final Query expectedQuery = JqlQueryBuilder.newBuilder().where().assignee().eq("jack").endWhere().orderBy().assignee(SortOrder.DESC).buildQuery();
        final User user = new MockUser("test");
        final JiraAuthenticationContext ctx  = new MockSimpleAuthenticationContext(user);
        final MessageSet errors = new ListOrderedMessageSetImpl();
        errors.addErrorMessage("I have an error");

        final IMocksControl mockControl = EasyMock.createControl();

        final SearchService service = mockControl.createMock(SearchService.class);
        EasyMock.expect(service.parseQuery(user, jql)).andReturn(new SearchService.ParseResult(expectedQuery, errors));

        mockControl.replay();

        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(ctx, null, null, service, null);
        final SearchRequest request = searchProvider.getRequest(createParameters(null, jql));

        Assert.assertNull(request);
        mockControl.verify();
    }

    @Test
    public void testHandlesParameters() throws Exception
    {
        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(null, null, null, null, null);

        //Should not accept null.
        Assert.assertFalse(searchProvider.handlesParameters(null, createParameters("", "something = bad")));
        Assert.assertFalse(searchProvider.handlesParameters(null, createParameters(null, "something = bad")));
        Assert.assertFalse(searchProvider.handlesParameters(null, createParameters("H", null)));
        Assert.assertTrue(searchProvider.handlesParameters(null, createParameters("H", "")));
        Assert.assertTrue(searchProvider.handlesParameters(null, createParameters("H", "sosmesms")));
    }

    private static IssuePickerSearchService.IssuePickerParameters createParameters(String query, String jql)
    {
        return new IssuePickerSearchService.IssuePickerParameters(query, jql, null, null, true, true, 10);
    }

    private static IssuePickerSearchService.IssuePickerParameters createParameters()
    {
        return createParameters(null, null);
    }
}
