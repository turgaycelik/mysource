package com.atlassian.jira.issue.search.handlers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DueDateIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.MockSystemSearcher;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.DefaultClauseHandler;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.permission.ClausePermissionChecker;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.permission.ClauseSanitiser;
import com.atlassian.jira.jql.permission.DefaultClausePermissionHandler;
import com.atlassian.jira.jql.permission.MockFieldClausePermissionFactory;
import com.atlassian.jira.jql.permission.NoOpClauseSanitiser;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.mock.issue.fields.MockSearchableField;
import com.atlassian.jira.mock.issue.search.searchers.information.MockSearcherInformation;
import com.atlassian.jira.mock.jql.MockClauseInformation;
import com.atlassian.jira.mock.jql.context.MockClauseContextFactory;
import com.atlassian.jira.mock.jql.query.MockClauseQueryFactory;
import com.atlassian.jira.mock.jql.validator.MockClausePermissionChecker;
import com.atlassian.jira.mock.jql.validator.MockClauseValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link SimpleSearchHandlerFactory}. 
 *
 * @since v4.0
 */
public class TestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateClauseHandler() throws Exception
    {
        final List<FieldIndexer> expectedIndexers = Collections.<FieldIndexer>singletonList(new DueDateIndexer(null));

        final MockSearcherInformation<SearchableField> information = new MockSearcherInformation<SearchableField>("searcherid");
        information.setIndexers(expectedIndexers);

        final MockSystemSearcher searcher = new MockSystemSearcher("searcherid");
        searcher.setInformation(information);
        final MockClausePermissionChecker checker = new MockClausePermissionChecker();

        final Set<String> expectedNames = CollectionBuilder.newBuilder("qwerty", "keyboards", "suck").asSet();
        final ClauseNames expectedClauseNames = new ClauseNames("qwerty", expectedNames);
        final MockClauseQueryFactory expectedFactory = new MockClauseQueryFactory();
        final MockClauseValidator expectedValidator = new MockClauseValidator();
        final MockClauseContextFactory clauseContextFactory = new MockClauseContextFactory();

        //Test with sanitiser.
        final MockClauseInformation clauseInformation = new MockClauseInformation(expectedClauseNames);
        MockSimpleSearchHandlerFactory factory = new MockSimpleSearchHandlerFactory(new MockComponentFactory(searcher), clauseInformation, MockSystemSearcher.class, expectedFactory, expectedValidator, checker, clauseContextFactory, NoOpClauseSanitiser.NOOP_CLAUSE_SANITISER);

        final MockSearchableField field = new MockSearchableField("myfield");
        SearchHandler actualhandler = factory.createHandler(field);

        ClausePermissionHandler mockHandler = new DefaultClausePermissionHandler(checker, NoOpClauseSanitiser.NOOP_CLAUSE_SANITISER);
        SearchHandler.SearcherRegistration expectedRego = new SearchHandler.SearcherRegistration(searcher, new SearchHandler.ClauseRegistration(new DefaultClauseHandler(clauseInformation, expectedFactory, expectedValidator, mockHandler, clauseContextFactory)));
        SearchHandler expectedHandler = new SearchHandler(expectedIndexers, expectedRego);
        assertEquals(expectedHandler, actualhandler);

        //Test without sanitiser.
        factory = new MockSimpleSearchHandlerFactory(new MockComponentFactory(searcher), clauseInformation, MockSystemSearcher.class, expectedFactory, expectedValidator, checker, clauseContextFactory, null);
        actualhandler = factory.createHandler(field);
        mockHandler = new DefaultClausePermissionHandler(checker);
        expectedRego = new SearchHandler.SearcherRegistration(searcher, new SearchHandler.ClauseRegistration(new DefaultClauseHandler(clauseInformation, expectedFactory, expectedValidator, mockHandler, clauseContextFactory)));
        expectedHandler = new SearchHandler(expectedIndexers, expectedRego);
        assertEquals(expectedHandler, actualhandler);
    }

    private static class MockSimpleSearchHandlerFactory extends SimpleSearchHandlerFactory
    {
        public MockSimpleSearchHandlerFactory(final ComponentFactory factory, final ClauseInformation information,
                final Class<? extends IssueSearcher<SearchableField>> searcherClass, final ClauseQueryFactory queryFactory,
                final ClauseValidator queryValidator, final ClausePermissionChecker permissionChecker, final ClauseContextFactory contextFactory,
                final ClauseSanitiser sanitister)
        {
            super(factory, information, searcherClass, queryFactory, queryValidator, new MockFieldClausePermissionFactory(permissionChecker), contextFactory, null, sanitister);
        }
    }

    private static class MockComponentFactory implements ComponentFactory
    {
        private final IssueSearcher<SearchableField> searcher;

        public MockComponentFactory(final IssueSearcher<SearchableField> searcher)
        {
            this.searcher = searcher;
        }

        public <T> T createObject(final Class<T> type, final Object... arguments)
        {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings ({ "unchecked" })
        public <T> T createObject(final Class<T> type)
        {
            if (type != searcher.getClass())
            {
                throw new IllegalArgumentException("Instances of " + searcher.getClass());
            }
            return (T)searcher;
        }
    }
}
