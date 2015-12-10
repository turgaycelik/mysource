package com.atlassian.jira.issue.statistics;

import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.resolution.MockResolution;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.query.Query;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.query.operand.EmptyOperand.EMPTY;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.IS;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * Tests that the {@link AbstractConstantStatisticsMapper} correctly modifies {@link
 * com.atlassian.jira.issue.search.SearchRequest}s to contain the additional clauses required to link to the specific
 * values given.
 *
 * @since v4.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestIssueConstantSearchRequestAppender
{
    private static final String PROJECT_CLAUSE_NAME = "project";
    private static final String ISSUE_FIELD_CONSTANT = "myResolution";

    private final IssueConstantSearchRequestAppender mapper = new IssueConstantSearchRequestAppender(ISSUE_FIELD_CONSTANT);

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void appendInclusiveSingleValueClause() throws Exception
    {
        Query originalQuery = JqlQueryBuilder.newBuilder().where().addNumberCondition(PROJECT_CLAUSE_NAME, 88L).buildQuery();
        SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        MockResolution value = new MockResolution("123", "fixed");

        final SearchRequest newSearchRequest = mapper.appendInclusiveSingleValueClause(value, originalSearchRequest);

        AndClause andClause = (AndClause) newSearchRequest.getQuery().getWhereClause();
        final List<?> expectedClauses = asList(
                new TerminalClauseImpl(PROJECT_CLAUSE_NAME, EQUALS, 88L),
                new TerminalClauseImpl(ISSUE_FIELD_CONSTANT, EQUALS, "fixed"));

        assertEquals(expectedClauses, andClause.getClauses());
    }

    @Test
    public void appendInclusiveNullValueClause() throws Exception
    {
        Query originalQuery = JqlQueryBuilder.newBuilder().where().project(88L).buildQuery();
        SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final SearchRequest newSearchRequest = mapper.appendInclusiveSingleValueClause(null, originalSearchRequest);

        AndClause and = (AndClause) newSearchRequest.getQuery().getWhereClause();
        final List<?> expectedClauses = asList(
                new TerminalClauseImpl(PROJECT_CLAUSE_NAME, EQUALS, 88L),
                new TerminalClauseImpl(ISSUE_FIELD_CONSTANT, IS, EMPTY));

        assertEquals(expectedClauses, and.getClauses());
    }

    @Test
    public void appendExclusiveMultiValueClause() throws Exception
    {
        Query originalQuery = JqlQueryBuilder.newBuilder().where().project(88L).buildQuery();
        SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        List<MockResolution> values = asList(new MockResolution("123", "fixed"), null);

        final SearchRequest newSearchRequest = mapper.appendExclusiveMultiValueClause(values, originalSearchRequest);

        AndClause and = (AndClause) newSearchRequest.getQuery().getWhereClause();
        final List<?> expectedClauses = asList(
                new TerminalClauseImpl(PROJECT_CLAUSE_NAME, EQUALS, 88L),
                new NotClause(new OrClause(
                        new TerminalClauseImpl(ISSUE_FIELD_CONSTANT, EQUALS, "fixed"),
                        new TerminalClauseImpl(ISSUE_FIELD_CONSTANT, IS, EMPTY)
                ))
        );

        assertEquals(expectedClauses, and.getClauses());
    }
}