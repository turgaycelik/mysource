package com.atlassian.jira.issue.statistics.util;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.query.Query;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @since v6.0
 */
public class TestSearchRequestAddendumBuilder
{
    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void appendSingleNullValueAndClauseToExistingQuery()
    {
        final Query originalQuery = JqlQueryBuilder.newBuilder().where().addNumberCondition("SomeCondition", 88L).buildQuery();
        final SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final SearchRequest newSearchRequest = SearchRequestAddendumBuilder.appendAndClause(null, originalSearchRequest, new ExampleCallback());

        assertThat(newSearchRequest.getQuery().toString(), is("{SomeCondition = 88} AND {myclause is EMPTY}"));
    }

    @Test
    public void appendSingleNonNullValueAndClauseToExistingQuery()
    {
        final Query originalQuery = JqlQueryBuilder.newBuilder().where().addNumberCondition("SomeCondition", 88L).buildQuery();
        final SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final SearchRequest newSearchRequest = SearchRequestAddendumBuilder.appendAndClause("myvalue", originalSearchRequest, new ExampleCallback());

        assertThat(newSearchRequest.getQuery().toString(), is("{SomeCondition = 88} AND {myclause = \"myvalue\"}"));
    }

    @Test
    public void appendMultiValueAndNotClauseToExistingQuery()
    {
        final Query originalQuery = JqlQueryBuilder.newBuilder().where().addNumberCondition("SomeCondition", 88L).buildQuery();
        final SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final List<String> values = Arrays.asList("value1", "value2", null);
        final SearchRequest newSearchRequest = SearchRequestAddendumBuilder.appendAndNotClauses(values, originalSearchRequest, new ExampleCallback());

        assertThat(newSearchRequest.getQuery().toString(), is("{SomeCondition = 88} AND NOT ( {myclause = \"value1\"} OR {myclause = \"value2\"} OR {myclause is EMPTY} )"));
    }

    private class ExampleCallback implements SearchRequestAddendumBuilder.AddendumCallback<String>
    {
        @Override
        public void appendNonNullItem(String value, JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addCondition("myclause", Operator.EQUALS, new SingleValueOperand(value));
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addCondition("myclause", Operator.IS, EmptyOperand.EMPTY);
        }
    }
}