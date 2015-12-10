package com.atlassian.jira.jql.query;

import java.util.Collection;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link com.atlassian.jira.jql.query.DefaultQueryRegistry}.
 *
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultQueryRegistry
{
    private static final User ANONYMOUS = null;
    private static final String CLAUSE_NAME = "clauseName";

    @Mock ClauseQueryFactory clauseQueryFactory;
    @Mock ClauseHandler clauseHandler;
    @Mock SearchHandlerManager searchHandlerManager;

    QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(ANONYMOUS);
    }

    @After
    public void tearDown()
    {
        queryCreationContext = null;
        clauseQueryFactory = null;
        clauseHandler = null;
        searchHandlerManager = null;
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor()
    {
        new DefaultQueryRegistry(null);
    }

    @Test
    public void testGetClauseQueryFactory() throws Exception
    {
        when(clauseHandler.getFactory()).thenReturn(clauseQueryFactory);
        when(searchHandlerManager.getClauseHandler(ANONYMOUS, CLAUSE_NAME)).thenReturn(ImmutableList.of(clauseHandler));

        final DefaultQueryRegistry queryRegistry = fixture();
        final Collection<ClauseQueryFactory> result = queryRegistry.getClauseQueryFactory(
                queryCreationContext, new TerminalClauseImpl(CLAUSE_NAME, Operator.IN, "value"));
        assertThat(result, contains(clauseQueryFactory));

        // Make sure we didn't try without security applied
        verify(searchHandlerManager, never()).getClauseHandler(anyString());
    }

    @Test
    public void testGetClauseQueryFactoryOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(ANONYMOUS, true);

        when(clauseHandler.getFactory()).thenReturn(clauseQueryFactory);
        when(searchHandlerManager.getClauseHandler(CLAUSE_NAME)).thenReturn(ImmutableList.of(clauseHandler));

        final DefaultQueryRegistry queryRegistry = fixture();
        final Collection<ClauseQueryFactory> result = queryRegistry.getClauseQueryFactory(
                queryCreationContext, new TerminalClauseImpl(CLAUSE_NAME, Operator.IN, "value"));
        assertThat(result, contains(clauseQueryFactory));

        // Make sure we didn't try with security applied
        verify(searchHandlerManager, never()).getClauseHandler(any(User.class), anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetClauseQueryFactoryBadArgs() throws Exception
    {
        final DefaultQueryRegistry queryRegistry = fixture();
        queryRegistry.getClauseQueryFactory(queryCreationContext, null);
    }


    DefaultQueryRegistry fixture()
    {
        return new DefaultQueryRegistry(searchHandlerManager);
    }
}
