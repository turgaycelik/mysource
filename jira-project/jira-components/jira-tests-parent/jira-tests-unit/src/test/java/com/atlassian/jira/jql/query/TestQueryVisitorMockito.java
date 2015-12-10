package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.WasClause;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestQueryVisitorMockito
{
    private User user = new MockUser("mockedUser");

    @Test
    public void testQueryVisitorWithWasClauseShouldUseProvidedWasClauseQueryFactory() throws Exception
    {
        MockComponentWorker componentWorker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(componentWorker);

        final WasClause wasClause = mock(WasClause.class);
        final WasClauseQueryFactory wasClauseQueryFactory = mock(WasClauseQueryFactory.class);

        final QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        when(wasClauseQueryFactory.create(user, wasClause)).thenReturn(expectedResult);

        final QueryVisitor queryVisitor = new QueryVisitor(null, new QueryCreationContextImpl(user), wasClauseQueryFactory, null);

        final QueryFactoryResult actualResult = queryVisitor.visit(wasClause);
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(wasClauseQueryFactory).create(user, wasClause);
    }

    @Test
    public void testQueryVisitorWithChangedClauseShouldUseProvidedChangedClauseQueryFactory() throws Exception
    {
        MockComponentWorker componentWorker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(componentWorker);

        final ChangedClause changedClause = mock(ChangedClause.class);
        final ChangedClauseQueryFactory changedClauseQueryFactory = mock(ChangedClauseQueryFactory.class);

        final QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        when(changedClauseQueryFactory.create(user, changedClause)).thenReturn(expectedResult);

        final QueryVisitor queryVisitor = new QueryVisitor(null, new QueryCreationContextImpl(user), null, changedClauseQueryFactory);

        final QueryFactoryResult actualResult = queryVisitor.visit(changedClause);
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(changedClauseQueryFactory).create(user, changedClause);
    }
}
