/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.customfields.statistics;

import java.util.List;

import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_TYPE;
import static com.atlassian.query.operand.EmptyOperand.EMPTY;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.IS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith (MockitoJUnitRunner.class)
public class TestSelectOptionSearchRequestAppender
{
    private final Clause issueTypeClause = new TerminalClauseImpl(ISSUE_TYPE, EQUALS, "Bug");
    private final SearchRequest baseSearchRequest = new MockJqlSearchRequest(10000L, new QueryImpl(issueTypeClause));

    private final SelectStatisticsMapper.SelectOptionSearchRequestAppender searchRequestAppender = new SelectStatisticsMapper.SelectOptionSearchRequestAppender("cf[10001]");

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void appendInclusiveSingleNullClause() throws Exception
    {
        final SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(null, baseSearchRequest);
        final AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new TerminalClauseImpl("cf[10001]", IS, EMPTY)
        )));
    }

    @Test
    public void appendInclusiveSingleValueClause() throws Exception
    {
        Option option = new MockOption(null, null, 1L, "value", null, 1L);
        final SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(option, baseSearchRequest);
        final AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new TerminalClauseImpl("cf[10001]", EQUALS, "value")
        )));
    }

    @Test
    public void appendExclusiveMultiValueClause() throws Exception
    {
        List values = asList(new MockOption(null, null, 1L, "value", null, 1L), null);
        final SearchRequest searchRequest = searchRequestAppender.appendExclusiveMultiValueClause(values, baseSearchRequest);
        final AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new NotClause(
                        new OrClause(
                                new TerminalClauseImpl("cf[10001]", EQUALS, "value"),
                                new TerminalClauseImpl("cf[10001]", IS, EMPTY)
                        )
                )
        )));
    }
}