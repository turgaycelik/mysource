package com.atlassian.jira.issue.statistics;

import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.query.operand.EmptyOperand.EMPTY;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.IS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @since v6.0
 */
public class TestLabelsSearchRequestAppender
{
    private LabelsStatisticsMapper.LabelsSearchRequestAppender searchRequestAppender;

    private final Clause originalClause = new TerminalClauseImpl("blah", EQUALS, "blah");
    private final SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, new QueryImpl(originalClause));

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        searchRequestAppender = new LabelsStatisticsMapper.LabelsSearchRequestAppender("labels");
    }

    @Test
    public void appendInclusiveSingleNullClause() throws Exception
    {
        final SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(null, originalSearchRequest);
        final AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                originalClause,
                new TerminalClauseImpl("labels", IS, EMPTY)
        )));
    }

    @Test
    public void appendInclusiveSingleValueClause() throws Exception
    {
        final Label label = new Label(1L, 2L, "somelabel");
        final SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(label, originalSearchRequest);
        final AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                originalClause,
                new TerminalClauseImpl("labels", EQUALS, "somelabel"))
        ));
    }

    @Test
    public void appendExclusiveMultiValueClause() throws Exception
    {
        final List values = asList(new Label(1L, 2L, "somelabel"), null);
        final SearchRequest searchRequest = searchRequestAppender.appendExclusiveMultiValueClause(values, originalSearchRequest);
        assertThat(searchRequest, is(nullValue()));
    }
}