package com.atlassian.jira.plugin.jql.function;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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
public class TestEndOfYearFunction extends AbstractDateFunctionTestCase
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
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(value.get(0).getLongValue().longValue());
        assertEquals(now.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals(11, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
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
        expectedSystemTime.set(Calendar.MONTH, 0);
        expectedSystemTime.set(Calendar.DAY_OF_MONTH, 1);
        expectedSystemTime.set(Calendar.HOUR_OF_DAY, 9);
        expectedSystemTime.set(Calendar.MINUTE, 59);
        expectedSystemTime.set(Calendar.SECOND, 59);
        expectedSystemTime.getTime();

        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(userTimeZone);

        GregorianCalendar systemTime = new GregorianCalendar(systemTimeZone);
        systemTime.set(Calendar.YEAR, 2011);
        systemTime.set(Calendar.MONTH, 0);
        systemTime.set(Calendar.DAY_OF_MONTH, 1);
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
        // Remember this is stupid Java so MONTHS are ZERO based (but day and year are not)
        doTest(new GregorianCalendar(2001, 11, 31, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(1901, 11, 31, 23, 59, 59), new GregorianCalendar(1901, 0, 1, 0, 0, 0), TimeZone.getDefault());
        doTest(new GregorianCalendar(2004, 11, 31, 23, 59, 59), new GregorianCalendar(2004, 1, 29, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(2004, 11, 31, 23, 59, 59), new GregorianCalendar(2004, 1, 28, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(2099, 11, 31, 23, 59, 59), new GregorianCalendar(2099, 6, 25, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(2010, 11, 31, 23, 59, 59), new GregorianCalendar(2010, 11, 31, 23, 59, 59), TimeZone.getDefault());
        doTest(new GregorianCalendar(2012, 11, 31, 23, 59, 59), new GregorianCalendar(2012, 2, 1, 23, 59, 59), TimeZone.getDefault());

        doTestwithIncrement("0", new GregorianCalendar(2001, 11, 31, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("1", new GregorianCalendar(2002, 11, 31, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+1y", new GregorianCalendar(2002, 11, 31, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3", new GregorianCalendar(1998, 11, 31, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-4y", new GregorianCalendar(1997, 11, 31, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));

        doTestwithIncrement("1M", new GregorianCalendar(2002, 0, 31, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-1M", new GregorianCalendar(2001, 10, 30, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));

        doTestwithIncrement("3M", new GregorianCalendar(2002, 2, 31, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("3w", new GregorianCalendar(2002, 0, 21, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("3d", new GregorianCalendar(2002, 0, 3, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("3h", new GregorianCalendar(2002, 0, 1, 2, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("3m", new GregorianCalendar(2002, 0, 1, 0, 2, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));

        doTestwithIncrement("-3M", new GregorianCalendar(2001, 8, 30, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3w", new GregorianCalendar(2001, 11, 10, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3d", new GregorianCalendar(2001, 11, 28, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3h", new GregorianCalendar(2001, 11, 31, 20, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3m", new GregorianCalendar(2001, 11, 31, 23, 56, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
    }
    
    @Override
    String getFunctionName() 
    {
        return EndOfYearFunction.FUNCTION_END_OF_YEAR;
    }

    @Override
    AbstractDateFunction getInstanceToTest() 
    {
        return getInstanceToTest(new ConstantClock(new Date()));
    }

    AbstractDateFunction getInstanceToTest(Clock clock) 
    {
        return new EndOfYearFunction(clock, timeZoneManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
    }
}
