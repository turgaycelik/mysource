package com.atlassian.jira.issue.comments;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.issue.statistics.util.FieldHitCollector;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactory;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactoryImpl;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.mock.MockSearchProvider;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.matcher.ArgumentMatcher;
import com.atlassian.jira.mock.matcher.ArgumentMatchers;
import com.atlassian.jira.mock.matcher.ArgumentsMatcherBuilder;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.easymock.ArgumentsMatcher;
import org.easymock.EasyMock;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the {@link com.atlassian.jira.issue.comments.DefaultRecentCommentManager}.
 *
 * @since v4.0
 */
public class TestDefaultRecentCommentManager
{
    private JqlDateSupport dateSupport;

    @Before
    public void setUp() throws Exception
    {
        final TimeZoneManager timeZoneManager = new TimeZoneManager()
        {
            @Override
            public TimeZone getLoggedInUserTimeZone()
            {
                return TimeZone.getDefault();
            }

            @Override
            public TimeZone getTimeZoneforUser(User user)
            {
                return TimeZone.getDefault();
            }

            @Override
            public TimeZone getDefaultTimezone()
            {
                return TimeZone.getDefault();
            }
        };
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        componentWorker.registerMock(JqlClauseBuilderFactory.class, new JqlClauseBuilderFactoryImpl(new JqlDateSupportImpl(timeZoneManager)));
        ComponentAccessor.initialiseWorker(componentWorker);
        dateSupport = new JqlDateSupportImpl(new ConstantClock(createDate(2007, 1, 12)), timeZoneManager);
    }


    @After
    public void tearDown() throws Exception
    {

    }

    /**
     * Make sure an empty query is created when there are no issues found.
     *
     * @throws Exception indicates some unexpected failure.
     */
    @Test
    public void testGetRecentCommentsNoIssues() throws Exception
    {
        final SearchRequest request = new SearchRequest();
        _testGetRecentComments(Collections.<Long> emptyList(), request, null, false, createIssueQuery(Collections.<Long> emptyList()));
    }

    /**
     * Make sure a pure issue key search is given when issues are found. Also, make sure there are no "updated date"
     * critera added to the query.
     *
     * @throws Exception indicates some unexpected failure.
     */
    @Test
    public void testGetRecentCommentsIssues() throws Exception
    {

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().updatedBetween(createDate(1996, Calendar.SEPTEMBER, 11), null);
        final SearchRequest request = new SearchRequest(builder.buildQuery());

        final List<Long> issueIds = CollectionBuilder.newBuilder(1L, 2L, 4L, 5L).asList();
        _testGetRecentComments(issueIds, request, null, false, createIssueQuery(issueIds));
    }

    /**
     * Make the correct search is performed when the search request does not contain any "updated date" critera and we
     * have determined that they can be included in the comment search.
     *
     * @throws Exception indicates some unexpected failure.
     */
    @Test
    public void testGetRecentCommentsIssuesNoDates() throws Exception
    {
        final SearchRequest request = new SearchRequest();
        final List<Long> issueIds = CollectionBuilder.newBuilder(5674L).asList();
        _testGetRecentComments(issueIds, request, null, true, createIssueQuery(issueIds));
    }

    /**
     * Make sure the correct search is performed when theh search request does contain "update date" critera and we have
     * determined that they be included in the comment search.
     *
     * @throws Exception indicates some unexpected failure.
     */
    @Test
    public void testGetRecentCommentsIssuesUpdatedFromAbsoluteDate() throws Exception
    {
        final Date afterDate = createDate(2006, Calendar.JANUARY, 26);

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().updatedAfter(afterDate);
        final SearchRequest request = new SearchRequest(builder.buildQuery());

        final List<Long> issueIds = CollectionBuilder.newBuilder(5674L).asList();
        final BooleanQuery query = createIssueQuery(issueIds);

        final org.apache.lucene.search.Query dateQuery = createAfterQuery(afterDate);
        query.add(dateQuery, BooleanClause.Occur.MUST);

        _testGetRecentComments(issueIds, request, null, true, query);
    }

    /**
     * Make sure the correct search is performed when theh search request does contain "update date" critera and we have
     * determined that they be included in the comment search.
     *
     * @throws Exception indicates some unexpected failure.
     */
    @Test
    public void testGetRecentCommentsIssuesUpdatedFromRelativeDate() throws Exception
    {
        final String afterDuration = "-5h";
        final Date afterDate = dateSupport.convertToDate(afterDuration);

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().updatedAfter(afterDuration);
        final SearchRequest request = new SearchRequest(builder.buildQuery());

        final List<Long> issueIds = CollectionBuilder.newBuilder(5674L).asList();
        final BooleanQuery query = createIssueQuery(issueIds);

        final TermRangeQuery dateQuery = createAfterQuery(afterDate);
        query.add(dateQuery, BooleanClause.Occur.MUST);

        _testGetRecentComments(issueIds, request, null, true, query);
    }

    /**
     * Make sure the correct search is performed when theh search request does contain "update date" critera and we have
     * determined that they be included in the comment search.
     *
     * @throws Exception indicates some unexpected failure.
     */
    @Test
    public void testGetRecentCommentsIssuesUpdatedToAbsoluteDate() throws Exception
    {
        final Date beforeDate = createDate(1994, Calendar.JANUARY, 26);

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().updated().ltEq(beforeDate);
        final SearchRequest request = new SearchRequest(builder.buildQuery());

        final List<Long> issueIds = CollectionBuilder.newBuilder(5674L).asList();
        final BooleanQuery query = createIssueQuery(issueIds);

        final TermRangeQuery dateQuery = createBeforeQuery(beforeDate);
        query.add(dateQuery, BooleanClause.Occur.MUST);

        _testGetRecentComments(issueIds, request, null, true, query);
    }

    /**
     * Make sure the correct search is performed when theh search request does contain "update date" critera and we have
     * determined that they be included in the comment search.
     *
     * @throws Exception indicates some unexpected failure.
     */
    @Test
    public void testGetRecentCommentsIssuesUpdatedToRelativeDate() throws Exception
    {
        final String beforeDuration = "-3d";
        final Date beforeDate = dateSupport.convertToDate(beforeDuration);

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().updated().ltEq(beforeDuration);
        final SearchRequest request = new SearchRequest(builder.buildQuery());

        final List<Long> issueIds = CollectionBuilder.newBuilder(5674L).asList();
        final BooleanQuery query = createIssueQuery(issueIds);

        final TermRangeQuery dateQuery = createBeforeQuery(beforeDate);
        query.add(dateQuery, BooleanClause.Occur.MUST);

        _testGetRecentComments(issueIds, request, null, true, query);
    }

    /**
     * Make sure the correct search is performed when theh search request does contain "update date" critera and we have
     * determined that they be included in the comment search.
     *
     * @throws Exception indicates some unexpected failure.
     */
    @Test
    public void testGetRecentCommentsIssuesUpdatedFromToDate() throws Exception
    {
        final String beforeDuration = "-3d";
        final Date beforeDate = dateSupport.convertToDate(beforeDuration);
        final Date afterDate = createDate(1981, Calendar.JANUARY, 12);
        final ApplicationUser user = new MockApplicationUser("me");

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().updated().ltEq(beforeDuration).and().updatedAfter(afterDate).and().priority("major");
        final SearchRequest request = new SearchRequest(builder.buildQuery());

        final List<Long> issueIds = CollectionBuilder.newBuilder(6L, 3L, 13484L, 11111134L).asList();
        final BooleanQuery query = createIssueQuery(issueIds);

        final BooleanQuery dateQuery = new BooleanQuery();
        dateQuery.add(createBeforeQuery(beforeDate), BooleanClause.Occur.MUST);
        dateQuery.add(createAfterQuery(afterDate), BooleanClause.Occur.MUST);

        query.add(dateQuery, BooleanClause.Occur.MUST);

        _testGetRecentComments(issueIds, request, user, true, query);
    }

    /**
     * Make sure the correct search is performed when theh search request does contain "update date", but that criteria
     * cannot be converted into a filter, that is, when all the "updated date" clauses are not anded together.
     *
     * @throws Exception indicates some unexpected failure.
     */
    @Test
    public void testGetRecentCommentsIssuesBad() throws Exception
    {
        final String beforeDuration = "-3d";
        final Date afterDate = createDate(1981, Calendar.JANUARY, 12);
        final MockApplicationUser user = new MockApplicationUser("me");

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().updatedAfter(afterDate).and().sub().priority("minor").or().updated().ltEq(beforeDuration).endsub();
        final SearchRequest request = new SearchRequest(builder.buildQuery());

        final List<Long> issueIds = CollectionBuilder.newBuilder(6L, 3L, 13484L, 11111134L).asList();
        final BooleanQuery query = createIssueQuery(issueIds);

        _testGetRecentComments(issueIds, request, user, true, query);
    }

    /**
     * Test that the manager executes the correct search to find the recent comments.
     *
     * @param issueIds the issue ids to limit the comment search by.
     * @param request the search request being used to find the comments. This is the request that is normally used to
     * find all the issue ids to form the scope of the comment search. The request may also be used to add extra "update
     * date" criteria to the comment search.
     * @param user the user performing the search.
     * @param includeDates true if and only if the passed search request should be used to create extra "update date"
     * criteria.
     * @param expectedQuery the query that we expect to be generated by the manager.
     * @throws Exception this is a test, just rethrow for a failure.
     */
    private void _testGetRecentComments(final List<Long> issueIds, final SearchRequest request, final ApplicationUser user, final boolean includeDates, final org.apache.lucene.search.Query expectedQuery) throws Exception
    {
        //the issue searcher is never called, but we need it there.
        final MockControl mockIssueSearcherControl = MockClassControl.createControl(IndexSearcher.class);
        final IndexSearcher mockIssueSearcher = (IndexSearcher) mockIssueSearcherControl.getMock();
        mockIssueSearcherControl.replay();

        //this is where we check to make sure search lucene with the correct comment.
        final MockControl mockCommentSearcherControl = MockClassControl.createControl(IndexSearcher.class);
        final IndexSearcher mockCommentSearcher = (IndexSearcher) mockCommentSearcherControl.getMock();

        final Sort sort = new Sort(new SortField[] { new SortField(DocumentConstants.COMMENT_UPDATED, SortField.STRING, true) });
        final ArgumentsMatcher compositeArgumentsMatcher = ArgumentsMatcherBuilder.newNaturalBuilder().addDefaultMatcher().addDefaultMatcher().addArgumentMatcher(
            Sort.class, new LuceneSortMatcher()).asArgumentsMatcher();

        mockCommentSearcher.search(expectedQuery, Integer.MAX_VALUE, sort);
        mockCommentSearcherControl.setMatcher(compositeArgumentsMatcher);
        mockCommentSearcherControl.setReturnValue(null);
        mockCommentSearcherControl.replay();

        final MockSearchProviderFactory factory = new MockSearchProviderFactory();
        factory.addRegistration(SearchProviderFactory.ISSUE_INDEX, mockIssueSearcher);
        factory.addRegistration(SearchProviderFactory.COMMENT_INDEX, mockCommentSearcher);

        //create a IssueSearchProvider that will return our constant list of ids.
        MockIssueSearchProvider mockIssueSearchProvider = new MockIssueSearchProvider(issueIds, user == null ? null : user.getDirectoryUser(), request.getQuery());
        final SearchService service = EasyMock.createMock(SearchService.class);
        EasyMock.expect(service.doesQueryFitFilterForm(user == null ? null : user.getDirectoryUser(), request.getQuery())).andReturn(includeDates);
        EasyMock.replay(service);

        MyLuceneQueryModifier myLuceneQueryModifier = new MyLuceneQueryModifier();

        CommentService commentService = EasyMock.createMock(CommentService.class);
        JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        final DefaultRecentCommentManager defaultCommentManager = new DefaultRecentCommentManager(commentService, factory, mockIssueSearchProvider, dateSupport, jqlOperandResolver, service, myLuceneQueryModifier, null, null);
        defaultCommentManager.getRecentComments(request, user);

        mockIssueSearcherControl.verify();
        mockCommentSearcherControl.verify();
    }

    private static Date createDate(final int year, final int month, final int day)
    {
        final Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        instance.setLenient(true);
        instance.set(year, month, day, 0, 0, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTime();
    }

    private TermRangeQuery createAfterQuery(final Date afterDate)
    {
        return new TermRangeQuery(DocumentConstants.COMMENT_UPDATED, dateSupport.getIndexedValue(afterDate), null, true, true);
    }

    private TermRangeQuery createBeforeQuery(final Date beforeDate)
    {
        return new TermRangeQuery(DocumentConstants.COMMENT_UPDATED, null, dateSupport.getIndexedValue(beforeDate), true, true);
    }

    private static BooleanQuery createIssueQuery(final Collection<Long> ids)
    {
        final BooleanQuery idQuery = new BooleanQuery();
        for (final Long id : ids)
        {
            idQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_ID, id.toString())), BooleanClause.Occur.SHOULD);
        }

        final BooleanQuery query = new BooleanQuery();
        query.add(idQuery, BooleanClause.Occur.MUST);

        return query;
    }

    /**
     * Simple map implementation of the SearchProviderFactory.
     */
    private static class MockSearchProviderFactory implements SearchProviderFactory
    {
        private final Map<String, IndexSearcher> searchers = new HashMap<String, IndexSearcher>();

        public MockSearchProviderFactory addRegistration(final String name, final IndexSearcher search)
        {
            searchers.put(name, search);
            return this;
        }

        public IndexSearcher getSearcher(final String searcherName)
        {
            return searchers.get(searcherName);
        }
    }

    /**
     * A search provider that returns a constant list of issues.
     */
    private static class MockIssueSearchProvider extends MockSearchProvider
    {
        private final List<Long> issueIds;
        private final User expectedUser;
        private final Query expectedQuery;

        public MockIssueSearchProvider(final Collection<Long> issueIds, final User expectedUser, final Query expectedQuery)
        {
            this.expectedQuery = expectedQuery;
            this.issueIds = new LinkedList<Long>(issueIds);
            this.expectedUser = expectedUser;
        }

        public SearchResults search(final Query query, final User searcher, final PagerFilter pager) throws SearchException
        {
            throw new UnsupportedOperationException();
        }

        public SearchResults search(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery) throws SearchException
        {
            throw new UnsupportedOperationException();
        }

        public SearchResults searchOverrideSecurity(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery) throws SearchException
        {
            return null;
        }

        public long searchCount(final Query query, final User searcher) throws SearchException
        {
            throw new UnsupportedOperationException();
        }

        public long searchCountOverrideSecurity(final Query query, final User searcher) throws SearchException
        {
            return 0;
        }

        public void search(Query query, ApplicationUser searcher, Collector collector) throws SearchException
        {
            search(query, searcher == null ? null : searcher.getDirectoryUser(), collector);
        }

        public void search(final Query query, final User user, final Collector collector) throws SearchException
        {
            if (((expectedUser == null) && (user != null)) || ((expectedUser != null) && !expectedUser.equals(user)))
            {
                throw new SearchException("Unexpected user.");
            }

            if (!expectedQuery.equals(query))
            {
                throw new SearchException("Unexpected search request.");
            }

            final FieldHitCollector fieldHitCollector = (FieldHitCollector) collector;

            for (final Long issueId : issueIds)
            {
                final Document document = new Document();
                document.add(new Field(DocumentConstants.ISSUE_ID, String.valueOf(issueId), Field.Store.YES, Field.Index.NOT_ANALYZED));
                fieldHitCollector.collect(document);
            }

        }

        public void search(final Query query, final User searcher, final Collector collector, final org.apache.lucene.search.Query andQuery)
                throws SearchException
        {
            throw new UnsupportedOperationException();
        }

        public void searchOverrideSecurity(final Query query, final User user, final Collector collector) throws SearchException
        {}

        public void searchAndSort(final Query query, final User user, final Collector collector, final PagerFilter pagerFilter) throws SearchException
        {
            throw new UnsupportedOperationException();
        }

        public void searchAndSortOverrideSecurity(final Query query, final User user, final Collector collector, final PagerFilter pagerFilter) throws SearchException
        {}
    }

    /**
     * A simple {@link com.atlassian.jira.mock.matcher.ArgumentMatcher} for Lucene {@link org.apache.lucene.search.Sort}
     * objects.
     */
    private static class LuceneSortMatcher implements ArgumentMatcher<Sort>
    {
        public boolean match(final Sort expected, final Sort acutal)
        {
            if (expected == acutal)
            {
                return true;
            }
            else if ((expected == null) || (acutal == null))
            {
                return false;
            }
            else
            {
                final SortField[] expectedFields = expected.getSort();
                final SortField[] actualFields = acutal.getSort();
                if (expectedFields.length != actualFields.length)
                {
                    return false;
                }

                final ArgumentMatcher<String> matcher = ArgumentMatchers.naturalMatcher();

                for (int i = 0; i < actualFields.length; i++)
                {
                    final SortField actualField = actualFields[i];
                    final SortField expectedField = expectedFields[i];
                    if (!matcher.match(expectedField.getField(), actualField.getField()))
                    {
                        return false;
                    }
                    if (actualField.getType() != expectedField.getType())
                    {
                        return false;
                    }

                    if (expectedField.getReverse() != actualField.getReverse())
                    {
                        return false;
                    }
                }
                return true;
            }
        }

        public String toString(final Sort object)
        {
            return String.valueOf(object);
        }
    }

    private static class MyLuceneQueryModifier implements LuceneQueryModifier
    {
        public org.apache.lucene.search.Query getModifiedQuery(final org.apache.lucene.search.Query originalQuery)
        {
            return originalQuery;
        }
    }
}
