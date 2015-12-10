package com.atlassian.jira.bc.issue.search;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.jql.context.AllIssueTypesContext;
import com.atlassian.jira.jql.context.AllProjectsContext;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.IssueTypeContextImpl;
import com.atlassian.jira.jql.context.ProjectContextImpl;
import com.atlassian.jira.jql.context.ProjectIssueTypeContext;
import com.atlassian.jira.jql.context.ProjectIssueTypeContextImpl;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.context.QueryContextImpl;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.CollectionAssert;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestQueryContextConverter extends MockControllerTestCase
{
    @Test
    public void testGetQueryContextGlobal() throws Exception
    {
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        searchContext.isForAnyProjects();
        mockController.setDefaultReturnValue(true);

        searchContext.isForAnyIssueTypes();
        mockController.setDefaultReturnValue(true);

        mockController.replay();
        final QueryContextConverter converter = new QueryContextConverter();
        final QueryContext result = converter.getQueryContext(searchContext);
        QueryContext expectedResult = new QueryContextImpl(ClauseContextImpl.createGlobalClauseContext());
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetQueryContextAnyProjects() throws Exception
    {
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        searchContext.isForAnyProjects();
        mockController.setDefaultReturnValue(true);

        searchContext.isForAnyIssueTypes();
        mockController.setDefaultReturnValue(false);

        searchContext.getIssueTypeIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder("1", "2").asList());

        mockController.replay();
        final QueryContextConverter converter = new QueryContextConverter();
        final QueryContext result = converter.getQueryContext(searchContext);

        QueryContext expectedResult = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("1")),
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("2"))
        ).asSet()));
        
        assertEquals(expectedResult, result);
        mockController.verify();    
    }

    @Test
    public void testGetQueryContextAnyTypes() throws Exception
    {
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        searchContext.isForAnyProjects();
        mockController.setDefaultReturnValue(false);

        searchContext.isForAnyIssueTypes();
        mockController.setDefaultReturnValue(true);

        searchContext.getProjectIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder(1L, 2L).asList());

        mockController.replay();
        final QueryContextConverter converter = new QueryContextConverter();
        final QueryContext result = converter.getQueryContext(searchContext);

        QueryContext expectedResult = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(1L), AllIssueTypesContext.INSTANCE),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(2L), AllIssueTypesContext.INSTANCE)
        ).asSet()));

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetQueryContextBothSpecified() throws Exception
    {
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        searchContext.isForAnyProjects();
        mockController.setDefaultReturnValue(false);

        searchContext.isForAnyIssueTypes();
        mockController.setDefaultReturnValue(false);

        searchContext.getProjectIds();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder(1L, 2L).asList());

        searchContext.getIssueTypeIds();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder("1", "2").asList());

        mockController.replay();
        final QueryContextConverter converter = new QueryContextConverter();
        final QueryContext result = converter.getQueryContext(searchContext);

        QueryContext expectedResult = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(1L), new IssueTypeContextImpl("1")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(1L), new IssueTypeContextImpl("2")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(2L), new IssueTypeContextImpl("1")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(2L), new IssueTypeContextImpl("2"))
        ).asSet()));

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetSearchContextFromQueryContextAllAll() throws Exception
    {
        QueryContext queryContext = new QueryContextImpl(ClauseContextImpl.createGlobalClauseContext());


        final AtomicBoolean called = new AtomicBoolean(false);
        QueryContextConverter queryContextConverter = new QueryContextConverter()
        {
            @Override
            SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
            {
                called.set(true);
                assertTrue(projects.isEmpty());
                assertTrue(issueTypes.isEmpty());
                return null;
            }
        };

        queryContextConverter.getSearchContext(queryContext);
        assertTrue(called.get());
    }

    @Test
    public void testGetSearchContextFromQueryContextSpecific() throws Exception
    {
        QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it"))
        ).asSet()));

        final AtomicBoolean called = new AtomicBoolean(false);
        QueryContextConverter queryContextConverter = new QueryContextConverter()
        {
            @Override
            SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
            {
                called.set(true);
                assertEquals(1, projects.size());
                assertTrue(projects.contains(10L));
                assertEquals(1, issueTypes.size());
                assertTrue(issueTypes.contains("it"));
                return null;
            }
        };

        queryContextConverter.getSearchContext(queryContext);
        assertTrue(called.get());
    }

    @Test
    public void testGetSearchContextFromQueryContextDifferentIssueTypes() throws Exception
    {
        QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L), new IssueTypeContextImpl("it1")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L), new IssueTypeContextImpl("it2")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it2")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it3"))
        ).asSet()));

        QueryContextConverter queryContextConverter = new QueryContextConverter()
        {
            @Override
            SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
            {
                fail("Invalid Query Context Conversion");
                return null;
            }
        };

        assertNull(queryContextConverter.getSearchContext(queryContext));
    }

    @Test
    public void testGetSearchContextFromQueryContextSameIssueTypes() throws Exception
    {
        QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L), new IssueTypeContextImpl("it1")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L), new IssueTypeContextImpl("it2")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it1")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it2"))
        ).asSet()));

        final AtomicBoolean called = new AtomicBoolean(false);
        QueryContextConverter queryContextConverter = new QueryContextConverter()
        {
            @Override
            SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
            {
                called.set(true);
                CollectionAssert.assertContainsExactly(Arrays.asList(10L, 11L), projects);
                CollectionAssert.assertContainsExactly(Arrays.asList("it1", "it2"), issueTypes);
                return null;
            }
        };

        queryContextConverter.getSearchContext(queryContext);
        assertTrue(called.get());
    }

    @Test
    public void testGetSearchContextFromQueryContextAllWithSpecificProject() throws Exception
    {
        QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it1")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L), new IssueTypeContextImpl("it1"))
        ).asSet()));

        QueryContextConverter queryContextConverter = new QueryContextConverter()
        {
            @Override
            SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
            {
                fail("Invalid Query Context Conversion");
                return null;
            }
        };

        assertNull(queryContextConverter.getSearchContext(queryContext));
    }

    @Test
    public void testGetSearchContextFromQueryContextAllWithSpecificIssueType() throws Exception
    {
        QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L), AllIssueTypesContext.INSTANCE),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L), new IssueTypeContextImpl("it1"))
        ).asSet()));

        QueryContextConverter queryContextConverter = new QueryContextConverter()
        {
            @Override
            SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
            {
                fail("Invalid Query Context Conversion");
                return null;
            }
        };
        assertNull(queryContextConverter.getSearchContext(queryContext));
    }
}
