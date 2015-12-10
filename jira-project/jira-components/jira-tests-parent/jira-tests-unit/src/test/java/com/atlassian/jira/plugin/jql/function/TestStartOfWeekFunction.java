package com.atlassian.jira.plugin.jql.function;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * @since v4.3
 */
public class TestStartOfWeekFunction extends AbstractDateFunctionTestCase
{
    private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Australia/Sydney");

    private TerminalClause terminalClause = null;

    @Before
    public void setUp()
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TIMEZONE);
    }

    @Test
    public void testGetValues() throws Exception
    {
        FunctionOperand function = new FunctionOperand(getFunctionName(), Collections.<String>emptyList());
        final List<QueryLiteral> value = getInstanceToTest().getValues(null, function, terminalClause);
        assertNotNull(value);
        assertEquals(1, value.size());

        GregorianCalendar now = new GregorianCalendar();
        now.set(Calendar.DAY_OF_WEEK, now.getFirstDayOfWeek());
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(value.get(0).getLongValue().longValue());
        assertEquals(now.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals(now.get(Calendar.MONTH), cal.get(Calendar.MONTH));
        assertEquals(now.get(Calendar.WEEK_OF_MONTH), cal.get(Calendar.WEEK_OF_MONTH));
        assertEquals(new GregorianCalendar().getFirstDayOfWeek(), cal.get(Calendar.DAY_OF_WEEK));
        assertEquals(now.getActualMinimum(0), cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(now.getActualMinimum(0), cal.get(Calendar.MINUTE));
        assertEquals(now.getActualMinimum(0), cal.get(Calendar.SECOND));

        assertEquals(function, value.get(0).getSourceOperand());
    }

    @Test
    public void testWithUserTimeZone() throws Exception
    {
        TimeZone userTimeZone = TimeZone.getTimeZone("Europe/Berlin");
        TimeZone systemTimeZone = TimeZone.getTimeZone("Australia/Sydney");

        GregorianCalendar expectedSystemTime = new GregorianCalendar(systemTimeZone);

        expectedSystemTime.set(Calendar.YEAR, 2011);
        expectedSystemTime.set(Calendar.MONTH, 2);
        expectedSystemTime.set(Calendar.DAY_OF_MONTH, 27);
        expectedSystemTime.set(Calendar.HOUR_OF_DAY, 10);
        expectedSystemTime.set(Calendar.MINUTE, 0);
        expectedSystemTime.set(Calendar.SECOND, 0);
        expectedSystemTime.getTime();

        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(userTimeZone);

        GregorianCalendar systemTime = new GregorianCalendar(systemTimeZone);
        systemTime.set(Calendar.YEAR, 2011);
        systemTime.set(Calendar.MONTH, 3);
        systemTime.set(Calendar.DAY_OF_MONTH, 3);
        systemTime.set(Calendar.HOUR_OF_DAY, 4);
        systemTime.set(Calendar.MINUTE, 12);
        systemTime.set(Calendar.SECOND, 12);
        systemTime.getTime();

        doTest(expectedSystemTime, systemTime, systemTimeZone);
    }

    @Test
    public void startOfWeekWithNoIncrementWhenWeekStartsOnMonday() throws Exception
    {
        runTestsWhenFirstDayOfWeekIsOnMonday(Arrays.asList(
            // Test Known wednesday to tuesday
            scenario().currentDate(new GregorianCalendar(2010, 4, 12, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 10, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 13, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 10, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 14, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 10, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 15, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 10, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 16, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 10, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 17, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 17, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 18, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 17, 0, 0, 0)).build(),
            // Some other days
            scenario().currentDate(new GregorianCalendar(2001, 6, 25, 12, 30, 20)).expectedDate(new GregorianCalendar(2001, 6, 23, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(1900, 0, 1, 0, 0, 0)).expectedDate(new GregorianCalendar(1900, 0, 1, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2004, 1, 29, 12, 30, 20)).expectedDate(new GregorianCalendar(2004, 1, 23, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2004, 1, 28, 12, 30, 20)).expectedDate(new GregorianCalendar(2004, 1, 23, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2099, 6, 25, 12, 30, 20)).expectedDate(new GregorianCalendar(2099, 6, 20, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 11, 27, 0, 0, 0)).expectedDate(new GregorianCalendar(2010, 11, 27, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 9, 1, 0, 0, 0)).expectedDate(new GregorianCalendar(2010, 8, 27, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2012, 2, 1, 0, 0, 0)).expectedDate(new GregorianCalendar(2012, 1, 27, 0, 0, 0)).build()
        ));
    }

    @Test
    public void startOfWeekWithNoIncrementWhenWeekStartsOnSunday() throws Exception
    {
        runTestsWhenFirstDayOfWeekIsOnSunday(Arrays.asList(
            // Test Known wednesday to tuesday
            scenario().currentDate(new GregorianCalendar(2010, 4, 12, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 9, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 13, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 9, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 14, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 9, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 15, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 9, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 16, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 16, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 17, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 16, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 4, 18, 12, 30, 20)).expectedDate(new GregorianCalendar(2010, 4, 16, 0, 0, 0)).build(),
            // Some other days
            scenario().currentDate(new GregorianCalendar(2001, 6, 25, 12, 30, 20)).expectedDate(new GregorianCalendar(2001, 6, 22, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(1900, 0, 1, 0, 0, 0)).expectedDate(new GregorianCalendar(1899, 11, 31, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2004, 1, 29, 12, 30, 20)).expectedDate(new GregorianCalendar(2004, 1, 29, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2004, 1, 28, 12, 30, 20)).expectedDate(new GregorianCalendar(2004, 1, 22, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2099, 6, 25, 12, 30, 20)).expectedDate(new GregorianCalendar(2099, 6, 19, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 11, 31, 23, 59, 59)).expectedDate(new GregorianCalendar(2010, 11, 26, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2010, 9, 1, 0, 0, 0)).expectedDate(new GregorianCalendar(2010, 8, 26, 0, 0, 0)).build(),
            scenario().currentDate(new GregorianCalendar(2012, 2, 1, 0, 0, 0)).expectedDate(new GregorianCalendar(2012, 1, 26, 0, 0, 0)).build()
        ));
    }

    @Test
    public void startOfWeekInOneMonthWhenWeekStartsOnMonday() throws Exception
    {
        runTestsWhenFirstDayOfWeekIsOnMonday(Arrays.asList(
            scenario().currentDate(new GregorianCalendar(2014, 11, 15)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 12)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 16)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 12)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 17)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 12)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 18)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 12)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 19)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 19)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 20)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 19)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 21)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 19)).build()
        ));
    }

    @Test
    public void startOfWeekWithInOneMonthWhenWeekStartsOnSunday() throws Exception
    {
        runTestsWhenFirstDayOfWeekIsOnSunday(Arrays.asList(
            scenario().currentDate(new GregorianCalendar(2014, 11, 15)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 11)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 16)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 11)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 17)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 11)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 18)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 18)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 19)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 18)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 20)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 18)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 21)).increment("+1M").expectedDate(new GregorianCalendar(2015, 0, 18)).build()
        ));
    }

    @Test
    public void startOfWeekOneMonthAgoWhenWeekStartsOnMonday() throws Exception
    {
        runTestsWhenFirstDayOfWeekIsOnMonday(Arrays.asList(
            scenario().currentDate(new GregorianCalendar(2014, 11, 15)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 10)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 16)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 10)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 17)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 17)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 18)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 17)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 19)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 17)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 20)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 17)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 21)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 17)).build()
        ));
    }

    @Test
    public void startOfWeekOneMonthAgoWhenWeekStartsOnSunday() throws Exception
    {
        runTestsWhenFirstDayOfWeekIsOnSunday(Arrays.asList(
            scenario().currentDate(new GregorianCalendar(2014, 11, 15)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 9)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 16)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 16)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 17)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 16)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 18)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 16)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 19)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 16)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 20)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 16)).build(),
            scenario().currentDate(new GregorianCalendar(2014, 11, 21)).increment("-1M").expectedDate(new GregorianCalendar(2014, 10, 16)).build()
        ));
    }

    private void runTestsWhenFirstDayOfWeekIsOnMonday(Collection<StartOfWeekTestScenario> scenarios) throws Exception
    {
        runTestsWithLocale("fr", "FR", scenarios);
    }

    private void runTestsWhenFirstDayOfWeekIsOnSunday(Collection<StartOfWeekTestScenario> scenarios) throws Exception
    {
        runTestsWithLocale("en", "US", scenarios);
    }

    private void runTestsWithLocale(String language, String country, Collection<StartOfWeekTestScenario> scenarios) throws Exception
    {
        Locale originalLocale = Locale.getDefault();
        try
        {
            Locale.setDefault(new Locale(language, country));

            for (StartOfWeekTestScenario scenario : scenarios)
            {
                if (scenario.increment != null)
                {
                    doTestwithIncrement(scenario.increment, scenario.expected, scenario.now);
                }
                else
                {
                    doTest(scenario.expected, scenario.now, scenario.timeZone);
                }
            }
        }
        finally
        {
            Locale.setDefault(originalLocale);
        }
    }

    @Override
    String getFunctionName()
    {
        return StartOfWeekFunction.FUNCTION_START_OF_WEEK;
    }

    @Override
    AbstractDateFunction getInstanceToTest()
    {
        return getInstanceToTest(new ConstantClock(new Date()));
    }

    AbstractDateFunction getInstanceToTest(Clock clock)
    {
        return new StartOfWeekFunction(clock, timeZoneManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
    }

    private StartOfWeekScenarioBuilder scenario()
    {
        return new StartOfWeekScenarioBuilder();
    }

    private static class StartOfWeekScenarioBuilder
    {
        private Calendar expected;
        private Calendar now;
        private TimeZone timeZone = TIMEZONE;
        private String increment;

        public StartOfWeekScenarioBuilder expectedDate(Calendar expected)
        {
            this.expected = expected;
            return this;
        }

        public StartOfWeekScenarioBuilder currentDate(Calendar now)
        {
            this.now = now;
            return this;
        }

        public StartOfWeekScenarioBuilder increment(String increment)
        {
            this.increment = increment;
            return this;
        }

        public StartOfWeekTestScenario build()
        {
            StartOfWeekTestScenario scenario = new StartOfWeekTestScenario();
            scenario.expected = this.expected;
            scenario.now = this.now;
            scenario.timeZone = this.timeZone;
            scenario.increment = increment;
            return scenario;
        }
    }

    private static class StartOfWeekTestScenario
    {
        public Calendar expected;
        public Calendar now;
        public TimeZone timeZone;
        public String increment;
    }
}
