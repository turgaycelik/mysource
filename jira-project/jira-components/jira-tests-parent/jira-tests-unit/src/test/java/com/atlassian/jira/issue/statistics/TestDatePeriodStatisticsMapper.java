package com.atlassian.jira.issue.statistics;

import java.util.Date;
import java.util.List;
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
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;

import org.jfree.data.time.Month;
import org.jfree.data.time.TimePeriod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_TYPE;
import static com.atlassian.jira.issue.IssueFieldConstants.PROJECT;
import static com.atlassian.jira.issue.IssueFieldConstants.RESOLUTION_DATE;
import static com.atlassian.jira.issue.index.DocumentConstants.ISSUE_RESOLUTION_DATE;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.GREATER_THAN_EQUALS;
import static com.atlassian.query.operator.Operator.LESS_THAN_EQUALS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test of {@link DatePeriodStatisticsMapper}.
 *
 * @since 6.2
 */
public class TestDatePeriodStatisticsMapper
{
    private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();

    private TimeZoneManager timeZoneManager;

    @Before
    public void setUp()
    {
        timeZoneManager = new TimeZoneManager()
        {
            @Override
            public TimeZone getLoggedInUserTimeZone()
            {
                return DEFAULT_TIME_ZONE;
            }
            @Override
            public TimeZone getTimeZoneforUser(User user)
            {
                return DEFAULT_TIME_ZONE;
            }

            @Override
            public TimeZone getDefaultTimezone()
            {
                return DEFAULT_TIME_ZONE;
            }
        };
        new MockComponentWorker().init().addMock(JqlClauseBuilderFactory.class,
                new JqlClauseBuilderFactoryImpl(new JqlDateSupportImpl(timeZoneManager)));
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testGetUrlSuffixWithClauses() throws Exception
    {
        // Set up
        final TimePeriod value = new Month(3, 2008);
        final TerminalClauseImpl projectClause = new TerminalClauseImpl(PROJECT, EQUALS, 13L);
        final TerminalClauseImpl issueTypeClause = new TerminalClauseImpl(ISSUE_TYPE, EQUALS, "Bug");
        final AndClause totalExistingClauses = new AndClause(projectClause, issueTypeClause);
        final Query query = new QueryImpl(totalExistingClauses);
        final SearchRequest sr = new MockJqlSearchRequest(10000L, query);
        final DatePeriodStatisticsMapper mapper = new DatePeriodStatisticsMapper(null, ISSUE_RESOLUTION_DATE, DEFAULT_TIME_ZONE);

        // Invoke
        @SuppressWarnings("deprecation")
        final SearchRequest urlSuffix = mapper.getSearchUrlSuffix(value, sr);

        // Check
        final List<Clause> modifiedClauses = urlSuffix.getQuery().getWhereClause().getClauses();
        assertEquals(2, modifiedClauses.size());
        final JqlDateSupportImpl support = new JqlDateSupportImpl(timeZoneManager);
        final Date expectedStart = value.getStart();
        final TerminalClauseImpl afterClause = new TerminalClauseImpl(RESOLUTION_DATE, GREATER_THAN_EQUALS, support.getDateString(expectedStart));
        final Date expectedEnd = value.getEnd();
        final TerminalClauseImpl beforeClause = new TerminalClauseImpl(RESOLUTION_DATE, LESS_THAN_EQUALS, support.getDateString(expectedEnd));
        final AndClause totalExpectedClauses = new AndClause(afterClause, beforeClause);
        assertTrue(modifiedClauses.contains(totalExistingClauses));
        assertTrue(modifiedClauses.contains(totalExpectedClauses));
    }
}
