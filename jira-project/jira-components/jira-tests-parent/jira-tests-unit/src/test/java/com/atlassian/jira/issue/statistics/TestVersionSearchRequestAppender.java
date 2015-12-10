package com.atlassian.jira.issue.statistics;

import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.Version;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_TYPE;
import static com.atlassian.query.operand.EmptyOperand.EMPTY;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.IS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests that the {@link VersionStatisticsMapper} correctly modifies {@link SearchRequest}s to contain the additional
 * clauses required to link to the specific values given.
 * <p/>
 *
 * @since v4.0
 */
public class TestVersionSearchRequestAppender
{
    private final VersionStatisticsMapper.VersionSearchRequestAppender searchRequestAppender = new VersionStatisticsMapper.VersionSearchRequestAppender("myVersion");

    private final Clause issueTypeClause = new TerminalClauseImpl(ISSUE_TYPE, EQUALS, "Bug");
    private final SearchRequest baseSearchRequest = new MockJqlSearchRequest(10000L, new QueryImpl(issueTypeClause));
    private final Version version555 = new MockVersion(555L, "New Version 555", new MockProject(13L, "HSP"));

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void appendInclusiveSingleValueClause() throws Exception
    {
        SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(version555, baseSearchRequest);
        AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new AndClause(
                        new TerminalClauseImpl("project", EQUALS, "HSP"),
                        new TerminalClauseImpl("myVersion", EQUALS, "New Version 555")
                )
        )));
    }

    @Test
    public void appendInclusiveSingleNullClause() throws Exception
    {
        final SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(null, baseSearchRequest);
        final AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new TerminalClauseImpl("myVersion", IS, EMPTY)
        )));
    }

    @Test
    public void appendExclusiveMultiValueClause() throws Exception
    {
        List versions = asList(version555, null);

        final SearchRequest searchRequest = searchRequestAppender.appendExclusiveMultiValueClause(versions, baseSearchRequest);
        final AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new NotClause(
                        new OrClause(
                                new AndClause(
                                        new TerminalClauseImpl("project", EQUALS, "HSP"),
                                        new TerminalClauseImpl("myVersion", EQUALS, "New Version 555")
                                ),
                                new TerminalClauseImpl("myVersion", IS, EMPTY)
                        )
                )
        )));
    }
}