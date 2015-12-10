/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.customfields.statistics;

import java.util.List;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.MockGroup;
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
public class TestGroupSearchRequestAppender
{
    private final Clause issueTypeClause = new TerminalClauseImpl(ISSUE_TYPE, EQUALS, "Bug");
    private final SearchRequest baseSearchRequest = new MockJqlSearchRequest(10000L, new QueryImpl(issueTypeClause));
    private final Group group = new MockGroup("mygroup");

    private final GroupPickerStatisticsMapper.GroupSearchRequestAppender searchRequestAppender = new GroupPickerStatisticsMapper.GroupSearchRequestAppender("myclause");

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void appendInclusiveSingleValueClause()
    {
        SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(group, baseSearchRequest);
        AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new TerminalClauseImpl("myclause", EQUALS, "mygroup")
        )));
    }

    @Test
    public void appendInclusiveSingleNullClause()
    {
        SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(null, baseSearchRequest);
        AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new TerminalClauseImpl("myclause", IS, EMPTY)
        )));
    }

    @Test
    public void appendExclusiveMultiValueClause()
    {
        List values = asList(group, null);
        SearchRequest searchRequest = searchRequestAppender.appendExclusiveMultiValueClause(values, baseSearchRequest);
        AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new NotClause(
                        new OrClause(
                                new TerminalClauseImpl("myclause", EQUALS, "mygroup"),
                                new TerminalClauseImpl("myclause", IS, EMPTY)
                        )
                )
        )));
    }
}