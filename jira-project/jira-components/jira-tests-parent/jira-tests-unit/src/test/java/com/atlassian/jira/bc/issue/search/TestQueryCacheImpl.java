package com.atlassian.jira.bc.issue.search;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.jql.context.AllIssueTypesContext;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.ProjectContextImpl;
import com.atlassian.jira.jql.context.ProjectIssueTypeContext;
import com.atlassian.jira.jql.context.ProjectIssueTypeContextImpl;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.context.QueryContextImpl;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestQueryCacheImpl
{
    @Test
    public void testFitCacheNullUser() throws Exception
    {
        final Map<QueryCacheImpl.QueryCacheKey, Boolean> cache = new HashMap<QueryCacheImpl.QueryCacheKey, Boolean>();

        final QueryCacheImpl queryCache = new QueryCacheImpl()
        {
            @Override
            Map<QueryCacheKey, Boolean> getFitCache()
            {
                return cache;
            }
        };

        final QueryImpl searchQuery1 = new QueryImpl(new TerminalClauseImpl("1", Operator.EQUALS, "1"));
        final QueryImpl searchQuery2 = new QueryImpl(new TerminalClauseImpl("2", Operator.EQUALS, "2"));
        final QueryImpl searchQuery3 = new QueryImpl(new TerminalClauseImpl("3", Operator.EQUALS, "3"));

        queryCache.setDoesQueryFitFilterFormCache(null, searchQuery1, true);
        queryCache.setDoesQueryFitFilterFormCache(null, searchQuery2, false);

        assertTrue(queryCache.getDoesQueryFitFilterFormCache(null, searchQuery1));
        assertFalse(queryCache.getDoesQueryFitFilterFormCache(null, searchQuery2));
        assertNull(queryCache.getDoesQueryFitFilterFormCache(null, searchQuery3));
    }

    @Test
    public void testFitCache() throws Exception
    {
        User user1 = ImmutableUser.newUser().name("user1").directoryId(1).toUser();
        User user1Copy = new MockUser("user1");
        User user2 = new MockUser("user2");

        final Map<QueryCacheImpl.QueryCacheKey, Boolean> cache = new HashMap<QueryCacheImpl.QueryCacheKey, Boolean>();

        final QueryCacheImpl queryCache = new QueryCacheImpl()
        {
            @Override
            Map<QueryCacheKey, Boolean> getFitCache()
            {
                return cache;
            }
        };

        final QueryImpl searchQuery1 = new QueryImpl(new TerminalClauseImpl("1", Operator.EQUALS, "1"));
        final QueryImpl searchQuery2 = new QueryImpl(new TerminalClauseImpl("2", Operator.EQUALS, "2"));
        final QueryImpl searchQuery3 = new QueryImpl(new TerminalClauseImpl("3", Operator.EQUALS, "3"));

        queryCache.setDoesQueryFitFilterFormCache(user1, searchQuery1, true);
        queryCache.setDoesQueryFitFilterFormCache(user1, searchQuery2, false);

        // searchQuery1
        assertTrue(queryCache.getDoesQueryFitFilterFormCache(user1, searchQuery1));
        assertTrue(queryCache.getDoesQueryFitFilterFormCache(user1Copy, searchQuery1));
        assertNull(queryCache.getDoesQueryFitFilterFormCache(user2, searchQuery1));
        assertNull(queryCache.getDoesQueryFitFilterFormCache(null, searchQuery1));
        // searchQuery2
        assertFalse(queryCache.getDoesQueryFitFilterFormCache(user1, searchQuery2));
        assertFalse(queryCache.getDoesQueryFitFilterFormCache(user1Copy, searchQuery2));
        assertNull(queryCache.getDoesQueryFitFilterFormCache(user2, searchQuery2));
        assertNull(queryCache.getDoesQueryFitFilterFormCache(null, searchQuery2));
        // searchQuery3
        assertNull(queryCache.getDoesQueryFitFilterFormCache(user1, searchQuery3));
        assertNull(queryCache.getDoesQueryFitFilterFormCache(null, searchQuery3));
    }

    @Test
    public void testContextCache() throws Exception
    {
        final Map<QueryCacheImpl.QueryCacheKey, QueryContext> cache = new HashMap<QueryCacheImpl.QueryCacheKey, QueryContext>();

        final QueryCacheImpl queryCache = new QueryCacheImpl()
        {
            @Override
            Map<QueryCacheKey, QueryContext> getQueryCache()
            {
                return cache;
            }
        };

        final QueryImpl searchQuery1 = new QueryImpl(new TerminalClauseImpl("1", Operator.EQUALS, "1"));
        final QueryImpl searchQuery2 = new QueryImpl(new TerminalClauseImpl("2", Operator.EQUALS, "2"));
        final QueryImpl searchQuery3 = new QueryImpl(new TerminalClauseImpl("3", Operator.EQUALS, "3"));

        QueryContext queryContext1 = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE)).asSet()));
        QueryContext queryContext2 = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(15L), AllIssueTypesContext.INSTANCE)).asSet()));

        queryCache.setQueryContextCache(null, searchQuery1, queryContext1);
        queryCache.setQueryContextCache(null, searchQuery2, queryContext2);

        assertEquals(queryContext1, queryCache.getQueryContextCache(null, searchQuery1));
        assertEquals(queryContext2, queryCache.getQueryContextCache(null, searchQuery2));
        assertNull(queryCache.getQueryContextCache(null, searchQuery3));
    }

    @Test
    public void testSimpleContextCache() throws Exception
    {
        final Map<QueryCacheImpl.QueryCacheKey, QueryContext> cache = new HashMap<QueryCacheImpl.QueryCacheKey, QueryContext>();

        final QueryCacheImpl queryCache = new QueryCacheImpl()
        {
            @Override
            Map<QueryCacheKey, QueryContext> getQueryCache()
            {
                return cache;
            }
        };

        final QueryImpl searchQuery1 = new QueryImpl(new TerminalClauseImpl("1", Operator.EQUALS, "1"));
        final QueryImpl searchQuery2 = new QueryImpl(new TerminalClauseImpl("2", Operator.EQUALS, "2"));
        final QueryImpl searchQuery3 = new QueryImpl(new TerminalClauseImpl("3", Operator.EQUALS, "3"));

        QueryContext queryContext1 = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE)).asSet()));
        QueryContext queryContext2 = new QueryContextImpl(new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(15L), AllIssueTypesContext.INSTANCE)).asSet()));

        queryCache.setSimpleQueryContextCache(null, searchQuery1, queryContext1);
        queryCache.setSimpleQueryContextCache(null, searchQuery2, queryContext2);

        assertEquals(queryContext1, queryCache.getSimpleQueryContextCache(null, searchQuery1));
        assertEquals(queryContext2, queryCache.getSimpleQueryContextCache(null, searchQuery2));
        assertNull(queryCache.getQueryContextCache(null, searchQuery3));
    }
}
