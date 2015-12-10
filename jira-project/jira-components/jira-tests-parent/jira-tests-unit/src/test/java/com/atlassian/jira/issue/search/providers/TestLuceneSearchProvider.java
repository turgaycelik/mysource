package com.atlassian.jira.issue.search.providers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.instrumentation.operations.SimpleOpTimer;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.TotalHitsAwareCollector;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.optimizers.QueryOptimizationService;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionsFilterGenerator;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.jql.query.LuceneQueryBuilder;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.SearchSort;

public class TestLuceneSearchProvider
{

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private IssueFactory issueFactory;

    @Mock
    private SearchProviderFactory searchProviderFactory;

    @Mock
    private IndexSearcher indexSearcher;

    @Mock
    private SearchHandlerManager searchHandlerManager;

    @Mock
    @AvailableInContainer
    private FieldManager fieldManager;

    @Mock
    private SearchSortUtil searchSortUtil;

    @Mock
    private LuceneQueryBuilder luceneQueryBuilder;

    @Mock
    private PermissionsFilterGenerator permissionsFilterGenerator;

    @Mock
    @AvailableInContainer
    private InstrumentRegistry instrumentRegistry;

    @Mock
    private UserKeyService userKeyService;

    @Mock
    private QueryOptimizationService queryOptimizationService;

    private LuceneSearchProvider testedObject;

    @Before
    public void onTestUp()
    {
        testedObject = new LuceneSearchProvider(issueFactory, searchProviderFactory, permissionsFilterGenerator, searchHandlerManager,
                searchSortUtil, luceneQueryBuilder, queryOptimizationService);
        when(searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX)).thenReturn(indexSearcher);
        when(instrumentRegistry.pullTimer(InstrumentationName.ISSUE_INDEX_READS.getInstrumentName())).thenAnswer(new Answer<OpTimer>()
        {

            @Override
            public OpTimer answer(final InvocationOnMock invocation) throws Throwable
            {
                return new SimpleOpTimer((String) invocation.getArguments()[0]);
            }

        });
        when(fieldManager.isNavigableField(Mockito.anyString())).thenReturn(true);
        when(queryOptimizationService.optimizeQuery(any(Query.class))) .thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                return invocation.getArguments()[0];
            }
        });
    }

    @Test
    public void testSearchWithUserNullQuery() throws Exception
    {
        final User searcher = newUserMock();
        final PagerFilter<Issue> pager = new PagerFilter<Issue>();

        final SearchResults searchResult = testedObject.search(null, searcher, pager);
        assertEquals(Collections.emptyList(), searchResult.getIssues());
        assertEquals(0, searchResult.getTotal());
        assertEquals(pager.getStart(), searchResult.getStart());
    }

    @Test
    public void testSearchWithApplicationUserNullQuery() throws Exception
    {
        final ApplicationUser searcher = newApplicationUserMock();
        final PagerFilter<Issue> pager = new PagerFilter<Issue>();

        final SearchResults searchResult = testedObject.search(null, searcher, pager);
        assertEquals(Collections.emptyList(), searchResult.getIssues());
        assertEquals(0, searchResult.getTotal());
        assertEquals(pager.getStart(), searchResult.getStart());
    }

    @Test
    public void testSearchWithUserSearchWithoutOverflow() throws Exception
    {
        final User searcher = newUserMock();
        final MockQuery mockQuery = new MockQuery();
        final PagerFilter<Issue> pager = new PagerFilter<Issue>();
        final Issue[] issues = new Issue[] { mock(Issue.class), mock(Issue.class), mock(Issue.class) };
        final TopDocs topDocs = newTopDocsMock(issues);

        when(indexSearcher.search(Mockito.eq(mockQuery.getLuceneQuery()), Mockito.<Filter> any(), Mockito.eq(pager.getEnd()))).thenReturn(
                topDocs);

        final SearchResults searchResult = testedObject.search(mockQuery.getQuery(), searcher, pager);
        assertEquals(Arrays.asList(issues), searchResult.getIssues());
        assertEquals(issues.length, searchResult.getTotal());
        assertEquals(pager.getStart(), searchResult.getStart());
    }

    @Test
    public void testSearchWithUserSearchWithOverflow() throws Exception
    {
        final User searcher = newUserMock();
        final MockQuery mockQuery = new MockQuery();
        final PagerFilter<Issue> pager = new PagerFilter<Issue>(4, 20);
        final Issue[] issues = new Issue[] { mock(Issue.class), mock(Issue.class), mock(Issue.class) };
        final TopDocs topDocs = newTopDocsMock(issues);

        when(indexSearcher.search(Mockito.eq(mockQuery.getLuceneQuery()), Mockito.<Filter> any(), Mockito.eq(pager.getEnd()))).thenReturn(
                topDocs);

        final SearchResults searchResult = testedObject.search(mockQuery.getQuery(), searcher, pager);
        assertEquals(Collections.emptyList(), searchResult.getIssues());
        assertEquals(issues.length, searchResult.getTotal());
        assertEquals(pager.getStart(), searchResult.getStart());
    }

    @Test
    public void testSearchWithUserCollector() throws Exception
    {
        final User searcher = newUserMock();
        final MockQuery mockQuery = new MockQuery();
        final Collector collector = mock(Collector.class);

        testedObject.search(mockQuery.getQuery(), searcher, collector);
        verify(indexSearcher).search(Mockito.eq(mockQuery.getLuceneQuery()), Mockito.<Filter> any(), Mockito.eq(collector));
    }

    @Test
    public void testSearchWithApplicationUserCollector() throws Exception
    {
        final ApplicationUser searcher = newApplicationUserMock();
        final MockQuery mockQuery = new MockQuery();
        final Collector collector = mock(Collector.class);

        testedObject.search(mockQuery.getQuery(), searcher, collector);
        verify(indexSearcher).search(Mockito.eq(mockQuery.getLuceneQuery()), Mockito.<Filter> any(), Mockito.eq(collector));
    }

    @Test
    public void testSearchWithUserSearchAndQueryWithCollector() throws Exception
    {
        final User searcher = newUserMock();
        final MockQuery mockQuery = new MockQuery();
        final Collector collector = mock(Collector.class);

        final org.apache.lucene.search.Query andQuery = new TermQuery(new Term("fakeField", "fakeValue"));
        testedObject.search(mockQuery.getQuery(), searcher, collector, andQuery);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(andQuery, BooleanClause.Occur.MUST);
        expectedQuery.add(mockQuery.getLuceneQuery(), BooleanClause.Occur.MUST);
        verify(indexSearcher).search(Mockito.eq(expectedQuery), Mockito.<Filter> any(), Mockito.eq(collector));
    }

    @Test
    public void testSearchWithUserIOException_search() throws Exception
    {
        final IOException ioException = new IOException("Fake IO exception");
        expectedException.expect(SearchException.class);
        expectedException.expectMessage(ioException.getMessage());

        final User searcher = newUserMock();
        final MockQuery mockQuery = new MockQuery();
        final PagerFilter<Issue> pager = new PagerFilter<Issue>(0, 20);
        when(indexSearcher.search(Mockito.eq(mockQuery.getLuceneQuery()), Mockito.<Filter> any(), Mockito.eq(pager.getEnd()))).thenThrow(
                ioException);
        testedObject.search(mockQuery.getQuery(), searcher, pager);
    }

    @Test
    public void testSearchWithUserSearchWithOverrideSecurity() throws Exception
    {
        final User searcher = newUserMock();
        final MockQuery mockQuery = new MockQuery();
        final PagerFilter<Issue> pager = new PagerFilter<Issue>();
        final Issue[] issues = new Issue[] { mock(Issue.class), mock(Issue.class), mock(Issue.class) };
        final TopDocs topDocs = newTopDocsMock(issues);

        final org.apache.lucene.search.Query andQuery = new TermQuery(new Term("fakeField", "fakeValue"));

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(andQuery, BooleanClause.Occur.MUST);
        expectedQuery.add(mockQuery.getLuceneQuery(), BooleanClause.Occur.MUST);

        when(indexSearcher.search(Mockito.eq(expectedQuery), Mockito.<Filter> any(), Mockito.eq(pager.getEnd()))).thenReturn(topDocs);

        final SearchResults searchResult = testedObject.searchOverrideSecurity(mockQuery.getQuery(), searcher, pager, andQuery);
        assertEquals(Arrays.asList(issues), searchResult.getIssues());
        assertEquals(issues.length, searchResult.getTotal());
        assertEquals(pager.getStart(), searchResult.getStart());
    }

    @Test
    public void testSearchWithUserOverrideSecurityCollector() throws Exception
    {
        final User searcher = newUserMock();
        final MockQuery mockQuery = new MockQuery();
        final Collector collector = mock(Collector.class);
        testedObject.searchOverrideSecurity(mockQuery.getQuery(), searcher, collector);
        verify(indexSearcher).search(Mockito.eq(mockQuery.getLuceneQuery()), Mockito.<Filter> any(), Mockito.eq(collector));
    }

    @Test
    public void testSearchWithApplicationUserOverrideSecurityCollector() throws Exception
    {
        final ApplicationUser searcher = newApplicationUserMock();
        final MockQuery mockQuery = new MockQuery();
        final Collector collector = mock(MockTotalHitsAwareCollector.class);
        testedObject.searchOverrideSecurity(mockQuery.getQuery(), searcher, collector);
        verify(indexSearcher).search(Mockito.eq(mockQuery.getLuceneQuery()), Mockito.<Filter> any(), Mockito.eq(collector));
    }

    @Test
    public void testSearchAndSortWithUser() throws Exception
    {
        final User searcher = newUserMock();
        final MockQuery mockQuery = new MockQuery();
        final PagerFilter<Issue> pager = new PagerFilter<Issue>();
        final Issue[] issues = new Issue[] { mock(Issue.class), mock(Issue.class), mock(Issue.class) };
        final SearchSort sort = new SearchSort("fakeField");
        final SortField[] sortFields = new SortField[] { new SortField(sort.getField(), SortField.STRING) };
        final TopFieldDocs topDocs = newTopFieldDocsMock(sortFields, issues);
        final MockTotalHitsAwareCollector collector = mock(MockTotalHitsAwareCollector.class);

        when(searchSortUtil.getSearchSorts(mockQuery.getQuery())).thenReturn(Arrays.asList(sort));
        when(searchHandlerManager.getFieldIds(searcher, sort.getField())).thenReturn(Collections.singletonList(sort.getField()));
        final NavigableField field = mock(NavigableField.class);

        when(field.getSortFields(Boolean.FALSE)).thenReturn(Arrays.asList(sortFields));
        when(fieldManager.getNavigableField(sort.getField())).thenReturn(field);
        when(
                indexSearcher.search(Mockito.eq(mockQuery.getLuceneQuery()), Mockito.<Filter> any(), Mockito.eq(pager.getEnd()),
                        Mockito.eq(new Sort(sortFields)))).thenReturn(topDocs);
        testedObject.searchAndSort(mockQuery.getQuery(), searcher, collector, pager);
        verify(collector).setTotalHits(issues.length);
    }

    @Test
    public void testSearchAndSortWithApplicationUser() throws Exception
    {
        final ApplicationUser searcher = newApplicationUserMock();
        final MockQuery mockQuery = new MockQuery();
        final PagerFilter<Issue> pager = new PagerFilter<Issue>();
        final Issue[] issues = new Issue[] { mock(Issue.class), mock(Issue.class), mock(Issue.class) };
        final SearchSort sort = new SearchSort("fakeField");
        final SortField[] sortFields = new SortField[] { new SortField(sort.getField(), SortField.STRING) };
        final TopFieldDocs topDocs = newTopFieldDocsMock(sortFields, issues);
        final MockTotalHitsAwareCollector collector = mock(MockTotalHitsAwareCollector.class);

        when(searchSortUtil.getSearchSorts(mockQuery.getQuery())).thenReturn(Arrays.asList(sort));
        when(searchHandlerManager.getFieldIds(ApplicationUsers.toDirectoryUser(searcher), sort.getField())).thenReturn(
                Collections.singletonList(sort.getField()));
        final NavigableField field = mock(NavigableField.class);

        when(field.getSortFields(Boolean.FALSE)).thenReturn(Arrays.asList(sortFields));
        when(fieldManager.getNavigableField(sort.getField())).thenReturn(field);
        when(
                indexSearcher.search(Mockito.eq(mockQuery.getLuceneQuery()), Mockito.<Filter> any(), Mockito.eq(pager.getEnd()),
                        Mockito.eq(new Sort(sortFields)))).thenReturn(topDocs);
        testedObject.searchAndSort(mockQuery.getQuery(), searcher, collector, pager);
        verify(collector).setTotalHits(issues.length);
    }

    @Test
    public void testSearchWithApplicationUserSearchWithScurityOverride() throws Exception
    {
        final ApplicationUser searcher = newApplicationUserMock();
        final MockQuery mockQuery = new MockQuery();
        final PagerFilter<Issue> pager = new PagerFilter<Issue>();
        final Issue[] issues = new Issue[] { mock(Issue.class), mock(Issue.class), mock(Issue.class) };
        final TopDocs topDocs = newTopDocsMock(issues);

        final org.apache.lucene.search.Query andQuery = new TermQuery(new Term("fakeField", "fakeValue"));

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(andQuery, BooleanClause.Occur.MUST);
        expectedQuery.add(mockQuery.getLuceneQuery(), BooleanClause.Occur.MUST);

        when(indexSearcher.search(Mockito.eq(expectedQuery), Mockito.<Filter> any(), Mockito.eq(pager.getEnd()))).thenReturn(topDocs);

        final SearchResults searchResult = testedObject.searchOverrideSecurity(mockQuery.getQuery(), searcher, pager, andQuery);
        assertEquals(Arrays.asList(issues), searchResult.getIssues());
        assertEquals(issues.length, searchResult.getTotal());
        assertEquals(pager.getStart(), searchResult.getStart());
    }

    @Test
    public void testSearchCountWithUserNullQuery() throws Exception
    {
        final User searcher = newUserMock();
        assertEquals(0, testedObject.searchCount(null, searcher));
    }

    @Test
    public void testSearchCountWithUserNotNullQuery() throws Exception
    {
        final User searcher = newUserMock();
        final MockQuery mockQuery = new MockQuery();
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                ((TotalHitCountCollector) invocation.getArguments()[2]).collect(0);
                return null;
            }

        }).when(indexSearcher).search(eq(mockQuery.getLuceneQuery()), (Filter) any(), (Collector) any());
        assertEquals(1, testedObject.searchCount(mockQuery.getQuery(), searcher));
    }

    @Test
    public void testSearchCountWithApplicationUserNotNullQuery() throws Exception
    {
        final ApplicationUser searcher = newApplicationUserMock();
        final MockQuery mockQuery = new MockQuery();
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                ((TotalHitCountCollector) invocation.getArguments()[2]).collect(0);
                return null;
            }

        }).when(indexSearcher).search(eq(mockQuery.getLuceneQuery()), (Filter) any(), (Collector) any());
        assertEquals(1, testedObject.searchCount(mockQuery.getQuery(), searcher));
    }

    @Test
    public void testSearchCountWithUserAndOverrideSecurity() throws Exception
    {
        final User searcher = newUserMock();
        final MockQuery mockQuery = new MockQuery();
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                ((TotalHitCountCollector) invocation.getArguments()[2]).collect(0);
                return null;
            }

        }).when(indexSearcher).search(eq(mockQuery.getLuceneQuery()), (Filter) any(), (Collector) any());
        assertEquals(1, testedObject.searchCountOverrideSecurity(mockQuery.getQuery(), searcher));
    }

    @Test
    public void testSearchCountWithApplicationUserAndOverrideSecurity() throws Exception
    {
        final ApplicationUser searcher = newApplicationUserMock();
        final MockQuery mockQuery = new MockQuery();
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                ((TotalHitCountCollector) invocation.getArguments()[2]).collect(0);
                return null;
            }

        }).when(indexSearcher).search(eq(mockQuery.getLuceneQuery()), (Filter) any(), (Collector) any());
        assertEquals(1, testedObject.searchCountOverrideSecurity(mockQuery.getQuery(), searcher));
    }

    @Test
    public void testSearchWithApplicationUserSearchAndQueryWithCollector() throws Exception
    {
        final ApplicationUser searcher = newApplicationUserMock();
        final MockQuery mockQuery = new MockQuery();
        final Collector collector = mock(Collector.class);

        final org.apache.lucene.search.Query andQuery = new TermQuery(new Term("fakeField", "fakeValue"));
        testedObject.search(mockQuery.getQuery(), searcher, collector, andQuery);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(andQuery, BooleanClause.Occur.MUST);
        expectedQuery.add(mockQuery.getLuceneQuery(), BooleanClause.Occur.MUST);
        verify(indexSearcher).search(Mockito.eq(expectedQuery), Mockito.<Filter> any(), Mockito.eq(collector));
    }

    private User newUserMock()
    {
        final User result = mock(User.class);
        final String username = "mockUserName";
        when(result.getName()).thenReturn(username);
        when(userKeyService.getKeyForUsername(username)).thenReturn(username);
        return result;
    }

    private ApplicationUser newApplicationUserMock()
    {
        final ApplicationUser result = mock(ApplicationUser.class);
        when(result.getName()).thenReturn("mockUsername");
        return result;
    }

    private TopDocs newTopDocsMock(final Issue... issues) throws Exception
    {
        final ScoreDoc[] result = new ScoreDoc[issues.length];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = new ScoreDoc(i, 1);
            final Document document = new Document();
            when(indexSearcher.doc(result[i].doc)).thenReturn(document);
            when(issueFactory.getIssue(document)).thenReturn(issues[i]);
        }
        return new TopDocs(result.length, result, 1);
    }

    private TopFieldDocs newTopFieldDocsMock(final SortField[] fields, final Issue... issues) throws Exception
    {
        final ScoreDoc[] result = new ScoreDoc[issues.length];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = new ScoreDoc(i, 1);
            final Document document = new Document();
            when(indexSearcher.doc(result[i].doc)).thenReturn(document);
            when(issueFactory.getIssue(document)).thenReturn(issues[i]);
        }
        return new TopFieldDocs(result.length, result, fields, 1);
    }

    // purpose of this mock is to create base class for an Collector, which implements TotalHitsAwareCollector
    private static abstract class MockTotalHitsAwareCollector extends Collector implements TotalHitsAwareCollector
    {
    }

    // creates mock query, which represents bridge between atlassian query and appropriate lucene query
    private final class MockQuery
    {

        private final Query query;
        private final org.apache.lucene.search.Query luceneQuery;

        public MockQuery() throws SearchException
        {
            query = mock(Query.class);
            luceneQuery = mock(org.apache.lucene.search.Query.class);
            final Clause clause = mock(Clause.class);

            when(query.getWhereClause()).thenReturn(clause);
            when(luceneQueryBuilder.createLuceneQuery((QueryCreationContext) any(), eq(clause))).thenReturn(luceneQuery);
            when(luceneQuery.toString()).thenReturn(MockQuery.class.getCanonicalName());
        }

        public Query getQuery()
        {
            return query;
        }

        public org.apache.lucene.search.Query getLuceneQuery()
        {
            return luceneQuery;
        }

    }

}
