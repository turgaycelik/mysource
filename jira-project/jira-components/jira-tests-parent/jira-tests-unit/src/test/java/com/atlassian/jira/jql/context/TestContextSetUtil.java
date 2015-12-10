package com.atlassian.jira.jql.context;

import java.util.Collections;

import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public final class TestContextSetUtil
{
    private static final ContextSetUtil CONTEXT_SET_UTIL = ContextSetUtil.getInstance();

    @Test
    public void testIntersectionEmptySet() throws Exception
    {
        final ClauseContext clauseContext = CONTEXT_SET_UTIL.intersect(Collections.<ClauseContext>emptySet());
        assertNotNull(clauseContext);
        assertTrue(clauseContext.getContexts().isEmpty());
    }

    @Test
    public void testIntersectionNoMatchingByProject() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("11"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(11L);
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertTrue(result.getContexts().isEmpty());
    }

    @Test
    public void testIntersectionNoMatchingIssueType() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(11L);
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());
        
        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertTrue(result.getContexts().isEmpty());
    }

    @Test
    public void testIntersectionOneMatching() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(projectIssueTypeContext1, result.getContexts().iterator().next());
    }

    @Test
    public void testIntersectionOneMatchingPromotedElementType() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(projectIssueTypeContext1, result.getContexts().iterator().next());
    }

    @Test
    public void testIntersectionComplex() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(10L);

        final ProjectIssueTypeContext projectIssueTypeContext3 = context(11L);
        final ProjectIssueTypeContext projectIssueTypeContext4 = context(11L);

        final ProjectIssueTypeContext projectIssueTypeContext5 = context(11L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext3).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2, projectIssueTypeContext4).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(2, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
        assertTrue(result.getContexts().contains(projectIssueTypeContext5));
    }

    @Test
    public void testIntersectionOneMatchingOneEmpty() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertTrue(result.getContexts().isEmpty());
    }

    @Test
    public void testIntersectionOneMatchingOneEmptyOtherWayAround() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertTrue(result.getContexts().isEmpty());
    }

    @Test
    public void testIntersectionPreCondition() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = null;

        try
        {
            CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testIntersectionOfGlobalContextAndAllIssueTypeContext() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = ProjectIssueTypeContextImpl.createGlobalContext();
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());
        ClauseContext result= CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1,context2).asListOrderedSet());
        assertThat("A Global context intersected with an any issue type context should only contain a single result", result.getContexts(), hasSize(1));
        assertTrue("A Global context intersected with an any issue type context should result in an Any Issue Type context", result.getContexts().contains(projectIssueTypeContext1));
    }

    @Test
    public void testIntersectionComplexAlls() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext3 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext4 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContext projectIssueTypeContext5 = context(11L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext3, projectIssueTypeContext4, projectIssueTypeContext5).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2, projectIssueTypeContext2, projectIssueTypeContext3, projectIssueTypeContext4).asListOrderedSet());
        assertEquals(4, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext2));
        assertTrue(result.getContexts().contains(projectIssueTypeContext3));
        assertTrue(result.getContexts().contains(projectIssueTypeContext4));
        assertTrue(result.getContexts().contains(projectIssueTypeContext5));
    }

    @Test
    public void testUnionComplexAlls() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext3 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext4 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContext projectIssueTypeContext5 = context(11L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext3, projectIssueTypeContext4, projectIssueTypeContext5).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2, projectIssueTypeContext2, projectIssueTypeContext3, projectIssueTypeContext4).asListOrderedSet());
        assertEquals(5, result.getContexts().size());

        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
        assertTrue(result.getContexts().contains(projectIssueTypeContext2));
        assertTrue(result.getContexts().contains(projectIssueTypeContext3));
        assertTrue(result.getContexts().contains(projectIssueTypeContext4));
        assertTrue(result.getContexts().contains(projectIssueTypeContext5));
    }

    @Test
    public void testIntersectionOfGlobalContextAndIssueTypeContext() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("1"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = ProjectIssueTypeContextImpl.createGlobalContext();
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());
        ClauseContext result= CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1,context2).asListOrderedSet());
        assertThat("A Global context intersected with an IssueTypeContext should only contain a single result", result.getContexts(), hasSize(1));
        assertTrue("A Global context intersected with an IssueTypeContext should result in an IssueTypeContext project", result.getContexts().contains(projectIssueTypeContext1));
    }

    @Test
    public void testIntersectionOfAllProjectAndAllIssueType() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("1"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.getInstance());
        final ProjectIssueTypeContext projectIssueTypeContext3 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("1"));
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());
        ClauseContext result= CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertThat("An AllProject intersected with an AllIssueType  should only contain the single specific type", result.getContexts(), hasSize(1));
        assertTrue("An AllProject intersected with an AllIssueType project  should produce a specific typel", result.getContexts().contains(projectIssueTypeContext3));
    }

    @Test
    public void testIntersectionOfTwoAllIssueType() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.getInstance());
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext).asListOrderedSet());
        ClauseContext result= CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertThat("An AllssueType intersected with the same AllIssueType should only contain the AllIssueType", result.getContexts(), hasSize(1));
        assertTrue("An AllssueType intersected with the same Issue AllssueType should contain the AllssueType", result.getContexts().contains(projectIssueTypeContext));
    }

    @Test
    public void testIntersectionOfAllProjectWithIssueType() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("1"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("2"));
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());
        ClauseContext result= CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertThat( result.getContexts(), hasSize(1));
        assertTrue(result.getContexts().contains(projectIssueTypeContext2));
    }

    @Test
    public void testUnionNoEmptySet() throws Exception
    {
        final ClauseContext clauseContext = CONTEXT_SET_UTIL.union(Collections.<ClauseContext>emptySet());
        assertNotNull(clauseContext);
        assertTrue(clauseContext.getContexts().isEmpty());
    }

    @Test
    public void testUnionNoMatchingByProject() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("11"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(11L);
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(2, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
        assertTrue(result.getContexts().contains(projectIssueTypeContext2));
    }

    @Test
    public void testUnionNoMatchingIssueType() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(11L);
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(2, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
        assertTrue(result.getContexts().contains(projectIssueTypeContext2));
    }

    @Test
    public void testUnionOneMatching() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(projectIssueTypeContext1, result.getContexts().iterator().next());
    }

    @Test
    public void testUnionOneMatchingOneAll() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext2 = ProjectIssueTypeContextImpl.createGlobalContext();

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asListOrderedSet());
        assertEquals(2, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testUnionTwoAllIssueType() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = ProjectIssueTypeContextImpl.createGlobalContext();

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asListOrderedSet());
        assertEquals(2, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testUnionTwoAllProject() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testUnionTwoAllIssueTypeSameProject() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testIntersectionOneMatchingOneAll() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext2 = ProjectIssueTypeContextImpl.createGlobalContext();

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testIntersectionOneMatchingOneAllOtherOrder() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(context(10L)).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testIntersectionTwoAllIssueType() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = ProjectIssueTypeContextImpl.createGlobalContext();

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testIntersectionTwoAllProject() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testIntersectionTwoAllIssueTypeSameProject() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testUnionOneMatchingPromotedElementType() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(projectIssueTypeContext1, result.getContexts().iterator().next());
    }

    @Test
    public void testUnionComplex() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);
        final ProjectIssueTypeContext projectIssueTypeContext2 = context(10L);

        final ProjectIssueTypeContext projectIssueTypeContext3 = context(11L);
        final ProjectIssueTypeContext projectIssueTypeContext4 = context(11L);
        final ProjectIssueTypeContext projectIssueTypeContext5 = context(12L);

        final ProjectIssueTypeContext newHigherOrderProjectIssueTypeContext = context(11L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext3).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2, projectIssueTypeContext4, projectIssueTypeContext5).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(3, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
        assertTrue(result.getContexts().contains(projectIssueTypeContext5));
        assertTrue(result.getContexts().contains(newHigherOrderProjectIssueTypeContext));
    }

    @Test
    public void testUnionOneMatchingOneEmpty() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
    }

    @Test
    public void testUnionOneMatchingOneEmptyOtherWayAround() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());

        ClauseContext result = CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
    }

    private ProjectIssueTypeContextImpl context(Long pid)
    {
        return new ProjectIssueTypeContextImpl(new ProjectContextImpl(pid), new IssueTypeContextImpl(pid.toString()));
    }

    @Test
    public void testUnionPreCondition() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = context(10L);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = null;

        try
        {
            CONTEXT_SET_UTIL.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }
}
