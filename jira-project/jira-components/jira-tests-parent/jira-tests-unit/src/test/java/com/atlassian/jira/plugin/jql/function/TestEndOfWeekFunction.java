package com.atlassian.jira.plugin.jql.function;

import java.util.Calendar;
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * @since v4.3
 */
public class TestEndOfWeekFunction extends AbstractDateFunctionTestCase
{
    private TerminalClause terminalClause = null;

    @Test
    public void testGetValues() throws Exception
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        FunctionOperand function = new FunctionOperand(getFunctionName(), Collections.<String>emptyList());
        final List<QueryLiteral> value = getInstanceToTest().getValues(null, function, terminalClause);
        assertNotNull(value);
        assertEquals(1, value.size());
        
        GregorianCalendar now = new GregorianCalendar();
        if (Calendar.MONDAY == new GregorianCalendar().getFirstDayOfWeek())
        {
            now.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        }
        else
        {
            now.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        }

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(value.get(0).getLongValue().longValue());
        assertEquals(now.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals(now.get(Calendar.MONTH), cal.get(Calendar.MONTH));
        assertEquals(now.get(Calendar.WEEK_OF_MONTH), cal.get(Calendar.WEEK_OF_MONTH));
        if (Calendar.MONDAY == new GregorianCalendar().getFirstDayOfWeek())
        {
            assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
        }
        else
        {
            assertEquals(Calendar.SATURDAY, cal.get(Calendar.DAY_OF_WEEK));
        }
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, cal.get(Calendar.MINUTE));
        assertEquals(59, cal.get(Calendar.SECOND));
        
        assertEquals(function, value.get(0).getSourceOperand());
    }

    @Test
    public void testWithUserTimeZone() throws Exception
    {
        TimeZone userTimeZone = TimeZone.getTimeZone("Europe/Berlin");
        TimeZone systemTimeZone = TimeZone.getTimeZone("Australia/Sydney");

        GregorianCalendar expectedSystemTime = new GregorianCalendar(systemTimeZone);

        expectedSystemTime.set(Calendar.YEAR, 2011);
        expectedSystemTime.set(Calendar.MONTH, 3);
        expectedSystemTime.set(Calendar.DAY_OF_MONTH, 3);
        expectedSystemTime.set(Calendar.HOUR_OF_DAY, 7);
        expectedSystemTime.set(Calendar.MINUTE, 59);
        expectedSystemTime.set(Calendar.SECOND, 59);
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
    public void testknowDates() throws Exception
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        Locale saveLocale = Locale.getDefault();
        try {
            // First day is Monday
            Locale.setDefault(new Locale("fr", "FR")); 
            assertEquals(Calendar.MONDAY, new GregorianCalendar().getFirstDayOfWeek());
            // Test Known wednesday to tuesday
            doTest(new GregorianCalendar(2010, 4, 16, 23, 59, 59), new GregorianCalendar(2010, 4, 12, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 16, 23, 59, 59), new GregorianCalendar(2010, 4, 13, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 16, 23, 59, 59), new GregorianCalendar(2010, 4, 14, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 16, 23, 59, 59), new GregorianCalendar(2010, 4, 15, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 16, 23, 59, 59), new GregorianCalendar(2010, 4, 16, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 23, 23, 59, 59), new GregorianCalendar(2010, 4, 17, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 23, 23, 59, 59), new GregorianCalendar(2010, 4, 18, 12, 30, 20), TimeZone.getDefault());
            // Some other days
            doTest(new GregorianCalendar(2001, 6, 29, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(1900, 0, 7, 23, 59, 59), new GregorianCalendar(1900, 0, 1, 23, 59, 59), TimeZone.getDefault());
            doTest(new GregorianCalendar(2004, 1, 29, 23, 59, 59), new GregorianCalendar(2004, 1, 29, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2004, 1, 29, 23, 59, 59), new GregorianCalendar(2004, 1, 28, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2099, 6, 26, 23, 59, 59), new GregorianCalendar(2099, 6, 25, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2011, 0, 2, 23, 59, 59), new GregorianCalendar(2010, 11, 31, 23, 59, 59), TimeZone.getDefault());
            doTest(new GregorianCalendar(2012, 2, 4, 23, 59, 59), new GregorianCalendar(2012, 2, 1, 23, 59, 59), TimeZone.getDefault());

            // First day is Sunday
            Locale.setDefault(new Locale("en", "US")); 
            
            // Test Known wednesday to tuesday
            doTest(new GregorianCalendar(2010, 4, 15, 23, 59, 59), new GregorianCalendar(2010, 4, 12, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 15, 23, 59, 59), new GregorianCalendar(2010, 4, 13, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 15, 23, 59, 59), new GregorianCalendar(2010, 4, 14, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 15, 23, 59, 59), new GregorianCalendar(2010, 4, 15, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 22, 23, 59, 59), new GregorianCalendar(2010, 4, 16, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 22, 23, 59, 59), new GregorianCalendar(2010, 4, 17, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 22, 23, 59, 59), new GregorianCalendar(2010, 4, 18, 12, 30, 20), TimeZone.getDefault());
            // Some other days
            doTest(new GregorianCalendar(2001, 6, 28, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(1900, 0, 6, 23, 59, 59), new GregorianCalendar(1900, 0, 1, 23, 59, 59), TimeZone.getDefault());
            doTest(new GregorianCalendar(2004, 2, 6, 23, 59, 59), new GregorianCalendar(2004, 1, 29, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2004, 1, 28, 23, 59, 59), new GregorianCalendar(2004, 1, 28, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2099, 6, 25, 23, 59, 59), new GregorianCalendar(2099, 6, 25, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2011, 0, 1, 23, 59, 59), new GregorianCalendar(2010, 11, 31, 23, 59, 59), TimeZone.getDefault());
            doTest(new GregorianCalendar(2012, 2, 3, 23, 59, 59), new GregorianCalendar(2012, 2, 1, 23, 59, 59), TimeZone.getDefault());

            doTestwithIncrement("+1M", new GregorianCalendar(2010, 10, 20, 23, 59, 59), new GregorianCalendar(2010, 9, 18, 12, 30, 20));
        } 
        finally
        {
            Locale.setDefault(saveLocale); 
        }
    }
    
    @Override
    String getFunctionName() 
    {
        return EndOfWeekFunction.FUNCTION_END_OF_WEEK;
    }

    @Override
    AbstractDateFunction getInstanceToTest() 
    {
        return getInstanceToTest(new ConstantClock(new Date()));
    }

    AbstractDateFunction getInstanceToTest(Clock clock) 
    {
        return new EndOfWeekFunction(clock, timeZoneManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
    }
}
