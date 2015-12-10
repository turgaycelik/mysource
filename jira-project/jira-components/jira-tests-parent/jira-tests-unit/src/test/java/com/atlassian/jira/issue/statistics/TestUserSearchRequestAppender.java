package com.atlassian.jira.issue.statistics;

import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.query.operand.EmptyOperand.EMPTY;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.IS;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestUserSearchRequestAppender
{
    private UserStatisticsMapper.UserSearchRequestAppender userSearchRequestAppender;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        userSearchRequestAppender = new UserStatisticsMapper.UserSearchRequestAppender("jqlClauseName");
    }

    @Test
    public void appendInclusiveNullValueClause() throws Exception
    {
        final Clause originalClause = new TerminalClauseImpl("blah", EQUALS, "blah");
        final QueryImpl originalQuery = new QueryImpl(originalClause);
        SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final SearchRequest newSearchRequest = userSearchRequestAppender.appendInclusiveSingleValueClause(null, originalSearchRequest);

        final AndClause expectedAndClause = new AndClause(originalClause, new TerminalClauseImpl("jqlClauseName", IS, EMPTY));
        assertEquals(expectedAndClause, newSearchRequest.getQuery().getWhereClause());
    }

    @Test
    public void appendInclusiveSingleValueClause() throws Exception
    {
        final Clause originalClause = new TerminalClauseImpl("blah", EQUALS, "blah");
        final QueryImpl originalQuery = new QueryImpl(originalClause);
        SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final SearchRequest newSearchRequest = userSearchRequestAppender.appendInclusiveSingleValueClause(new MockUser("me"), originalSearchRequest);

        final AndClause expectedAndClause = new AndClause(originalClause, new TerminalClauseImpl("jqlClauseName", EQUALS, "me"));
        assertEquals(expectedAndClause, newSearchRequest.getQuery().getWhereClause());
    }

    @Test
    public void appendExclusiveMultiValueClause() throws Exception
    {
        final Clause originalClause = new TerminalClauseImpl("blah", EQUALS, "blah");
        final QueryImpl originalQuery = new QueryImpl(originalClause);
        SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        List<MockUser> values = asList(new MockUser("me"), null);
        final SearchRequest newSearchRequest = userSearchRequestAppender.appendExclusiveMultiValueClause(values, originalSearchRequest);

        final AndClause expectedAndClause = new AndClause(originalClause, new NotClause(new OrClause(
                new TerminalClauseImpl("jqlClauseName", EQUALS, "me"),
                new TerminalClauseImpl("jqlClauseName", IS, EMPTY)
        )));
        assertEquals(expectedAndClause, newSearchRequest.getQuery().getWhereClause());
    }
}