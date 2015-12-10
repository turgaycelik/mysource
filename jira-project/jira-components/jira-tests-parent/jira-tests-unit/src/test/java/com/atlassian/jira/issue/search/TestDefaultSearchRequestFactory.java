package com.atlassian.jira.issue.search;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultSearchRequestFactory
{
    @Mock
    private IssueSearcherManager issueSearcherManager;
    @Mock
    private SearchSortUtil searchSortUtil;
    @Mock
    private SearchService searchService;

    private MockUserManager userManager;
    private DefaultSearchRequestFactory searchRequestFactory;

    @Before
    public void setUp() throws Exception
    {
        searchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);
        userManager = new MockUserManager();
        new MockComponentWorker().addMock(UserManager.class, userManager).init();
    }

    @Test
    public void testCreateNewSearchRequestLoaded() throws Exception
    {
        final MockApplicationUser oldowner = new MockApplicationUser("oldowner");
        final SearchRequest oldSR = new SearchRequest(new QueryImpl(), oldowner, "", "", 123L, 1L);
        final ApplicationUser user = new MockApplicationUser("newowner");
        userManager.addUser(oldowner);
        userManager.addUser(user);

        final SearchRequest newSR = searchRequestFactory.createNewSearchRequest(oldSR, user.getDirectoryUser());
        assertEquals("oldowner", newSR.getOwner().getUsername());
    }

    @Test
    public void testCreateNewSearchRequestNotLoaded() throws Exception
    {
        final SearchRequest oldSR = new SearchRequest(new QueryImpl(), new MockApplicationUser("oldowner"), "", "", null, 1L);
        final User user = new MockUser("newowner");
        userManager.addUser(user);

        final SearchRequest newSR = searchRequestFactory.createNewSearchRequest(oldSR, user);
        assertEquals("newowner", newSR.getOwner().getUsername());
    }

    @Test
    public void testCreateNewSearchRequestNullOldSR() throws Exception
    {
        final User user = new MockUser("newowner");
        userManager.addUser(user);

        final SearchRequest newSR = searchRequestFactory.createNewSearchRequest(null, user);
        assertEquals("newowner", newSR.getOwner().getUsername());
    }

    @Test
    public void testCreateNewSearchRequestNullOldSRAndUser() throws Exception
    {

        final SearchRequest newSR = searchRequestFactory.createNewSearchRequest(null, null);
        assertNull(newSR.getOwner());
    }

    @Test
    public void testComparisonOfSearchRequestsStringsEqual() throws Exception
    {
        MockJqlSearchRequest oldSearchRequest = new MockJqlSearchRequest(1l, new QueryImpl(new TerminalClauseImpl("monkey", Operator.EQUALS, "monkey"), new OrderByImpl(Collections.<SearchSort>emptyList()), "monkey = monkey"));
        MockJqlSearchRequest searchRequest = new MockJqlSearchRequest(1l, new QueryImpl(new TerminalClauseImpl("monkey", Operator.EQUALS, "monkey"), new OrderByImpl(Collections.<SearchSort>emptyList()), "monkey = monkey"));
        assertEquiv(oldSearchRequest, searchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsStringsNotEqual() throws Exception
    {
        MockJqlSearchRequest oldSearchRequest = new MockJqlSearchRequest(1l, new QueryImpl(new TerminalClauseImpl("monkey", Operator.EQUALS, "monkey"), new OrderByImpl(Collections.<SearchSort>emptyList()), "monkey =    monkey"));
        MockJqlSearchRequest searchRequest = new MockJqlSearchRequest(1l, new QueryImpl(new TerminalClauseImpl("monkey", Operator.EQUALS, "monkey"), new OrderByImpl(Collections.<SearchSort>emptyList()), "monkey = monkey"));
        assertNotEquiv(oldSearchRequest, searchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsNonQueryAttributesUnequal() throws Exception
    {
        SearchRequest oldSearchRequest = new SearchRequest(new QueryImpl(), new MockApplicationUser("dude"), "name1", null, 1L, 0L);
        SearchRequest searchRequest = new SearchRequest(new QueryImpl(), new MockApplicationUser("dude"), "name2", null, 2L, 0L);
        assertNotEquiv(oldSearchRequest, searchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsLegacyEqualBothNullQuery() throws Exception
    {
        MockJqlSearchRequest oldSearchRequest = createSR(1L, null);
        MockJqlSearchRequest searchRequest = createSR(2L, null);
        assertEquiv(oldSearchRequest, searchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsLegacyEqualOneNullQuery() throws Exception
    {
        MockJqlSearchRequest oldSearchRequest = createSR(1L, null);
        MockJqlSearchRequest searchRequest = createSR(2L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        assertNotEquiv(oldSearchRequest, searchRequest);
        assertNotEquiv(searchRequest, oldSearchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsLegacyEqualOneWhereClause() throws Exception
    {
        MockJqlSearchRequest oldSearchRequest = createSR(1L, null);
        MockJqlSearchRequest searchRequest = createSR(2L, JqlQueryBuilder.newBuilder().where().project(1234L).buildQuery());
        assertNotEquiv(oldSearchRequest, searchRequest);
        assertNotEquiv(searchRequest, oldSearchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsBothNullWhereClause() throws Exception
    {
        MockJqlSearchRequest oldSearchRequest = createSR(1L, new QueryImpl());
        MockJqlSearchRequest searchRequest = createSR(2L, new QueryImpl());
        assertEquiv(oldSearchRequest, searchRequest);
        assertEquiv(searchRequest, oldSearchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsOneNullWhereClause() throws Exception
    {
        MockJqlSearchRequest oldSearchRequest = createSR(1L, new QueryImpl());
        MockJqlSearchRequest searchRequest = createSR(2L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        assertNotEquiv(oldSearchRequest, searchRequest);
        assertNotEquiv(searchRequest, oldSearchRequest);
    }

    @Test
    public void testGetClausesFromSearchersHappyPath() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchInputTransformer transformer = mock(SearchInputTransformer.class);
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromParams(null, valuesHolder, actionParams);
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", "test");
        when(transformer.getSearchClause(null, valuesHolder)).thenReturn(clause);

        final IssueSearcher issueSearcher = mock(IssueSearcher.class);
        when(issueSearcher.getSearchInputTransformer()).thenReturn(transformer);

        when(issueSearcherManager.getAllSearchers()).thenReturn(Collections.<IssueSearcher<?>>singleton(issueSearcher));

        final List<Clause> clauses = searchRequestFactory.getClausesFromSearchers(null, actionParams);

        assertTrue(clauses.contains(clause));
    }

    @Test
    public void testGetClausesFromSearchersNoClauseGenerated() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchInputTransformer transformer = mock(SearchInputTransformer.class);
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromParams(null, valuesHolder, actionParams);
        when(transformer.getSearchClause(null, valuesHolder)).thenReturn(null);

        final IssueSearcher issueSearcher = mock(IssueSearcher.class);
        when(issueSearcher.getSearchInputTransformer()).thenReturn(transformer);

        issueSearcherManager.getAllSearchers();
        when(issueSearcherManager.getAllSearchers()).thenReturn(Collections.<IssueSearcher<?>>singleton(issueSearcher));

        final List<Clause> clauses = searchRequestFactory.getClausesFromSearchers(null, actionParams);

        assertTrue(clauses.isEmpty());

    }

    @Test
    public void testGetClauseOneClause() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", "test");

        final Clause generatedClause = searchRequestFactory.getClause(Collections.<Clause>singletonList(clause));
        assertEquals(clause, generatedClause);
    }

    @Test
    public void testGetClauseMultipleClauses() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("test1", "test");
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("test1", "test");

        final Clause generatedClause = searchRequestFactory.getClause(CollectionBuilder.<Clause>newBuilder(clause1, clause2).asList());
        Clause whenedClause = new AndClause(clause1, clause2);
        assertEquals(whenedClause, generatedClause);
    }

    @Test
    public void testGetClauseNoClauses() throws Exception
    {
        final Clause generatedClause = searchRequestFactory.getClause(Collections.<Clause>emptyList());
        assertNull(generatedClause);
    }

    @Test
    public void testCheckWhereClauses() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("test1", "test");
        assertTrue(searchRequestFactory.checkWhereClauses(null, null));
        assertFalse(searchRequestFactory.checkWhereClauses(null, clause1));
        assertTrue(searchRequestFactory.checkWhereClauses(clause1, clause1));
    }

    @Test
    public void testCheckOrderByClauses() throws Exception
    {
        assertTrue(searchRequestFactory.checkOrderByClauses(null, null));
        assertFalse(searchRequestFactory.checkOrderByClauses(null, new OrderByImpl()));
        assertTrue(searchRequestFactory.checkOrderByClauses(new OrderByImpl(), new OrderByImpl()));
    }

    @Test
    public void testComparisonOfSearchRequestsLegacyEqualQueriesEquiv() throws Exception
    {
        MockJqlSearchRequest oldSearchRequest = createSR(1L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        MockJqlSearchRequest searchRequest = createSR(2L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(null, null, searchService)
        {
            @Override
            boolean checkClauseEquivalence(final Clause oldClause, final Clause clause)
            {
                return true;
            }
        };
        assertTrue(factory.searchRequestsSameOrQueriesEquivalent(oldSearchRequest, searchRequest));
    }

    @Test
    public void testComparisonOfSearchRequestsLegacyEqualQueriesNotEquiv() throws Exception
    {
        MockJqlSearchRequest oldSearchRequest = createSR(1L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        MockJqlSearchRequest searchRequest = createSR(2L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(null, null, searchService)
        {
            @Override
            boolean checkClauseEquivalence(final Clause oldClause, final Clause clause)
            {
                return false;
            }
        };
        assertFalse(factory.searchRequestsSameOrQueriesEquivalent(oldSearchRequest, searchRequest));
    }

    @Test
    public void testCombineSortsOnlyParamsSorts() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchSort sortFromParams = new SearchSort("fieldFromParams");

        final Query oldQuery = new QueryImpl(null);
        final SearchRequest oldSR = new SearchRequest(oldQuery);

        final OrderByImpl orderByFromParams = new OrderByImpl(sortFromParams);

        final List<SearchSort> list1 = CollectionBuilder.newBuilder(sortFromParams).asList();

        when(searchSortUtil.getOrderByClause(actionParams.getKeysAndValues()))
                .thenReturn(orderByFromParams);

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);

        final List<SearchSort> result = defaultSearchRequestFactory.combineSorts(oldSR, null, actionParams);

        assertEquals(list1, result);
    }

    @Test
    public void testCombineSortsOnlyOldSorts() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchSort sortFromOldSr = new SearchSort("fieldFromOldSr");

        final Query oldQuery = new QueryImpl(null, new OrderByImpl(sortFromOldSr), "");
        final SearchRequest oldSR = new SearchRequest(oldQuery);

        final OrderByImpl orderByFromParams = new OrderByImpl();

        final List<SearchSort> list1 = CollectionBuilder.newBuilder(sortFromOldSr).asList();

        when(searchSortUtil.getOrderByClause(actionParams.getKeysAndValues()))
                .thenReturn(orderByFromParams);

        when(searchSortUtil.mergeSearchSorts((com.atlassian.crowd.embedded.api.User) null, Collections.<SearchSort>emptyList(), Collections.singletonList(sortFromOldSr), Integer.MAX_VALUE))
                .thenReturn(list1);

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);

        final List<SearchSort> result = defaultSearchRequestFactory.combineSorts(oldSR, null, actionParams);

        assertEquals(list1, result);
    }

    @Test
    public void testCombineSortsNoJqlSortsDropDuplicates() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchSort sortFromOldSr = new SearchSort("fieldFromOldSr");
        final SearchSort sortFromParams = new SearchSort("fieldFromParams");

        final Query oldQuery = new QueryImpl(null, new OrderByImpl(sortFromOldSr), "");
        final SearchRequest oldSR = new SearchRequest(oldQuery);

        final OrderByImpl orderByFromParams = new OrderByImpl(sortFromParams);

        final List<SearchSort> list1 = CollectionBuilder.newBuilder(sortFromParams, sortFromOldSr).asList();

        when(searchSortUtil.getOrderByClause(actionParams.getKeysAndValues()))
                .thenReturn(orderByFromParams);

        when(searchSortUtil.mergeSearchSorts((com.atlassian.crowd.embedded.api.User) null, Collections.singletonList(sortFromParams), Collections.singletonList(sortFromOldSr), Integer.MAX_VALUE))
                .thenReturn(list1);

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);

        final List<SearchSort> result = defaultSearchRequestFactory.combineSorts(oldSR, null, actionParams);

        assertEquals(list1, result);
    }

    @Test
    public void testCombineSortsNoActionParamSorts() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchSort sortFromOldSr = new SearchSort("fieldFromOldSr");

        final Query oldQuery = new QueryImpl(null, new OrderByImpl(sortFromOldSr), "");
        final SearchRequest oldSR = new SearchRequest(oldQuery);

        final OrderByImpl orderByFromParams = new OrderByImpl();

        final List<SearchSort> list1 = CollectionBuilder.newBuilder(sortFromOldSr).asList();
        final List<SearchSort> list2 = CollectionBuilder.newBuilder(sortFromOldSr).asList();

        when(searchSortUtil.getOrderByClause(actionParams.getKeysAndValues()))
                .thenReturn(orderByFromParams);

        when(searchSortUtil.mergeSearchSorts((com.atlassian.crowd.embedded.api.User) null, Collections.<SearchSort>emptyList(), Collections.singletonList(sortFromOldSr), Integer.MAX_VALUE))
                .thenReturn(list1);

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);

        final List<SearchSort> result = defaultSearchRequestFactory.combineSorts(oldSR, null, actionParams);

        assertEquals(list2, result);
    }

    @Test
    public void testCombineSortsNoOldSearchSorts() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchSort sortFromParams = new SearchSort("fieldFromParams");

        final Query oldQuery = new QueryImpl(null);
        final SearchRequest oldSR = new SearchRequest(oldQuery);

        final OrderByImpl orderByFromParams = new OrderByImpl(sortFromParams);

        final List<SearchSort> list1 = CollectionBuilder.newBuilder(sortFromParams).asList();

        when(searchSortUtil.getOrderByClause(actionParams.getKeysAndValues()))
                .thenReturn(orderByFromParams);

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);

        final List<SearchSort> result = defaultSearchRequestFactory.combineSorts(oldSR, null, actionParams);

        assertEquals(list1, result);
    }

    @Test
    public void testCreateFromParametersNullActionParams() throws Exception
    {
        ActionParams actionParams = null;

        final SearchRequest oldSR = mock(SearchRequest.class);

        try
        {
            searchRequestFactory.createFromParameters(oldSR, null, actionParams);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // whened
        }

    }

    @Test
    public void testCreateFromParametersNotModified() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchRequest newSR = mock(SearchRequest.class);
        final SearchRequest oldSR = mock(SearchRequest.class);
        final Clause clause = mock(Clause.class);

        final QueryImpl query = new QueryImpl(clause, new OrderByImpl(Collections.<SearchSort>emptyList()), null);
        newSR.setQuery(query);

        when(oldSR.getQuery()).thenReturn(query);
        newSR.setQuery(query);
        when(oldSR.isModified()).thenReturn(false);
        newSR.setModified(false);


        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            List<Clause> getClausesFromSearchers(final User searchUser, final ActionParams actionParams)
            {
                return Collections.singletonList(clause);
            }

            @Override
            Clause getClause(final List<Clause> clauses)
            {
                return clause;
            }

            @Override
            List<SearchSort> combineSorts(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams actionParameters)
            {
                return Collections.emptyList();
            }

            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return newSR;
            }

            @Override
            boolean simpleSearchRequestsSameOrQueriesEquivalent(final User user, final SearchRequest oldSearchRequest, final SearchRequest newSearchRequest)
            {
                return true;
            }
        };

        defaultSearchRequestFactory.createFromParameters(oldSR, null, actionParams);

    }

    @Test
    public void testCreateFromParametersModified() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchRequest newSR = mock(SearchRequest.class);
        final SearchRequest oldSR = mock(SearchRequest.class);
        final Clause clause = mock(Clause.class);

        final QueryImpl query = new QueryImpl(clause, new OrderByImpl(Collections.<SearchSort>emptyList()), null);
        newSR.setQuery(query);

        newSR.setModified(true);


        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            List<Clause> getClausesFromSearchers(final User searchUser, final ActionParams actionParams)
            {
                return Collections.singletonList(clause);
            }

            @Override
            Clause getClause(final List<Clause> clauses)
            {
                return clause;
            }

            @Override
            List<SearchSort> combineSorts(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams actionParameters)
            {
                return Collections.emptyList();
            }

            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return newSR;
            }

            @Override
            boolean simpleSearchRequestsSameOrQueriesEquivalent(final User user, final SearchRequest oldSearchRequest, final SearchRequest newSearchRequest)
            {
                return false;
            }
        };

        defaultSearchRequestFactory.createFromParameters(oldSR, null, actionParams);

    }

    @Test
    public void testCreateFromParametersAlreadyModified() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchRequest newSR = mock(SearchRequest.class);
        final SearchRequest oldSR = mock(SearchRequest.class);
        final Clause clause = mock(Clause.class);

        final QueryImpl query = new QueryImpl(clause, new OrderByImpl(Collections.<SearchSort>emptyList()), null);
        newSR.setQuery(query);

        when(oldSR.getQuery()).thenReturn(query);
        newSR.setQuery(query);
        when(oldSR.isModified()).thenReturn(true);
        newSR.setModified(true);


        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            List<Clause> getClausesFromSearchers(final User searchUser, final ActionParams actionParams)
            {
                return Collections.singletonList(clause);
            }

            @Override
            Clause getClause(final List<Clause> clauses)
            {
                return clause;
            }

            @Override
            List<SearchSort> combineSorts(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams actionParameters)
            {
                return Collections.emptyList();
            }

            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return newSR;
            }

            @Override
            boolean simpleSearchRequestsSameOrQueriesEquivalent(final User user, final SearchRequest oldSearchRequest, final SearchRequest newSearchRequest)
            {
                return true;
            }
        };

        defaultSearchRequestFactory.createFromParameters(oldSR, null, actionParams);

    }

    @Test
    public void testCreateFromParametersNoOldSR() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchRequest newSR = mock(SearchRequest.class);
        final Clause clause = mock(Clause.class);

        final QueryImpl query = new QueryImpl(clause, new OrderByImpl(Collections.<SearchSort>emptyList()), null);
        newSR.setQuery(query);

        newSR.setModified(false);


        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            List<Clause> getClausesFromSearchers(final User searchUser, final ActionParams actionParams)
            {
                return Collections.singletonList(clause);
            }

            @Override
            Clause getClause(final List<Clause> clauses)
            {
                return clause;
            }

            @Override
            List<SearchSort> combineSorts(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams actionParameters)
            {
                return Collections.emptyList();
            }

            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return newSR;
            }

            @Override
            boolean simpleSearchRequestsSameOrQueriesEquivalent(final User user, final SearchRequest oldSearchRequest, final SearchRequest newSearchRequest)
            {
                return true;
            }
        };

        defaultSearchRequestFactory.createFromParameters(null, null, actionParams);

    }


    @Test
    public void testCreateFromQuerySameOldModifiedQuery() throws Exception
    {
        final SearchRequest oldSearchRequest = mock(SearchRequest.class);
        when(oldSearchRequest.isModified()).thenReturn(true);

        Query query = new QueryImpl();

        final SearchRequest request = mock(SearchRequest.class);
        request.setQuery(query);
        request.setModified(true);

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(null, null, searchService)
        {
            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return request;
            }

            @Override
            boolean searchRequestsSameOrQueriesEquivalent(final SearchRequest oldSearchRequest, final SearchRequest searchRequest)
            {
                return true;
            }
        };

        defaultSearchRequestFactory.createFromQuery(oldSearchRequest, null, query);
    }

    @Test
    public void testCreateFromQuerySameOldNotModifiedQuery() throws Exception
    {
        final SearchRequest oldSearchRequest = mock(SearchRequest.class);
        when(oldSearchRequest.isModified()).thenReturn(false);

        Query query = new QueryImpl();

        final SearchRequest request = mock(SearchRequest.class);
        request.setQuery(query);
        request.setModified(false);

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(null, null, searchService)
        {
            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return request;
            }

            @Override
            boolean searchRequestsSameOrQueriesEquivalent(final SearchRequest oldSearchRequest, final SearchRequest searchRequest)
            {
                return true;
            }
        };

        defaultSearchRequestFactory.createFromQuery(oldSearchRequest, null, query);
    }

    @Test
    public void testCreateFromQueryNullOldQuery() throws Exception
    {
        Query query = new QueryImpl();

        final SearchRequest request = mock(SearchRequest.class);
        request.setQuery(query);
        request.setModified(false);

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(null, null, searchService)
        {
            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return request;
            }
        };

        defaultSearchRequestFactory.createFromQuery(null, null, query);

    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesEquuivalentAttributesNotEqual() throws Exception
    {
        final SearchRequest oldSR = mock(SearchRequest.class);
        final SearchRequest newSR = mock(SearchRequest.class);

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);

        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return false;
            }
        };

        assertFalse(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesOrderByClausesNotEqual() throws Exception
    {
        final SearchRequest oldSR = mock(SearchRequest.class);
        final SearchRequest newSR = mock(SearchRequest.class);

       when(oldSR.getQuery()).thenReturn(new QueryImpl());
       when(newSR.getQuery()).thenReturn(new QueryImpl());

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);
        final AtomicBoolean checkOrderByClausesCalled = new AtomicBoolean(false);

        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return true;
            }

            @Override
            boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
            {
                checkOrderByClausesCalled.set(true);
                return false;
            }
        };

        assertFalse(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        assertTrue(checkOrderByClausesCalled.get());
    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesQueryContextsNotEqual() throws Exception
    {
        final SearchRequest oldSR = mock(SearchRequest.class);
        final SearchRequest newSR = mock(SearchRequest.class);

        final QueryContext newQC = mock(QueryContext.class);
        final QueryContext oldQC = mock(QueryContext.class);

        final Query oldQuery = mock(Query.class);
        final Query newQuery = mock(Query.class);

        when(oldSR.getQuery()).thenReturn(oldQuery);
        when(newSR.getQuery()).thenReturn(newQuery);

        when(searchService.getQueryContext(null, newQuery)).thenReturn(newQC);
        when(searchService.getQueryContext(null, oldQuery)).thenReturn(oldQC);

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);
        final AtomicBoolean checkOrderByClausesCalled = new AtomicBoolean(false);

        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return true;
            }

            @Override
            boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
            {
                checkOrderByClausesCalled.set(true);
                return true;
            }
        };

        assertFalse(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        assertTrue(checkOrderByClausesCalled.get());
    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesSearchContextsNotEqual() throws Exception
    {
        final SearchRequest oldSR = mock(SearchRequest.class);
        final SearchRequest newSR = mock(SearchRequest.class);

        final QueryContext queryContext = mock(QueryContext.class);

        final SearchContext newSC = mock(SearchContext.class);
        final SearchContext oldSC = mock(SearchContext.class);

        final Query oldQuery = mock(Query.class);
        final Query newQuery = mock(Query.class);

        when(oldSR.getQuery()).thenReturn(oldQuery);
        when(newSR.getQuery()).thenReturn(newQuery);

        when(searchService.getQueryContext(null, newQuery)).thenReturn(queryContext);
        when(searchService.getQueryContext(null, oldQuery)).thenReturn(queryContext);

        when(searchService.getSearchContext(null, newQuery)).thenReturn(newSC);
        when(searchService.getSearchContext(null, oldQuery)).thenReturn(oldSC);

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);
        final AtomicBoolean checkOrderByClausesCalled = new AtomicBoolean(false);

        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return true;
            }

            @Override
            boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
            {
                checkOrderByClausesCalled.set(true);
                return true;
            }
        };

        assertFalse(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        assertTrue(checkOrderByClausesCalled.get());
    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesClauseNotEqual() throws Exception
    {
        final FieldValuesHolder newHolder = new FieldValuesHolderImpl();
        final FieldValuesHolder oldHolder = new FieldValuesHolderImpl();

        final IssueSearcher issueSearcher = mock(IssueSearcher.class);
        final SearchInputTransformer transformer = mock(SearchInputTransformer.class);

        final QueryContext queryContext = mock(QueryContext.class);

        final SearchRequest oldSR = mock(SearchRequest.class);
        final SearchRequest newSR = mock(SearchRequest.class);

        final SearchContext searchContext = mock(SearchContext.class);

        final QueryImpl oldQuery = new QueryImpl();
        final QueryImpl newQuery = new QueryImpl();

        when(oldSR.getQuery()).thenReturn(oldQuery);
        when(newSR.getQuery()).thenReturn(newQuery);

        when(searchService.getQueryContext((User) null, newQuery)).thenReturn(queryContext);
        when(searchService.getQueryContext((User) null, oldQuery)).thenReturn(queryContext);

        when(searchService.getSearchContext((User) null, newQuery)).thenReturn(searchContext);
        when(searchService.getSearchContext((User) null, oldQuery)).thenReturn(searchContext);

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);
        final AtomicBoolean checkOrderByClausesCalled = new AtomicBoolean(false);

        when(issueSearcherManager.getAllSearchers()).thenReturn(CollectionBuilder.<IssueSearcher<?>>newBuilder(issueSearcher).asList());
        when(issueSearcher.getSearchInputTransformer()).thenReturn(transformer);

        transformer.populateFromQuery(null, newHolder, newQuery, searchContext);
        transformer.populateFromQuery(null, oldHolder, newQuery, searchContext);

        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return true;
            }

            @Override
            boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
            {
                checkOrderByClausesCalled.set(true);
                return true;
            }

            @Override
            boolean holdersEqual(final FieldValuesHolder oldHolder, final FieldValuesHolder newHolder)
            {
                return false;
            }
        };

        assertFalse(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        assertTrue(checkOrderByClausesCalled.get());
    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesClauseEqual() throws Exception
    {
        final FieldValuesHolder newHolder = new FieldValuesHolderImpl();
        final FieldValuesHolder oldHolder = new FieldValuesHolderImpl();

        final IssueSearcher issueSearcher = mock(IssueSearcher.class);
        final SearchInputTransformer transformer = mock(SearchInputTransformer.class);

        final QueryContext queryContext = mock(QueryContext.class);

        final SearchRequest oldSR = mock(SearchRequest.class);
        final SearchRequest newSR = mock(SearchRequest.class);

        final SearchContext searchContext = mock(SearchContext.class);

        final QueryImpl oldQuery = new QueryImpl();
        final QueryImpl newQuery = new QueryImpl();

        when(oldSR.getQuery()).thenReturn(oldQuery);
        when(newSR.getQuery()).thenReturn(newQuery);

        when(searchService.getQueryContext((User) null, newQuery)).thenReturn(queryContext);
        when(searchService.getQueryContext((User) null, oldQuery)).thenReturn(queryContext);

        when(searchService.getSearchContext((User) null, newQuery)).thenReturn(searchContext);
        when(searchService.getSearchContext((User) null, oldQuery)).thenReturn(searchContext);

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);
        final AtomicBoolean checkOrderByClausesCalled = new AtomicBoolean(false);

        when(issueSearcherManager.getAllSearchers()).thenReturn(CollectionBuilder.<IssueSearcher<?>>newBuilder(issueSearcher).asList());
        when(issueSearcher.getSearchInputTransformer()).thenReturn(transformer);

        transformer.populateFromQuery(null, newHolder, newQuery, searchContext);
        transformer.populateFromQuery(null, oldHolder, newQuery, searchContext);

        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return true;
            }

            @Override
            boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
            {
                checkOrderByClausesCalled.set(true);
                return true;
            }

            @Override
            boolean holdersEqual(final FieldValuesHolder oldHolder, final FieldValuesHolder newHolder)
            {
                return true;
            }
        };

        assertTrue(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        assertTrue(checkOrderByClausesCalled.get());
    }


    private MockJqlSearchRequest createSR(Long id, Query query)
    {
        return new MockJqlSearchRequest(id, query);
    }

    private void assertNotEquiv(final SearchRequest oldSearchRequest, final SearchRequest searchRequest)
    {
        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(null, null, searchService);
        assertFalse(factory.searchRequestsSameOrQueriesEquivalent(oldSearchRequest, searchRequest));
    }

    private void assertEquiv(final MockJqlSearchRequest oldSearchRequest, final MockJqlSearchRequest searchRequest)
    {
        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(null, null, searchService);
        assertTrue(factory.searchRequestsSameOrQueriesEquivalent(oldSearchRequest, searchRequest));
    }
}
