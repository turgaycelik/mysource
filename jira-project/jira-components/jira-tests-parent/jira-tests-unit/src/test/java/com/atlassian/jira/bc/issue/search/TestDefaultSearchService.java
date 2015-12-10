package com.atlassian.jira.bc.issue.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.RelevantSearcherVisitor;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.context.AllIssueTypesContext;
import com.atlassian.jira.jql.context.AllProjectsContext;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.ProjectIssueTypeContext;
import com.atlassian.jira.jql.context.ProjectIssueTypeContextImpl;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.context.QueryContextImpl;
import com.atlassian.jira.jql.context.QueryContextVisitor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.parser.JqlParseErrorMessage;
import com.atlassian.jira.jql.parser.JqlParseErrorMessages;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.jql.permission.ClauseSanitisingVisitor;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.jql.validator.OrderByValidator;
import com.atlassian.jira.jql.validator.ValidatorVisitor;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.search.MockSearchContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.MockUrlBuilderFactory;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestDefaultSearchService extends MockControllerTestCase
{
    private QueryContextConverter queryContextConverter;
    private SearchHandlerManager searchHandlerManager;
    private JqlQueryParser jqlQueryParser;
    private ValidatorVisitor.ValidatorVisitorFactory validatorVisitorFactory;
    private JqlStringSupport jqlStringSupport;
    private QueryContextVisitor.QueryContextVisitorFactory contextVisitorFactory;
    private QueryCache queryCache;
    private JqlOperandResolver jqlOperandResolver;
    private OrderByValidator orderByValidator;
    private SearchProvider searchProvider;
    private I18nHelper.BeanFactory factory;

    @Before
    public void setUp() throws Exception
    {
        queryContextConverter = mockController.getMock(QueryContextConverter.class);
        searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        jqlQueryParser = mockController.getMock(JqlQueryParser.class);
        validatorVisitorFactory = mockController.getMock(ValidatorVisitor.ValidatorVisitorFactory.class);
        jqlStringSupport = mockController.getMock(JqlStringSupport.class);
        contextVisitorFactory = mockController.getMock(QueryContextVisitor.QueryContextVisitorFactory.class);
        queryCache = mockController.getMock(QueryCache.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        orderByValidator = mockController.getMock(OrderByValidator.class);
        searchProvider = mockController.getMock(SearchProvider.class);

        factory = new NoopI18nFactory();

    }

    @Test
    public void testParseResultBadConstructorArgs() throws Exception
    {
        mockController.replay();
        try
        {
            new SearchService.ParseResult(null, null);
            fail("Expected exception for MessageSet arg");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testGetQueryStringFromSearchQuery() throws Exception
    {
        final String queryAsString = "clause = fake";
        final Query query = mockController.getMock(Query.class);
        final AtomicBoolean called = new AtomicBoolean(false);

        replay();

        final DefaultSearchService service = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            public String getJqlString(final Query inputQuery)
            {
                called.set(true);
                assertSame(inputQuery, query);
                return queryAsString;
            }

            @Override
            UrlBuilder createUrlBuilder()
            {
                return MockUrlBuilderFactory.createUrlBuilder(true);
            }
        };

        final String result = service.getQueryString(null, query);
        assertEquals("&jqlQuery=clause+%3D+fake", result);
        assertTrue(called.get());

        verify();
    }

    @Test
    public void testGetSearchContextQueryNull() throws Exception
    {
        final AtomicBoolean createCalled = new AtomicBoolean(false);
        SearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
            {
                createCalled.set(true);
                assertTrue(projects.isEmpty());
                assertTrue(issueTypes.isEmpty());
                return null;
            }
        };

        mockController.replay();

        searchService.getSearchContext(null, null);
        assertTrue(createCalled.get());
        mockController.verify();
    }

    @Test
    public void testGetSearchContextQueryContextNull() throws Exception
    {
        final AtomicBoolean createCalled = new AtomicBoolean(false);
        SearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            public QueryContext getSimpleQueryContext(final User searcher, final Query searchQuery)
            {
                return null;
            }

            @Override
            SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
            {
                createCalled.set(true);
                assertTrue(projects.isEmpty());
                assertTrue(issueTypes.isEmpty());
                return null;
            }
        };

        final Query query = mockController.getMock(Query.class);

        mockController.replay();

        searchService.getSearchContext(null, query);
        assertTrue(createCalled.get());
        mockController.verify();
    }

    @Test
    public void testGetSearchContextNullSearchContext() throws Exception
    {
        final QueryContext queryContext = mockController.getMock(QueryContext.class);
        queryContextConverter.getSearchContext(queryContext);
        mockController.setReturnValue(null);

        final AtomicBoolean createCalled = new AtomicBoolean(false);
        SearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            public QueryContext getSimpleQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }

            @Override
            SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
            {
                createCalled.set(true);
                assertTrue(projects.isEmpty());
                assertTrue(issueTypes.isEmpty());
                return null;
            }
        };

        final Query query = mockController.getMock(Query.class);

        mockController.replay();

        searchService.getSearchContext(null, query);
        assertTrue(createCalled.get());
        mockController.verify();
    }

    @Test
    public void testGetSearchContextHappyPath() throws Exception
    {
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        final QueryContext queryContext = mockController.getMock(QueryContext.class);
        queryContextConverter.getSearchContext(queryContext);
        mockController.setReturnValue(searchContext);

        final AtomicBoolean createCalled = new AtomicBoolean(false);
        SearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            public QueryContext getSimpleQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }

            @Override
            SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
            {
                createCalled.set(true);
                assertTrue(projects.isEmpty());
                assertTrue(issueTypes.isEmpty());
                return null;
            }
        };

        final Query query = mockController.getMock(Query.class);

        mockController.replay();

        searchService.getSearchContext(null, query);
        assertFalse(createCalled.get());
        mockController.verify();
    }

    @Test
    public void testGetQueryContext() throws Exception
    {
        final Clause clause = mockController.getMock(Clause.class);
        final QueryContextVisitor queryContextVisitor = new QueryContextVisitor(null, null, null);

        SearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);

        final QueryImpl searchQuery = new QueryImpl(clause);
        final ClauseContextImpl fullContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        final ClauseContextImpl simpleContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        QueryContextVisitor.ContextResult contextResult = new QueryContextVisitor.ContextResult(fullContext, simpleContext);

        queryCache.getQueryContextCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setQueryContextCache(null, searchQuery, new QueryContextImpl(fullContext));

        queryCache.setSimpleQueryContextCache(null, searchQuery, new QueryContextImpl(simpleContext));

        contextVisitorFactory.createVisitor(null);
        mockController.setReturnValue(queryContextVisitor);

        clause.accept(queryContextVisitor);
        mockController.setReturnValue(contextResult);

        mockController.replay();

        searchService.getQueryContext(null, searchQuery);

        mockController.verify();
    }

    @Test
    public void testGetSimpleQueryContext() throws Exception
    {
        final Clause clause = mockController.getMock(Clause.class);
        final QueryContextVisitor queryContextVisitor = new QueryContextVisitor(null, null, null);

        SearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);

        final QueryImpl searchQuery = new QueryImpl(clause);
        final ClauseContextImpl fullContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        final ClauseContextImpl simpleContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        QueryContextVisitor.ContextResult contextResult = new QueryContextVisitor.ContextResult(fullContext, simpleContext);

        queryCache.getSimpleQueryContextCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setQueryContextCache(null, searchQuery, new QueryContextImpl(fullContext));

        queryCache.setSimpleQueryContextCache(null, searchQuery, new QueryContextImpl(simpleContext));

        contextVisitorFactory.createVisitor(null);
        mockController.setReturnValue(queryContextVisitor);

        clause.accept(queryContextVisitor);
        mockController.setReturnValue(contextResult);

        mockController.replay();

        searchService.getSimpleQueryContext(null, searchQuery);

        mockController.verify();
    }

    @Test
    public void testGetQueryContextForAllQuery() throws Exception
    {
        SearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);

        final QueryImpl searchQuery = new QueryImpl();
        final ClauseContextImpl expectedClause = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, AllIssueTypesContext.INSTANCE)));

        QueryContext expectedContext = new QueryContextImpl(expectedClause);

        mockController.replay();

        final QueryContext queryContext = searchService.getQueryContext(null, searchQuery);

        assertEquals(expectedContext, queryContext);

        mockController.verify();
    }

    @Test
    public void testGetQueryContextCache() throws Exception
    {
        final Clause clause = mockController.getMock(Clause.class);
        final QueryContextVisitor queryContextVisitor = new QueryContextVisitor(null, null, null);

        final QueryImpl searchQuery = new QueryImpl(clause);
        final ClauseContextImpl fullContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        final ClauseContextImpl simpleContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        QueryContextVisitor.ContextResult contextResult = new QueryContextVisitor.ContextResult(fullContext, simpleContext);

        SearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);
        contextVisitorFactory.createVisitor(null);
        mockController.setReturnValue(queryContextVisitor, 1);
        clause.accept(queryContextVisitor);
        mockController.setReturnValue(contextResult, 1);

        queryCache.getQueryContextCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setQueryContextCache(null, searchQuery,  new QueryContextImpl(fullContext));
        queryCache.setSimpleQueryContextCache(null, searchQuery, new QueryContextImpl(simpleContext));

        queryCache.getQueryContextCache(null, searchQuery);
        mockController.setReturnValue( new QueryContextImpl(fullContext));

        mockController.replay();

        searchService.getQueryContext(null, searchQuery);
        searchService.getQueryContext(null, searchQuery);

        mockController.verify();
    }

    @Test
    public void testGetSimpleQueryContextCache() throws Exception
    {
        final Clause clause = mockController.getMock(Clause.class);
        final QueryContextVisitor queryContextVisitor = new QueryContextVisitor(null, null, null);

        final QueryImpl searchQuery = new QueryImpl(clause);
        final ClauseContextImpl fullContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        final ClauseContextImpl simpleContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        QueryContextVisitor.ContextResult contextResult = new QueryContextVisitor.ContextResult(fullContext, simpleContext);

        SearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);
        contextVisitorFactory.createVisitor(null);
        mockController.setReturnValue(queryContextVisitor, 1);
        clause.accept(queryContextVisitor);
        mockController.setReturnValue(contextResult, 1);

        queryCache.getSimpleQueryContextCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setQueryContextCache(null, searchQuery,  new QueryContextImpl(fullContext));
        queryCache.setSimpleQueryContextCache(null, searchQuery, new QueryContextImpl(simpleContext));

        queryCache.getSimpleQueryContextCache(null, searchQuery);
        mockController.setReturnValue( new QueryContextImpl(simpleContext));

        mockController.replay();

        searchService.getSimpleQueryContext(null, searchQuery);
        searchService.getSimpleQueryContext(null, searchQuery);

        mockController.verify();
    }

    @Test
    public void testParseResult() throws Exception
    {
        mockController.replay();
        MessageSetImpl set = new MessageSetImpl();
        set.addErrorMessage("Blah");

        SearchService.ParseResult parseResult = new SearchService.ParseResult(null, set);
        assertFalse(parseResult.isValid());

        set = new MessageSetImpl();
        parseResult = new SearchService.ParseResult(null, set);
        assertTrue(parseResult.isValid());
    }

    @Test
    public void testParseQueryBadArgs() throws Exception
    {
        DefaultSearchService searchService = mockController.instantiate(DefaultSearchService.class);

        try
        {
            searchService.parseQuery(null, null);
            fail("Expected exception for null string input");
        }
        catch (IllegalArgumentException expected)
        {
        }

        mockController.verify();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void testParseQueryThrowsParseException() throws Exception
    {
        final String input = "input";
        final JqlParseErrorMessage errorMessage = JqlParseErrorMessages.illegalNumber("4884k", 1, 100);

        jqlQueryParser.parseQuery(input);
        mockController.setThrowable(new JqlParseException(errorMessage));
        mockController.replay();

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);

        final SearchService.ParseResult parseResult = searchService.parseQuery(null, input);
        assertFalse(parseResult.isValid());
        assertNull(parseResult.getQuery());

        assertEquals(1, parseResult.getErrors().getErrorMessages().size());
        assertTrue(parseResult.getErrors().getWarningMessages().isEmpty());

        final String onlyMessage = parseResult.getErrors().getErrorMessages().iterator().next();
        assertEquals("jql.parse.illegal.number{[1, 101, 4884k, -9223372036854775808, 9223372036854775807]}", onlyMessage);

        mockController.verify();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void testParseQueryNullErrorMessage() throws Exception
    {
        final String input = "input";
        final JqlParseErrorMessage errorMessage = JqlParseErrorMessages.genericParseError();

        jqlQueryParser.parseQuery(input);
        mockController.setThrowable(new JqlParseException(errorMessage));
        mockController.replay();

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);

        final SearchService.ParseResult parseResult = searchService.parseQuery(null, input);
        assertFalse(parseResult.isValid());
        assertNull(parseResult.getQuery());

        assertEquals(1, parseResult.getErrors().getErrorMessages().size());
        assertTrue(parseResult.getErrors().getWarningMessages().isEmpty());

        final String onlyMessage = parseResult.getErrors().getErrorMessages().iterator().next();
        assertEquals("jql.parse.unknown.no.pos{[]}", onlyMessage);

        mockController.verify();
    }

    @Test
    public void testParseQueryHappyPath() throws Exception
    {
        final String input = "input";
        final Clause returnClause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        final QueryImpl returnQuery = new QueryImpl(returnClause, input);

        final JqlQueryParser jqlQueryParser = mockController.getMock(JqlQueryParser.class);
        jqlQueryParser.parseQuery(input);
        mockController.setReturnValue(returnQuery);

        DefaultSearchService searchService = mockController.instantiate(DefaultSearchService.class);

        final SearchService.ParseResult parseResult = searchService.parseQuery(null, input);
        assertTrue(parseResult.isValid());
        assertNotNull(parseResult.getQuery());
        assertEquals(returnQuery, parseResult.getQuery());

        mockController.verify();
    }

    @Test
    public void testDoesQueryFitFilterFormNullQuery() throws Exception
    {
        final QueryImpl searchQuery = null;

        DefaultSearchService searchService = mockController.instantiate(DefaultSearchService.class);

        try
        {
            searchService.doesQueryFitFilterForm(null, searchQuery);
            fail("Should not take a null query");
        }
        catch (Exception e)
        {
            // expected
        }
        mockController.verify();
    }

    @Test
    public void testDoesQueryFitFilterFormNullWhereClause() throws Exception
    {
        final QueryImpl searchQuery = new QueryImpl();

        DefaultSearchService searchService = mockController.instantiate(DefaultSearchService.class);

        assertTrue(searchService.doesQueryFitFilterForm(null, searchQuery));
        mockController.verify();
    }

    @Test
    public void testDoesQueryFitFilterFormClauseBadContext() throws Exception
    {
        final QueryContext queryContext = new QueryContext()
        {
            public Collection<ProjectIssueTypeContexts> getProjectIssueTypeContexts()
            {
                return null;
            }

            public boolean isExplicit()
            {
                return true;
            }
        };

        queryContextConverter.getSearchContext(queryContext);
        mockController.setReturnValue(null);

        final QueryImpl searchQuery = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value"));

        queryCache.getDoesQueryFitFilterFormCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setDoesQueryFitFilterFormCache(null, searchQuery, false);


        mockController.replay();

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            public QueryContext getQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }

            @Override
            public QueryContext getSimpleQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }
        };

        assertFalse(searchService.doesQueryFitFilterForm(null, searchQuery));
        mockController.verify();
    }

    @Test
    public void testDoesQueryFitFilterFormClauseWithNoSearcher() throws Exception
    {
        final QueryContext queryContext = new QueryContext()
        {
            public Collection<ProjectIssueTypeContexts> getProjectIssueTypeContexts()
            {
                return null;
            }

            public boolean isExplicit()
            {
                return true;
            }
        };

        queryContextConverter.getSearchContext(queryContext);
        mockController.setReturnValue(new MySearchContext());
        final QueryImpl searchQuery = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value"));

        queryCache.getDoesQueryFitFilterFormCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setDoesQueryFitFilterFormCache(null, searchQuery, false);

        mockController.replay();

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            Set<IssueSearcher> getRelevantQuerySearchers(final User user, final Query searchQuery)
            {
                return null;
            }

            @Override
            public QueryContext getQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }

            @Override
            public QueryContext getSimpleQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }
        };

        assertFalse(searchService.doesQueryFitFilterForm(null, searchQuery));
        mockController.verify();
    }

    @Test
    public void testDoesQueryFitFilterFormClauseWithSearcherValidationDoesntFit() throws Exception
    {
        SearchContext searchContext = new MySearchContext();

        final QueryContext queryContext = new QueryContext()
        {
            public Collection<ProjectIssueTypeContexts> getProjectIssueTypeContexts()
            {
                return null;
            }

            public boolean isExplicit()
            {
                return true;
            }
        };

        queryContextConverter.getSearchContext(queryContext);
        mockController.setReturnValue(searchContext);

        final QueryImpl searchQuery = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value"));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        final SearchInputTransformer searchInputTransformer = mockController.getMock(SearchInputTransformer.class);


        issueSearcher.getSearchInputTransformer();
        mockController.setReturnValue(searchInputTransformer);
        searchInputTransformer.doRelevantClausesFitFilterForm(null, searchQuery, searchContext);
        mockController.setReturnValue(true);

        queryCache.getDoesQueryFitFilterFormCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setDoesQueryFitFilterFormCache(null, searchQuery, false);

        mockController.replay();

        final Set<IssueSearcher> relevant = CollectionBuilder.newBuilder(issueSearcher).asSet();

        final AtomicBoolean navigationFitCalled = new AtomicBoolean(false);
        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            Set<IssueSearcher> getRelevantQuerySearchers(final User user, final Query searchQuery)
            {
                return relevant;
            }

            @Override
            public QueryContext getQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }

            @Override
            public QueryContext getSimpleQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }

            @Override
            boolean calculateDoesQueryValidationFitFilterForm(final User user, final SearchContext searchContext, final Query query)
            {
                navigationFitCalled.set(true);
                return false;
            }
        };

        assertFalse(searchService.doesQueryFitFilterForm(null, searchQuery));
        assertTrue(navigationFitCalled.get());
        mockController.verify();
    }

    @Test
    public void testDoesQueryFitFilterFormClauseWithSearcherAndFits() throws Exception
    {
        SearchContext searchContext = new MySearchContext();
        final QueryContext queryContext = new QueryContext()
        {
            public Collection<ProjectIssueTypeContexts> getProjectIssueTypeContexts()
            {
                return null;
            }

            public boolean isExplicit()
            {
                return true;
            }
        };

        queryContextConverter.getSearchContext(queryContext);
        mockController.setReturnValue(searchContext);

        final QueryImpl searchQuery = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value"));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        final SearchInputTransformer searchInputTransformer = mockController.getMock(SearchInputTransformer.class);


        issueSearcher.getSearchInputTransformer();
        mockController.setReturnValue(searchInputTransformer);
        searchInputTransformer.doRelevantClausesFitFilterForm(null, searchQuery, searchContext);
        mockController.setReturnValue(true);

        queryCache.getDoesQueryFitFilterFormCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setDoesQueryFitFilterFormCache(null, searchQuery, true);

        mockController.replay();

        final Set<IssueSearcher> relevant = CollectionBuilder.newBuilder(issueSearcher).asSet();

        final AtomicBoolean navigationFitCalled = new AtomicBoolean(false);
        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            Set<IssueSearcher> getRelevantQuerySearchers(final User user, final Query searchQuery)
            {
                return relevant;
            }

            @Override
            public QueryContext getQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }

            @Override
            boolean calculateDoesQueryValidationFitFilterForm(final User user, final SearchContext searchContext, final Query query)
            {
                navigationFitCalled.set(true);
                return true;
            }

            @Override
            public QueryContext getSimpleQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }
        };

        assertTrue(searchService.doesQueryFitFilterForm(null, searchQuery));
        assertTrue(navigationFitCalled.get());
        mockController.verify();
    }

    @Test
    public void testDoesQueryFitFilterFormClauseWithSearcherAndDoesntFit() throws Exception
    {
        SearchContext searchContext = new MySearchContext();
        final QueryContext queryContext = new QueryContext()
        {
            public Collection<ProjectIssueTypeContexts> getProjectIssueTypeContexts()
            {
                return null;
            }

            public boolean isExplicit()
            {
                return true;
            }
        };

        queryContextConverter.getSearchContext(queryContext);
        mockController.setReturnValue(searchContext);

        final QueryImpl searchQuery = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value"));

        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        final SearchInputTransformer searchInputTransformer = mockController.getMock(SearchInputTransformer.class);

        issueSearcher.getSearchInputTransformer();
        mockController.setReturnValue(searchInputTransformer);
        searchInputTransformer.doRelevantClausesFitFilterForm(null, searchQuery, searchContext);
        mockController.setReturnValue(false);

        queryCache.getDoesQueryFitFilterFormCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setDoesQueryFitFilterFormCache(null, searchQuery, false);

        mockController.replay();

        final Set<IssueSearcher> relevant = CollectionBuilder.newBuilder(issueSearcher).asSet();

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            Set<IssueSearcher> getRelevantQuerySearchers(final User user, final Query searchQuery)
            {
                return relevant;
            }

            @Override
            public QueryContext getQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }

            @Override
            public QueryContext getSimpleQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }

        };

        assertFalse(searchService.doesQueryFitFilterForm(null, searchQuery));
        mockController.verify();
    }

    @Test
    public void testDoesQueryFitFilterFormClauseWithNoRelevantSearchers() throws Exception
    {
        final QueryContext queryContext = new QueryContext()
        {
            public Collection<ProjectIssueTypeContexts> getProjectIssueTypeContexts()
            {
                return null;
            }

            public boolean isExplicit()
            {
                return true;
            }
        };

        queryContextConverter.getSearchContext(queryContext);
        mockController.setReturnValue(new MySearchContext());

        final QueryImpl searchQuery = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value"));

        queryCache.getDoesQueryFitFilterFormCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setDoesQueryFitFilterFormCache(null, searchQuery, true);

        mockController.replay();

        final Set<IssueSearcher> relevant = Collections.emptySet();

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            Set<IssueSearcher> getRelevantQuerySearchers(final User user, final Query searchQuery)
            {
                return relevant;
            }

            @Override
            public QueryContext getQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }

            @Override
            public QueryContext getSimpleQueryContext(final User searcher, final Query searchQuery)
            {
                return queryContext;
            }
        };

        assertTrue(searchService.doesQueryFitFilterForm(null, searchQuery));
        mockController.verify();
    }

    @Test
    public void testDoesSearchRequestFitNavigtorCache() throws Exception
    {
        final QueryImpl searchQuery = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value"));

        queryCache.getDoesQueryFitFilterFormCache(null, searchQuery);
        mockController.setReturnValue(null);

        queryCache.setDoesQueryFitFilterFormCache(null, searchQuery, true);

        queryCache.getDoesQueryFitFilterFormCache(null, searchQuery);
        mockController.setReturnValue(true);

        mockController.replay();

        final AtomicInteger timesCalculated = new AtomicInteger(0);

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            boolean calculateDoesQueryFitFilterForm(final User user, final Query query)
            {
                timesCalculated.getAndIncrement();
                return true;
            }
        };

        assertTrue(searchService.doesQueryFitFilterForm(null, searchQuery));
        assertTrue(searchService.doesQueryFitFilterForm(null, searchQuery));

        assertEquals(1, timesCalculated.get());

        mockController.verify();
    }

    @Test
    public void testGetRelevantQuerySearchersClauseWithNoSearcher() throws Exception
    {
        final AtomicBoolean visited = new AtomicBoolean(false);
        mockController.replay();

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            RelevantSearcherVisitor createRelevantSearcherVisitor(final User user)
            {
                return new RelevantSearcherVisitor(searchHandlerManager, null)
                {
                    @Override
                    public Boolean visit(final TerminalClause terminalClause)
                    {
                        visited.set(true);
                        return false;
                    }
                };
            }

        };

        assertNull(searchService.getRelevantQuerySearchers(null, new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value"))));
        assertTrue(visited.get());

        mockController.verify();
    }

    @Test
    public void testGetRelevantQuerySearchersClauseWithSearcher() throws Exception
    {
        final AtomicBoolean visited = new AtomicBoolean(false);
        final AtomicBoolean retrievedRelevant = new AtomicBoolean(false);

        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        mockController.replay();

        final Set<IssueSearcher> relevant = CollectionBuilder.newBuilder(issueSearcher).asSet();

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            RelevantSearcherVisitor createRelevantSearcherVisitor(final User user)
            {
                return new RelevantSearcherVisitor(searchHandlerManager, null)
                {
                    @Override
                    public Boolean visit(final TerminalClause terminalClause)
                    {
                        visited.set(true);
                        return true;
                    }

                    @Override
                    public Set<IssueSearcher> getRelevantSearchers()
                    {
                        retrievedRelevant.set(true);
                        return relevant;
                    }
                };
            }
        };

        Set<IssueSearcher> result = searchService.getRelevantQuerySearchers(null, new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value")));
        assertTrue(visited.get());
        assertTrue(retrievedRelevant.get());
        assertEquals(relevant, result);

        mockController.verify();
    }

    @Test
    public void testValidateQueryHappyPath() throws Exception
    {
        final User theUser = null;
        final OrderBy orderBy = new OrderByImpl(new SearchSort("blah"));
        final ValidatorVisitor validatorVisitor = new MyValidatorVisitor();
        final long filterId = 1010L;

        final ValidatorVisitor.ValidatorVisitorFactory validatorVisitorFactory = mockController.getMock(ValidatorVisitor.ValidatorVisitorFactory.class);
        validatorVisitorFactory.createVisitor(theUser, filterId);
        mockController.setReturnValue(validatorVisitor);

        final Clause clause = mockController.getMock(Clause.class);

        final Query query = mockController.getMock(Query.class);
        query.getWhereClause();
        mockController.setReturnValue(clause);

        query.getOrderByClause();
        mockController.setReturnValue(orderBy);

        final MessageSetImpl result = new MessageSetImpl();
        clause.accept(validatorVisitor);
        mockController.setReturnValue(result);

        orderByValidator.validate(theUser, orderBy);
        mockController.setReturnValue(new MessageSetImpl());

        mockController.replay();

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);
        assertEquals(result, searchService.validateQuery(theUser, query, filterId));

        mockController.verify();
    }

    @Test
    public void testValidateAllQuery() throws Exception
    {
        final User theUser = null;

        final Query query = mockController.getMock(Query.class);
        query.getWhereClause();
        mockController.setReturnValue(null);

        query.getOrderByClause();
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);
        assertEquals(new MessageSetImpl(), searchService.validateQuery(theUser, query));

        mockController.verify();
    }

    @Test
    public void testValidateQueryBadSort() throws Exception
    {
        final User theUser = null;
        final OrderBy orderBy = new OrderByImpl(new SearchSort("blah"));
        final ValidatorVisitor validatorVisitor = new MyValidatorVisitor();

        final ValidatorVisitor.ValidatorVisitorFactory validatorVisitorFactory = mockController.getMock(ValidatorVisitor.ValidatorVisitorFactory.class);
        validatorVisitorFactory.createVisitor(theUser, null);
        mockController.setReturnValue(validatorVisitor);

        final Clause clause = mockController.getMock(Clause.class);

        final Query query = mockController.getMock(Query.class);
        query.getWhereClause();
        mockController.setReturnValue(clause);

        query.getOrderByClause();
        mockController.setReturnValue(orderBy);

        final MessageSet clauseResult = new MessageSetImpl();
        clause.accept(validatorVisitor);
        mockController.setReturnValue(clauseResult);

        final MessageSet orderByResult = new MessageSetImpl();
        orderByResult.addErrorMessage("baderror");
        clauseResult.addMessageSet(orderByResult);
        orderByValidator.validate(theUser, orderBy);
        mockController.setReturnValue(orderByResult);

        mockController.replay();

        MessageSet expectedSet = new MessageSetImpl();
        expectedSet.addMessageSet(clauseResult);
        expectedSet.addMessageSet(orderByResult);

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);
        assertEquals(expectedSet, searchService.validateQuery(theUser, query));

        mockController.verify();
    }

    @Test
    public void testValidateQueryNoSortBadQuery() throws Exception
    {
        final User theUser = null;

        final ValidatorVisitor validatorVisitor = new MyValidatorVisitor();
        final ValidatorVisitor.ValidatorVisitorFactory validatorVisitorFactory = mockController.getMock(ValidatorVisitor.ValidatorVisitorFactory.class);

        validatorVisitorFactory.createVisitor(theUser, null);
        mockController.setReturnValue(validatorVisitor);

        final Clause clause = mockController.getMock(Clause.class);

        final Query query = mockController.getMock(Query.class);
        query.getWhereClause();
        mockController.setReturnValue(clause);

        final MessageSet clauseErrors = new MessageSetImpl();
        clauseErrors.addErrorMessage("baderror");

        clause.accept(validatorVisitor);
        mockController.setReturnValue(clauseErrors);

        query.getOrderByClause();
        mockController.setReturnValue(null);

        mockController.replay();

        MessageSet expectedSet = new MessageSetImpl();
        expectedSet.addMessageSet(clauseErrors);

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);
        assertEquals(expectedSet, searchService.validateQuery(theUser, query));

        mockController.verify();
    }


    @Test
    public void testValidateQueryBadSortAllQuery() throws Exception
    {
        final User theUser = null;
        final OrderBy orderBy = new OrderByImpl(new SearchSort("blah"));

        final ValidatorVisitor.ValidatorVisitorFactory validatorVisitorFactory = mockController.getMock(ValidatorVisitor.ValidatorVisitorFactory.class);

        final Query query = mockController.getMock(Query.class);
        query.getWhereClause();
        mockController.setReturnValue(null);

        query.getOrderByClause();
        mockController.setReturnValue(orderBy);

        final MessageSet orderByResult = new MessageSetImpl();
        orderByResult.addErrorMessage("baderror");
        orderByValidator.validate(theUser, orderBy);
        mockController.setReturnValue(orderByResult);

        mockController.replay();

        MessageSet expectedSet = new MessageSetImpl();
        expectedSet.addMessageSet(orderByResult);

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);
        assertEquals(expectedSet, searchService.validateQuery(theUser, query));

        mockController.verify();
    }

    @Test
    public void testValidateQueryBadClause() throws Exception
    {
        final User theUser = null;
        final OrderBy orderBy = new OrderByImpl(new SearchSort("blah"));
        final ValidatorVisitor validatorVisitor = new MyValidatorVisitor();

        final ValidatorVisitor.ValidatorVisitorFactory validatorVisitorFactory = mockController.getMock(ValidatorVisitor.ValidatorVisitorFactory.class);
        validatorVisitorFactory.createVisitor(theUser, null);
        mockController.setReturnValue(validatorVisitor);

        final Clause clause = mockController.getMock(Clause.class);

        final Query query = mockController.getMock(Query.class);
        query.getWhereClause();
        mockController.setReturnValue(clause);

        query.getOrderByClause();
        mockController.setReturnValue(orderBy);

        final MessageSet clauseResult = new MessageSetImpl();
        clauseResult.addErrorMessage("gooderror");
        clause.accept(validatorVisitor);
        mockController.setReturnValue(clauseResult);

        final MessageSet orderByResult = new MessageSetImpl();
        orderByValidator.validate(theUser, orderBy);
        mockController.setReturnValue(orderByResult);

        mockController.replay();

        MessageSet expectedSet = new MessageSetImpl();
        expectedSet.addMessageSet(clauseResult);
        expectedSet.addMessageSet(orderByResult);

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);
        assertEquals(expectedSet, searchService.validateQuery(theUser, query));

        mockController.verify();
    }

    @Test
    public void testValidateQueryBadClauseAndBadOrder() throws Exception
    {
        final User theUser = null;
        final OrderBy orderBy = new OrderByImpl(new SearchSort("blah"));
        final ValidatorVisitor validatorVisitor = new MyValidatorVisitor();

        final ValidatorVisitor.ValidatorVisitorFactory validatorVisitorFactory = mockController.getMock(ValidatorVisitor.ValidatorVisitorFactory.class);
        validatorVisitorFactory.createVisitor(theUser, null);
        mockController.setReturnValue(validatorVisitor);

        final Clause clause = mockController.getMock(Clause.class);

        final Query query = mockController.getMock(Query.class);
        query.getWhereClause();
        mockController.setReturnValue(clause);

        query.getOrderByClause();
        mockController.setReturnValue(orderBy);

        final MessageSet clauseResult = new MessageSetImpl();
        clauseResult.addErrorMessage("clauseerror");
        clauseResult.addErrorMessage("why");
        clause.accept(validatorVisitor);
        mockController.setReturnValue(clauseResult);

        final MessageSet orderByResult = new MessageSetImpl();
        orderByResult.addErrorMessage("ordererror");
        orderByValidator.validate(theUser, orderBy);
        mockController.setReturnValue(orderByResult);

        mockController.replay();

        MessageSet expectedSet = new MessageSetImpl();
        expectedSet.addMessageSet(clauseResult);
        expectedSet.addMessageSet(orderByResult);

        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);
        assertEquals(expectedSet, searchService.validateQuery(theUser, query));

        mockController.verify();
    }

    /*
     * Test that the service returns the user entered string if available.
     */
    @Test
    public void testGetJqlStringSupplied() throws Exception
    {
        final DefaultSearchService service = mockController.instantiate(DefaultSearchService.class);

        final String originalJql = "priority = major";
        Query query = new QueryImpl(new TerminalClauseImpl("random", Operator.EQUALS, "stuff"), originalJql);
        assertEquals(originalJql, service.getJqlString(query));

        final String emptyJql = "";
        query = new QueryImpl(new TerminalClauseImpl("random", Operator.EQUALS, "stuff"), emptyJql);
        assertEquals(emptyJql, service.getJqlString(query));

        mockController.verify();
    }

    @Test
    public void testGetJqlStringGenerated() throws Exception
    {
        Query query = new QueryImpl(new TerminalClauseImpl("random", Operator.EQUALS, "stuff"), null);

        final String expectedJql = "somejql = string";

        final JqlStringSupport stringSupport = mockController.getMock(JqlStringSupport.class);
        stringSupport.generateJqlString(query);
        mockController.setReturnValue(expectedJql);

        final DefaultSearchService service = mockController.instantiate(DefaultSearchService.class);
        final String actualJql = service.getJqlString(query);
        assertEquals(expectedJql, actualJql);
    }

    @Test
    public void testGetGeneratedJqlString() throws Exception
    {
        Query query = new QueryImpl(new TerminalClauseImpl("random", Operator.EQUALS, "stuff"), "priority = major");

        final String expectedJql = "somejql = string";

        final JqlStringSupport stringSupport = mockController.getMock(JqlStringSupport.class);
        stringSupport.generateJqlString(query);
        mockController.setReturnValue(expectedJql);


        final DefaultSearchService service = mockController.instantiate(DefaultSearchService.class);
        final String actualJql = service.getGeneratedJqlString(query);
        assertEquals(expectedJql, actualJql);
    }

    @Test
    public void testSanitiseSearchQueryAllQuery() throws Exception
    {
        final Query query = new QueryImpl();
        final DefaultSearchService service = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);

        mockController.replay();

        assertSame(query, service.sanitiseSearchQuery(null, query));

        mockController.verify();
    }

    @Test
    public void testSanitiseSearchQueryNoChange() throws Exception
    {
        final TerminalClause inputClause = new TerminalClauseImpl("field", Operator.EQUALS, "value");
        final Query query = new QueryImpl(inputClause);
        final ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, null)
        {
            @Override
            public Clause visit(final TerminalClause clause)
            {
                assertEquals(inputClause, clause);
                return clause;
            }
        };

        final DefaultSearchService service = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            ClauseSanitisingVisitor createClauseSanitisingVisitor(final User searcher)
            {
                return visitor;
            }
        };

        mockController.replay();

        assertSame(query, service.sanitiseSearchQuery(null, query));

        mockController.verify();
    }

    @Test
    public void testSanitiseSearchQueryChangeMade() throws Exception
    {
        final TerminalClause inputClause = new TerminalClauseImpl("field", Operator.EQUALS, "value1");
        final TerminalClause outputClause = new TerminalClauseImpl("field", Operator.EQUALS, "value2");
        final OrderByImpl orderByClause = new OrderByImpl(new SearchSort("field", SortOrder.DESC));
        final String originalQuery = "I contain sensitive information";
        final Query query = new QueryImpl(inputClause, orderByClause, originalQuery);
        final ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, null)
        {
            @Override
            public Clause visit(final TerminalClause clause)
            {
                assertEquals(inputClause, clause);
                return outputClause;
            }
        };

        final DefaultSearchService service = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            ClauseSanitisingVisitor createClauseSanitisingVisitor(final User searcher)
            {
                return visitor;
            }
        };

        mockController.replay();

        final Query resultQuery = service.sanitiseSearchQuery(null, query);
        assertNotSame(query, resultQuery);
        assertEquals(outputClause, resultQuery.getWhereClause());
        assertEquals(orderByClause, resultQuery.getOrderByClause());
        assertNull(resultQuery.getQueryString());

        mockController.verify();
    }

    @Test
    public void testCheckValidationMatchesNoClauseErrors() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("clause", Operator.EQUALS, "value");

        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        final ValidatorVisitor validatorVisitor = new MyValidatorVisitor(new MessageSetImpl());

        mockController.replay();
        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory);
        searchService.checkValidationMatches(null, new QueryImpl(), searchContext, clause, issueSearcher, validatorVisitor);
        mockController.verify();
    }

    @Test
    public void testCheckValidationMatchesClauseErrorsNoSearcherErrors() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("clause", Operator.EQUALS, "value");
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        ErrorCollection errors = new SimpleErrorCollection();
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("blarG!");
        final I18nHelper i18n = new MockI18nHelper();

        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        final ValidatorVisitor validatorVisitor = new MyValidatorVisitor(messageSet);
        final SearchInputTransformer transformer = mockController.getMock(SearchInputTransformer.class);

        issueSearcher.getSearchInputTransformer();
        mockController.setReturnValue(transformer);

        final QueryImpl query = new QueryImpl();
        transformer.populateFromQuery(null, holder, query, searchContext);
        transformer.validateParams(null, searchContext, holder, i18n, errors);


        mockController.replay();
        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return i18n;
            }
        };

        assertFalse(searchService.checkValidationMatches(null, query, searchContext, clause, issueSearcher, validatorVisitor));
        mockController.verify();
    }

    @Test
    public void testCheckValidationMatchesClauseErrorsAndSearcherErrors() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("clause", Operator.EQUALS, "value");
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addError("blah", "blah");
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("blarG!");
        final I18nHelper i18n = new MockI18nHelper();

        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        final ValidatorVisitor validatorVisitor = new MyValidatorVisitor(messageSet);

        final AtomicBoolean populateCalled = new AtomicBoolean(false);
        final AtomicBoolean validateCalled = new AtomicBoolean(false);
        final SearchInputTransformer transformer = new SearchInputTransformer()
        {
            public void populateFromParams(final User user, final FieldValuesHolder fieldValuesHolder, final ActionParams actionParams)
            {

            }

            public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
            {
                errors.addError("blah", "bah");
                validateCalled.set(true);
            }

            public void populateFromQuery(final User user, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
            {
                populateCalled.set(true);
            }

            public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
            {
                return false;
            }

            public Clause getSearchClause(final User user, final FieldValuesHolder fieldValuesHolder)
            {
                return null;
            }

        };

        issueSearcher.getSearchInputTransformer();
        mockController.setReturnValue(transformer);

        mockController.replay();
        DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return i18n;
            }
        };

        assertTrue(searchService.checkValidationMatches(null, new QueryImpl(), searchContext, clause, issueSearcher, validatorVisitor));
        assertTrue(populateCalled.get());
        assertTrue(validateCalled.get());
        mockController.verify();
    }

    @Test
    public void testCalculateDoesQueryValidationFitFilterFormAllHappy() throws Exception
    {
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        validatorVisitorFactory.createVisitor(null, null);
        mockController.setReturnValue(new MyValidatorVisitor());

        final IssueSearcher issueSearcher1 = mockController.getMock(IssueSearcher.class);
        final IssueSearcher issueSearcher2 = mockController.getMock(IssueSearcher.class);

        TerminalClause clause1 = new TerminalClauseImpl("name1", Operator.EQUALS, "val1");
        TerminalClause clause2 = new TerminalClauseImpl("name2", Operator.EQUALS, "val2");
        AndClause andClause = new AndClause(clause1, clause2);

        User anyUser = null;
        searchHandlerManager.getSearchersByClauseName(anyUser, "name1");
        mockController.setReturnValue(CollectionBuilder.newBuilder(issueSearcher1).asCollection());

        searchHandlerManager.getSearchersByClauseName(anyUser, "name2");
        mockController.setReturnValue(CollectionBuilder.newBuilder(issueSearcher2).asCollection());

        mockController.replay();
        final AtomicInteger calls = new AtomicInteger(0);
        final DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            boolean checkValidationMatches(final User user, final Query query, final SearchContext searchContext, final TerminalClause clause, final IssueSearcher searcher, final ClauseVisitor<MessageSet> validatorVisitor)
            {
                calls.incrementAndGet();
                return true;
            }
        };

        assertTrue(searchService.calculateDoesQueryValidationFitFilterForm(null, searchContext, new QueryImpl(andClause)));
        assertEquals(2, calls.get());
        mockController.verify();
    }

    @Test
    public void testCalculateDoesQueryValidationFitFilterFormOneSad() throws Exception
    {
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        validatorVisitorFactory.createVisitor(null, null);
        mockController.setReturnValue(new MyValidatorVisitor());

        final IssueSearcher issueSearcher1 = mockController.getMock(IssueSearcher.class);
        final IssueSearcher issueSearcher2 = mockController.getMock(IssueSearcher.class);

        TerminalClause clause1 = new TerminalClauseImpl("name1", Operator.EQUALS, "val1");
        TerminalClause clause2 = new TerminalClauseImpl("name2", Operator.EQUALS, "val2");
        AndClause andClause = new AndClause(clause1, clause2);

        User anyUser = null;
        searchHandlerManager.getSearchersByClauseName(anyUser, "name1");
        mockController.setReturnValue(CollectionBuilder.newBuilder(issueSearcher1).asCollection());

        searchHandlerManager.getSearchersByClauseName(anyUser, "name2");
        mockController.setReturnValue(CollectionBuilder.newBuilder(issueSearcher2).asCollection());

        mockController.replay();
        final AtomicInteger calls = new AtomicInteger(0);
        final DefaultSearchService searchService = new DefaultSearchService(searchHandlerManager, jqlQueryParser, validatorVisitorFactory, contextVisitorFactory, jqlStringSupport, queryContextConverter, queryCache, jqlOperandResolver, orderByValidator, searchProvider, factory)
        {
            @Override
            boolean checkValidationMatches(final User user, final Query query, final SearchContext searchContext, final TerminalClause clause, final IssueSearcher searcher, final ClauseVisitor<MessageSet> validatorVisitor)
            {
                return calls.incrementAndGet() != 2;
            }
        };

        assertFalse(searchService.calculateDoesQueryValidationFitFilterForm(null, searchContext, new QueryImpl(andClause)));

        assertEquals(2, calls.get());
        mockController.verify();
    }

    private static class MySearchContext implements SearchContext
    {
        private final List<Long> pids;
        private final List<String> tids;
        private final boolean forSingleProject;

        MySearchContext()
        {
            this(new ArrayList<Long>(), new ArrayList<String>(), false);
        }

        MySearchContext(List<Long> pids, List<String> tids, boolean isForSingleProject)
        {

            this.pids = pids;
            this.tids = tids;
            forSingleProject = isForSingleProject;
        }

        public boolean isForAnyProjects()
        {
            return false;
        }

        public boolean isForAnyIssueTypes()
        {
            return false;
        }

        public boolean isSingleProjectContext()
        {
            return forSingleProject;
        }

        @Override
        public Project getSingleProject()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        public List getProjectCategoryIds()
        {
            return null;
        }

        public List<Long> getProjectIds()
        {
            return pids;
        }

        public GenericValue getOnlyProject()
        {
            return null;
        }

        public List<String> getIssueTypeIds()
        {
            return tids;
        }

        public List<IssueContext> getAsIssueContexts()
        {
            return null;
        }

        public void verify()
        {
        }

        @Override
        public List<Project> getProjects()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<IssueType> getIssueTypes()
        {
            throw new UnsupportedOperationException();
        }
    }

    private static class MyValidatorVisitor extends ValidatorVisitor
    {
        private final MessageSet messageSet;

        public MyValidatorVisitor(MessageSet messageSet)
        {
            super(null, null, null, null, null);
            this.messageSet = messageSet;
        }

        public MyValidatorVisitor()
        {
            super(null, null, null, null, null);
            this.messageSet = new MessageSetImpl();
        }

        @Override
        public MessageSet visit(final AndClause andClause)
        {
            return messageSet;
        }

        @Override
        public MessageSet visit(final TerminalClause clause)
        {
            return messageSet;
        }

        @Override
        public MessageSet visit(final NotClause notClause)
        {
            return messageSet;
        }

        @Override
        public MessageSet visit(final OrClause orClause)
        {
            return messageSet;
        }
    }
}
