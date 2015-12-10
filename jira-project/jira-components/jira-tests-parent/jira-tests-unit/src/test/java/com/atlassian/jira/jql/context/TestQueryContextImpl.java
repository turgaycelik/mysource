package com.atlassian.jira.jql.context;

import java.util.Collection;
import java.util.Set;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestQueryContextImpl extends MockControllerTestCase
{
    @Test
    public void testGetProjectIssueTypeContextsExplicit() throws Exception
    {
        final Set<IssueTypeContext> types1 = CollectionBuilder.<IssueTypeContext>newBuilder(new IssueTypeContextImpl("it1"), new IssueTypeContextImpl("it2")).asSet();
        final Set<IssueTypeContext> types2 = CollectionBuilder.<IssueTypeContext>newBuilder(new IssueTypeContextImpl("it1"), new IssueTypeContextImpl("it3")).asSet();
        QueryContext.ProjectIssueTypeContexts context1 = new QueryContext.ProjectIssueTypeContexts(new ProjectContextImpl(10L), types1);
        QueryContext.ProjectIssueTypeContexts context2 = new QueryContext.ProjectIssueTypeContexts(new ProjectContextImpl(20L), types2);


        ProjectIssueTypeContext inputContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it1"));
        ProjectIssueTypeContext inputContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it2"));
        ProjectIssueTypeContext inputContext3 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("it1"));
        ProjectIssueTypeContext inputContext4 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("it3"));
        ClauseContext clauseContext = new ClauseContextImpl(CollectionBuilder.newBuilder(inputContext1, inputContext2, inputContext3, inputContext4).asSet());

        QueryContext queryContext = new QueryContextImpl(clauseContext);
        final Collection<QueryContext.ProjectIssueTypeContexts> contextsCollection = queryContext.getProjectIssueTypeContexts();
        assertEquals(2, contextsCollection.size());
        assertTrue(contextsCollection.contains(context1));
        assertTrue(contextsCollection.contains(context2));
    }

    @Test
    public void testGetProjectIssueTypeContextsImplicit() throws Exception
    {
        final Set<IssueTypeContext> types = CollectionBuilder.<IssueTypeContext>newBuilder(new IssueTypeContextImpl("it3")).asSet();
        QueryContext.ProjectIssueTypeContexts context = new QueryContext.ProjectIssueTypeContexts(new ProjectContextImpl(20L), types);

        ProjectIssueTypeContext inputContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("it3"));
        ClauseContext clauseContext = new ClauseContextImpl(CollectionBuilder.newBuilder(inputContext).asSet());

        QueryContext queryContext = new QueryContextImpl(clauseContext);
        final Collection<QueryContext.ProjectIssueTypeContexts> contextsCollection = queryContext.getProjectIssueTypeContexts();
        assertEquals(1, contextsCollection.size());
        assertTrue(contextsCollection.contains(context));
    }

    @Test
    public void testGetProjectIssueTypeContextsAll() throws Exception
    {
        final Set<IssueTypeContext> types = CollectionBuilder.<IssueTypeContext>newBuilder(new IssueTypeContextImpl("it3")).asSet();
        QueryContext.ProjectIssueTypeContexts context = new QueryContext.ProjectIssueTypeContexts(AllProjectsContext.INSTANCE, types);

        ProjectIssueTypeContext inputContext = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it3"));
        ClauseContext clauseContext = new ClauseContextImpl(CollectionBuilder.newBuilder(inputContext).asSet());

        QueryContext queryContext = new QueryContextImpl(clauseContext);
        final Collection<QueryContext.ProjectIssueTypeContexts> contextsCollection = queryContext.getProjectIssueTypeContexts();
        assertEquals(1, contextsCollection.size());
        assertTrue(contextsCollection.contains(context));
    }
    
    @Test
    public void testGetProjectIssueTypeContextsAllImplicit() throws Exception
    {
        final Set<IssueTypeContext> types = CollectionBuilder.<IssueTypeContext>newBuilder(new IssueTypeContextImpl("it3")).asSet();
        QueryContext.ProjectIssueTypeContexts context = new QueryContext.ProjectIssueTypeContexts(AllProjectsContext.INSTANCE, types);

        ProjectIssueTypeContext inputContext = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it3"));
        ClauseContext clauseContext = new ClauseContextImpl(CollectionBuilder.newBuilder(inputContext).asSet());

        QueryContext queryContext = new QueryContextImpl(clauseContext);
        final Collection<QueryContext.ProjectIssueTypeContexts> contextsCollection = queryContext.getProjectIssueTypeContexts();
        assertEquals(1, contextsCollection.size());
        assertTrue(contextsCollection.contains(context));
    }
}
