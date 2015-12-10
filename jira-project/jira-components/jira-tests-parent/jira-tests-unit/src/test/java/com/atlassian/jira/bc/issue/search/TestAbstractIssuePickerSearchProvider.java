package com.atlassian.jira.bc.issue.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.analyzer.EnglishAnalyzer;
import com.atlassian.jira.issue.index.analyzer.TokenFilters;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.QueryImpl;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createControl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.bc.issue.search.AbstractIssuePickerSearchProvider}.
 *
 * @since v4.2
 */
public class TestAbstractIssuePickerSearchProvider
{
    private static final String NO_FILTER_LUCENCEQSTR = "(key:WEB* key:SEARCH*) ((keynumpart:WEB* keynumpart:SEARCH*)^1.5) (summary:web* summary:search*) (summary:web summary:search) key:WEB SEARCH^2.0 keynumpart:WEB SEARCH^1.8";
    private static final String WEB_SEARCH = "web search";

    private JiraServiceContext jiraServiceContext;

    @Before
    public void setUp() throws Exception
    {
        jiraServiceContext = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());
    }

    @After
    public void tearDown() throws Exception
    {
        jiraServiceContext = null;
    }

    @Test
    public void testCreateQueryNoQuery()
    {
        AbstractIssuePickerSearchProvider provider = new SimpleProvider();
        provider.summaryAnalyzer =
                new EnglishAnalyzer
                        (
                                LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(),
                                TokenFilters.English.StopWordRemoval.defaultSet()
                        );

        org.apache.lucene.search.Query query = provider.createQuery(null, null, null);
        assertNull(query);
    }

    @Test
    public void testCreateQuerySingleTerm()
    {
        AbstractIssuePickerSearchProvider provider = new SimpleProvider();
        provider.summaryAnalyzer =
                new EnglishAnalyzer
                        (
                                LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(),
                                TokenFilters.English.StopWordRemoval.defaultSet()
                        );

        final Set<String> keyTerms = new LinkedHashSet<String>();
        final Set<String> summaryTerms = new LinkedHashSet<String>();
        org.apache.lucene.search.Query query = provider.createQuery("web", keyTerms, summaryTerms);
        assertNotNull(query);
        assertEquals("(key:WEB*) ((keynumpart:WEB*)^1.5) (summary:web*) (summary:web) key:WEB^2.0 keynumpart:WEB^1.8", query.toString());
        assertEquals(Collections.singleton("WEB"), keyTerms);
        assertEquals(Collections.singleton("web"), summaryTerms);
    }

    @Test
    public void testCreateQueryMultipleTerms()
    {
        final Set<String> keyTerms = new LinkedHashSet<String>();
        final Set<String> summaryTerms = new LinkedHashSet<String>();
        AbstractIssuePickerSearchProvider provider = new SimpleProvider();
        provider.summaryAnalyzer =
                new EnglishAnalyzer
                        (
                                LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(),
                                TokenFilters.English.StopWordRemoval.defaultSet()
                        );

        org.apache.lucene.search.Query query = provider.createQuery(WEB_SEARCH, keyTerms, summaryTerms);
        assertNotNull(query);
        assertEquals(NO_FILTER_LUCENCEQSTR, query.toString());

        assertCollectionEquals(Arrays.asList("WEB", "SEARCH"), keyTerms);
        assertCollectionEquals(Arrays.asList("web", "search"), summaryTerms);
    }

    @Test
    public void testCreateNullFilterQuery()
    {
        AbstractIssuePickerSearchProvider provider = new SimpleProvider();

        IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, true, true, 50);
        org.apache.lucene.search.Query query = provider.addFilterToQuery(params, null);
        assertNull(query);
    }

    @Test
    public void testCreateEmptyFilterQuery()
    {
        AbstractIssuePickerSearchProvider provider = new SimpleProvider();

        IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, true, true, 50);
        BooleanQuery origQuery = new BooleanQuery();
        org.apache.lucene.search.Query query = provider.addFilterToQuery(params, origQuery);
        assertNotNull(query);
        assertEquals("+()", query.toString());
    }

    @Test
    public void testCreateEmptyFilterQueryWithNonEmptyOrig()
    {
        AbstractIssuePickerSearchProvider provider = new SimpleProvider();

        IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, true, true, 50);
        BooleanQuery origQuery = new BooleanQuery();
        origQuery.add(new TermQuery(new Term("key", "KEY")), BooleanClause.Occur.MUST);
        org.apache.lucene.search.Query query = provider.addFilterToQuery(params, origQuery);
        assertNotNull(query);
        assertEquals("+(+key:KEY)", query.toString());
    }

    @Test
    public void testCreateFilterQueryWithNonEmptyOrigAndIssue()
    {
        AbstractIssuePickerSearchProvider provider = new SimpleProvider();

        Issue issue = getMockIssueWithParent("JRA-123", "JRA-123 Summary", null);
        IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, issue, null, true, true, 50);
        BooleanQuery origQuery = new BooleanQuery();
        origQuery.add(new TermQuery(new Term("key", "KEY")), BooleanClause.Occur.MUST);
        org.apache.lucene.search.Query query = provider.addFilterToQuery(params, origQuery);
        assertNotNull(query);
        assertEquals("+(+key:KEY) -key:JRA-123", query.toString());
    }

    @Test
    public void testCreateFilterQueryWithNonEmptyOrigAndProject()
    {
        AbstractIssuePickerSearchProvider provider = new SimpleProvider();

        Project project = new MockProject(123L);
        Issue issue = getMockIssueWithParent("JRA-123", "JRA-123 Summary", null);
        IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, issue, project, true, true, 50);
        BooleanQuery origQuery = new BooleanQuery();
        origQuery.add(new TermQuery(new Term("key", "KEY")), BooleanClause.Occur.MUST);
        org.apache.lucene.search.Query query = provider.addFilterToQuery(params, origQuery);
        assertNotNull(query);
        assertEquals("+(+key:KEY) -key:JRA-123 +projid:123", query.toString());
    }

    @Test
    public void testCreateFilterQueryWithNonEmptyOrigAndshowParentFalse()
    {
        AbstractIssuePickerSearchProvider provider = new SimpleProvider();

        Project project = new MockProject(123L);
        Issue issue = getMockIssueWithParent("JRA-123", "JRA-123 Summary", getMockIssue("JRA-321", "PArent Issue"));
        IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, issue, project, true, false, 50);
        BooleanQuery origQuery = new BooleanQuery();
        origQuery.add(new TermQuery(new Term("key", "KEY")), BooleanClause.Occur.MUST);
        org.apache.lucene.search.Query query = provider.addFilterToQuery(params, origQuery);
        assertNotNull(query);
        assertEquals("+(+key:KEY) -key:JRA-123 +projid:123 -key:JRA-321", query.toString());
    }

    @Test
    public void testCreateFilterQueryWithNonEmptyOrigAndshowSubsFalse()
    {
        final IMocksControl iMocksControl = createControl();

        IssueType it1 = iMocksControl.createMock(IssueType.class);
        expect(it1.getId()).andReturn("1234");
        IssueType it2 = iMocksControl.createMock(IssueType.class);
        expect(it2.getId()).andReturn("9876");

        ConstantsManager cMgr = iMocksControl.createMock(ConstantsManager.class);
        expect(cMgr.getRegularIssueTypeObjects()).andReturn(Arrays.asList(it1, it2));

        AbstractIssuePickerSearchProvider provider = new SimpleProvider(null, cMgr, null);

        iMocksControl.replay();

        Issue issue = getMockIssueWithParent("JRA-123", "JRA-123 Summary", getMockIssue("JRA-321", "PArent Issue"));
        IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, issue, null, false, true, 50);
        BooleanQuery origQuery = new BooleanQuery();
        origQuery.add(new TermQuery(new Term("key", "KEY")), BooleanClause.Occur.MUST);
        org.apache.lucene.search.Query query = provider.addFilterToQuery(params, origQuery);
        assertNotNull(query);
        assertEquals("+(+key:KEY) -key:JRA-123 +(type:1234 type:9876)", query.toString());

        iMocksControl.verify();
    }

    @Test
    public void testGetIssues() throws Exception
    {
        final int maxCount = 20;

        //The Query created from the "query" given by the user.
        final Query createQuery = new TermQuery(new Term("a", "b"));

        //The query created after the filter terms have been added.
        final BooleanQuery createQueryWithFilter = new BooleanQuery();
        createQueryWithFilter.add(createQuery, BooleanClause.Occur.MUST);
        createQueryWithFilter.add(new TermQuery(new Term("c", "c")), BooleanClause.Occur.MUST);

        //The query created after passed through the modifier to add the "MATCH ALL" term.
        final BooleanQuery modifiedQuery = new BooleanQuery();
        modifiedQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        modifiedQuery.add(createQueryWithFilter, BooleanClause.Occur.MUST);

        final IMocksControl mocksControl = createControl();

        final List<String> keyTermsExpected = Arrays.asList("a", "b");
        final List<String> summaryTermsExpected = Arrays.asList("c", "d");

        //The list of issues to return.
        final List<Issue> issueList = new ArrayList<Issue>();
        issueList.add(getMockIssue("JRA-123", "JRA-123 Summary"));
        issueList.add(getMockIssue("JRA-124", "JRA-124 Summary"));
        issueList.add(getMockIssue("JRA-125", "JRA-125 Summary"));

        //The parameters we will be testing.
        final IssuePickerSearchService.IssuePickerParameters testParams = new IssuePickerSearchService.IssuePickerParameters(WEB_SEARCH, null, null, null, true, true, maxCount);

        //The results returned from the search service.
        final SearchResults searchResults = new SearchResults(issueList, new PagerFilter<Issue>(maxCount));

        //Prime the modifier with expected behaviour.
        final LuceneQueryModifier modifier = mocksControl.createMock(LuceneQueryModifier.class);
        expect(modifier.getModifiedQuery(createQueryWithFilter)).andReturn(modifiedQuery);

        //Prime the search provider with expected behaviour.
        final SearchProvider mockSearchProvider = mocksControl.createMock(SearchProvider.class);
        final Capture<PagerFilter<Issue>> pager = new Capture<PagerFilter<Issue>>();
        expect(mockSearchProvider.search(EasyMock.eq(new QueryImpl()), EasyMock.<User>isNull(), capture(pager), EasyMock.eq(modifiedQuery))).andReturn(searchResults);

        AbstractIssuePickerSearchProvider provider = new SimpleProvider(mockSearchProvider, null, modifier)
        {
            @Override
            Query createQuery(final String query, final Collection<String> keyTerms, final Collection<String> summaryTerms)
            {
                assertEquals(WEB_SEARCH, query);

                keyTerms.addAll(keyTermsExpected);
                summaryTerms.addAll(summaryTermsExpected);
                return createQuery;
            }

            @Override
            Query addFilterToQuery(final IssuePickerSearchService.IssuePickerParameters issuePickerParams, final Query filterQuery)
            {
                assertEquals(createQuery, filterQuery);
                return createQueryWithFilter;
            }
        };

        // we have to use this as the default one we should use is DB dependant
        provider.summaryAnalyzer =
                new EnglishAnalyzer
                        (
                                LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(),
                                TokenFilters.English.StopWordRemoval.defaultSet()
                        );

        mocksControl.replay();

        IssuePickerResults results = provider.getResults(jiraServiceContext, testParams, maxCount);

        assertNotNull(results);
        assertEquals(results.getLabel(), provider.getLabelKey());
        assertEquals(maxCount, pager.getValue().getMax());
        assertNotNull(results.getIssues());
        assertCollectionEquals(issueList, results.getIssues());
        assertCollectionEquals(keyTermsExpected, results.getKeyTerms());
        assertCollectionEquals(summaryTermsExpected, results.getSummaryTerms());
        
        mocksControl.verify();
    }

    private <T> void assertCollectionEquals(Collection<T> expected, Collection<? extends T> actual)
    {
        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
    }

    private Issue getMockIssue(final String key, final String summary)
    {
        return new MockIssue(999L)
        {
            public String getKey()
            {
                return key;
            }

            public String getSummary()
            {
                return summary;
            }
        };
    }

    private Issue getMockIssueWithParent(final String key, final String summary, final Issue parent)
    {
        return new MockIssue(999L)
        {

            public String getKey()
            {
                return key;
            }

            public String getSummary()
            {
                return summary;
            }

            public Issue getParentObject()
            {
                return parent;
            }

        };
    }

    private static class SimpleProvider extends AbstractIssuePickerSearchProvider
    {
        private SimpleProvider(final SearchProvider searchProvider, final ConstantsManager constantsManager, final LuceneQueryModifier modifier)
        {
            super(searchProvider, constantsManager, modifier);
        }

        private SimpleProvider()
        {
            super(null, null, null);
        }

        @Override
        protected String getLabelKey()
        {
            return "label.ley";
        }

        @Override
        protected String getId()
        {
            return "label.id";
        }

        @Override
        protected SearchRequest getRequest(final IssuePickerSearchService.IssuePickerParameters issuePickerParams)
        {
            return new SearchRequest(new QueryImpl());
        }

        public boolean handlesParameters(final User searcher, final IssuePickerSearchService.IssuePickerParameters issuePickerParams)
        {
            return false;
        }
    }
}
