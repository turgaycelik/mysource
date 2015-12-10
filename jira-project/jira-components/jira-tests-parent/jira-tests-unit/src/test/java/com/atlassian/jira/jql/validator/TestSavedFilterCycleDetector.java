package com.atlassian.jira.jql.validator;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.MockSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestSavedFilterCycleDetector
{
    @Mock
    private SavedFilterResolver savedFilterResolver;

    private JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
    private SearchRequest searchRequest;
    private SavedFilterCycleDetector savedFilterCycleDetector;

    private static final String SEARCH_REQUEST_NAME = "My Search Request";
    private static final Long SEARCH_REQUEST_ID = 12345L;

    private User theUser = null;
    private boolean overrideSecurity = false;
    private long nextId = 0;

    @Before
    public void setUp() throws Exception
    {
        searchRequest = new MockSearchRequest("admin", SEARCH_REQUEST_ID);
        searchRequest.setName(SEARCH_REQUEST_NAME);
        savedFilterCycleDetector = new SavedFilterCycleDetector(savedFilterResolver, jqlOperandResolver);
        new MockComponentWorker().init();
    }

    @Test
    public void testContainsSavedFilterReferenceReferencesSelf() throws Exception
    {
        searchRequest.setQuery(createQuery(SEARCH_REQUEST_NAME));

        final QueryLiteral queryLiteral = createLiteral(SEARCH_REQUEST_NAME);
        when(savedFilterResolver.getSearchRequest(theUser, singletonList(queryLiteral)))
                .thenReturn(singletonList(searchRequest));

        assertTrue(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
    }

    @Test
    public void testContainsSavedFilterReferenceReferencesSelfWhenPassedIdToSelf() throws Exception
    {
        searchRequest.setQuery(createQuery());
        assertTrue(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, SEARCH_REQUEST_ID));
    }

    @Test
    public void testNullWhere() throws Exception
    {
        assertFalse(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
    }

    @Test
    public void testContainsSavedFilterReferenceReferencesSelfOverrideSecurity() throws Exception
    {
        overrideSecurity = true;
        searchRequest.setQuery(createQuery(SEARCH_REQUEST_NAME));

        final QueryLiteral queryLiteral = createLiteral(SEARCH_REQUEST_NAME);
        when(savedFilterResolver.getSearchRequestOverrideSecurity(singletonList(queryLiteral)))
                .thenReturn(CollectionBuilder.newBuilder(searchRequest).asList());

        assertTrue(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
    }

    @Test
    public void testContainsSavedFilterReferenceReferencesThroughAnother() throws Exception
    {
        final String otherName = "Other Saved Filter";
        final SearchRequest otherSearchRequest = new MockSearchRequest("admin", 54321L);
        otherSearchRequest.setName(otherName);
        otherSearchRequest.setQuery(createQuery(SEARCH_REQUEST_NAME));

        searchRequest.setQuery(createQuery(otherName));

        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral(otherName)).asList();
        final List<QueryLiteral> otherQueryLiterals = CollectionBuilder.newBuilder(createLiteral(SEARCH_REQUEST_NAME)).asList();

        when(savedFilterResolver.getSearchRequest(theUser, queryLiterals))
                .thenReturn(CollectionBuilder.newBuilder(otherSearchRequest).asList());

        when(savedFilterResolver.getSearchRequest(theUser, otherQueryLiterals))
                .thenReturn(CollectionBuilder.newBuilder(searchRequest).asList());

        assertTrue(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
    }

    @Test
    public void testContainsSavedFilterReferenceReferencesThroughAnotherUseFilterID() throws Exception
    {
        final String otherName = "Other Saved Filter";
        final long otherId = 54321L;
        final SearchRequest otherSearchRequest = new MockSearchRequest("admin", otherId);
        otherSearchRequest.setName(otherName);

        searchRequest.setQuery(createQuery(otherName));

        when(savedFilterResolver.getSearchRequest(theUser, singletonList(createLiteral(otherName))))
                .thenReturn(CollectionBuilder.newBuilder(otherSearchRequest).asList());

        assertTrue(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, otherId));
    }

    @Test
    public void testNoCycle() throws Exception
    {
        TerminalClause anotherFilterClause = new TerminalClauseImpl("project", Operator.EQUALS, "my proj");

        final String anotherFilter = "Another Saved Filter";
        final SearchRequest anotherSearchRequest = new MockSearchRequest("admin", 98765L, anotherFilter);
        anotherSearchRequest.setQuery(new QueryImpl(anotherFilterClause));


        final String otherFilter = "Other Saved Filter";
        final SearchRequest otherSearchRequest = new MockSearchRequest("admin", 54321L, otherFilter);
        otherSearchRequest.setQuery(createQuery(anotherFilter));

        TerminalClause filterClause = new TerminalClauseImpl("filter", Operator.EQUALS, otherFilter);
        searchRequest.setQuery(new QueryImpl(filterClause));

        final List<QueryLiteral> queryLiterals = singletonList(createLiteral(otherFilter));
        final List<QueryLiteral> otherQueryLiterals = singletonList(createLiteral(anotherFilter));

        when(savedFilterResolver.getSearchRequest(theUser, queryLiterals))
                .thenReturn(singletonList(otherSearchRequest));
        when(savedFilterResolver.getSearchRequest(theUser, otherQueryLiterals))
                .thenReturn(singletonList(anotherSearchRequest));

        assertFalse(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
    }

    @Test
    public void testPassedANullSavedFilter() throws Exception
    {
        try
        {
            savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, null, null);
            fail("should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testPassedAllSearchQuery() throws Exception
    {
        searchRequest.setQuery(new QueryImpl());
        assertFalse(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
    }

    @Test
    public void testPassedAllSearchQueryLetsGoDeeper() throws Exception
    {
        searchRequest.setQuery(createQuery(SEARCH_REQUEST_NAME));

        SearchRequest otherSearcherRequest = new MockSearchRequest("admin", 6L, "Other");
        otherSearcherRequest.setQuery(new QueryImpl());

        final QueryLiteral queryLiteral = createLiteral(SEARCH_REQUEST_NAME);

        when(savedFilterResolver.getSearchRequest(theUser, singletonList(queryLiteral)))
                .thenReturn(singletonList(otherSearcherRequest));

        assertFalse(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
    }

    /*
     * Creating query resolved -> unresolved -> common
     *                       |                    ^
     *                       |                   /
     *                       -------------------
     * Making sure this does not get picked up incorrectly as a cycle.
     */
    @Test
    public void jra28270FindsCyclesEvenWhenNone()
    {
        final SearchRequest common = createSearchRequest("common", JqlQueryBuilder.newBuilder().buildQuery());
        final SearchRequest unresolved = createSearchRequest("unresolved",
                JqlQueryBuilder.newBuilder()
                        .where().savedFilter(common.getName()).and().resolution().isEmpty().buildQuery());
        SearchRequest resolved = createSearchRequest("resolved",
                JqlQueryBuilder.newBuilder()
                        .where().savedFilter().notEq(unresolved.getName()).and().savedFilter(common.getName()).buildQuery());
        assertFalse(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, resolved, null));
    }

    /*
     *  -------> (c) -------> (e)
     *  |                    /
     * (a)        -----------
     *  |         V
     *  -------> (b) -------> (d)
     *
     * Making sure this does not get picked up incorrectly as a cycle.
     */
    @Test
    public void jra28270FindsCyclesEvenWhenNoneComplex()
    {
        final SearchRequest d = createSearchRequest("d", JqlQueryBuilder.newBuilder().where().buildQuery());
        final SearchRequest b = createSearchRequest("b", JqlQueryBuilder.newBuilder().where().savedFilter(d.getName()).buildQuery());
        final SearchRequest e = createSearchRequest("e", JqlQueryBuilder.newBuilder().where().savedFilter(b.getName()).buildQuery());
        final SearchRequest c = createSearchRequest("c", JqlQueryBuilder.newBuilder().where().savedFilter(e.getName()).buildQuery());
        final SearchRequest a = createSearchRequest("a", JqlQueryBuilder.newBuilder().where().savedFilter(b.getName(), c.getName()).buildQuery());

        assertFalse(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, a, null));
    }

    private SearchRequest createSearchRequest(final String name, Query query)
    {
        SearchRequest request = new MockSearchRequest("admin", getNextId(), name);
        request.setQuery(query);
        mockLookup(request);
        return request;
    }

    private long getNextId() {return nextId++;}

    private void mockLookup(SearchRequest request)
    {
        when(savedFilterResolver.getSearchRequest(theUser, singletonList(createLiteral(request.getName()))))
                .thenReturn(Collections.singletonList(request));
    }

    private static Query createQuery(final String... name)
    {
        if (name.length == 0)
        {
            return new QueryImpl();
        }
        else
        {
            return new QueryImpl(new TerminalClauseImpl("filter", name));
        }
    }
}
