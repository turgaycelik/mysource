package com.atlassian.jira.issue.statistics;

import java.util.Date;
import java.util.TimeZone;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactory;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactoryImpl;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;

import org.jfree.data.time.Month;
import org.jfree.data.time.TimePeriod;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_TYPE;
import static com.atlassian.jira.issue.IssueFieldConstants.RESOLUTION_DATE;
import static com.atlassian.jira.issue.index.DocumentConstants.ISSUE_RESOLUTION_DATE;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.GREATER_THAN_EQUALS;
import static com.atlassian.query.operator.Operator.LESS_THAN_EQUALS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @since v4.0
 */
public class TestDatePeriodSearchRequestAppender
{

    private static final TimePeriod MARCH_2008 = new Month(3, 2008);
    private static final JqlDateSupportImpl jqlDateSupport = new JqlDateSupportImpl(new TestTimeZoneManager());
    private static final TerminalClauseImpl issueTypeClause = new TerminalClauseImpl(ISSUE_TYPE, EQUALS, "Bug");

    private final DatePeriodStatisticsMapper.TimePeriodSearchRequestAppender searchRequestAppender = new DatePeriodStatisticsMapper.TimePeriodSearchRequestAppender(ISSUE_RESOLUTION_DATE);

    @Before
    public void setUp() throws Exception
    {
        final MockComponentWorker mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.registerMock(JqlClauseBuilderFactory.class, new JqlClauseBuilderFactoryImpl(new JqlDateSupportImpl(new TestTimeZoneManager())));
        ComponentAccessor.initialiseWorker(mockComponentWorker);
    }

    @Test
    public void appendInclusiveSingleValueClause() throws Exception
    {
        final SearchRequest urlSuffix = searchRequestAppender.appendInclusiveSingleValueClause(
                MARCH_2008,
                searchRequest(issueTypeClause)
        );

        final AndClause whereClause = (AndClause) urlSuffix.getQuery().getWhereClause();

        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new AndClause(
                        new TerminalClauseImpl(RESOLUTION_DATE, GREATER_THAN_EQUALS, dateStr(MARCH_2008.getStart())),
                        new TerminalClauseImpl(RESOLUTION_DATE, LESS_THAN_EQUALS, dateStr(MARCH_2008.getEnd()))
                )
        )));
    }

    @Test
    public void appendInclusiveSingleNullClause() throws Exception
    {
        final SearchRequest urlSuffix = searchRequestAppender.appendInclusiveSingleValueClause(
                null,
                searchRequest(issueTypeClause)
        );

        final Clause whereClause = urlSuffix.getQuery().getWhereClause();

        assertThat(whereClause, is((Clause) issueTypeClause));
    }

    private static String dateStr(Date date)
    {
        return jqlDateSupport.getDateString(date);
    }

    private static SearchRequest searchRequest(Clause clause)
    {
        return new MockJqlSearchRequest(10000L, new QueryImpl(clause));
    }

    private static class TestTimeZoneManager implements TimeZoneManager
    {
        @Override
        public TimeZone getLoggedInUserTimeZone()
        {
            return TimeZone.getDefault();
        }

        @Override
        public TimeZone getTimeZoneforUser(User user)
        {
            return TimeZone.getDefault();
        }

        @Override
        public TimeZone getDefaultTimezone()
        {
            return TimeZone.getDefault();
        }
    }
}