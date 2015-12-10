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
public class TestEndOfDayFunction extends AbstractDateFunctionTestCase
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
        assertEquals(now.get(Calendar.MONTH), cal.get(Calendar.MONTH));
        assertEquals(now.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH));
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
        expectedSystemTime.set(Calendar.DAY_OF_MONTH, 11);
        expectedSystemTime.set(Calendar.HOUR_OF_DAY, 7);
        expectedSystemTime.set(Calendar.MINUTE, 59);
        expectedSystemTime.set(Calendar.SECOND, 59);

        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(userTimeZone);

        GregorianCalendar systemTime = new GregorianCalendar(systemTimeZone);
        systemTime.set(Calendar.YEAR, 2011);
        systemTime.set(Calendar.MONTH, 3);
        systemTime.set(Calendar.DAY_OF_MONTH, 11);
        systemTime.set(Calendar.HOUR_OF_DAY, 4);
        systemTime.set(Calendar.MINUTE, 12);
        systemTime.set(Calendar.SECOND, 12);

        doTest(expectedSystemTime, systemTime, systemTimeZone);
    }

    @Test
    public void testknowDates() throws Exception
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        // Remember this is stupid Java so MONTHS are ZERO based (but day and year are not)
        doTest(new GregorianCalendar(2001, 6, 25, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(1900, 0, 1, 23, 59, 59), new GregorianCalendar(1900, 0, 1, 0, 0, 0), TimeZone.getDefault());
        doTest(new GregorianCalendar(2004, 1, 29, 23, 59, 59), new GregorianCalendar(2004, 1, 29, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(2004, 1, 28, 23, 59, 59), new GregorianCalendar(2004, 1, 28, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(2099, 6, 25, 23, 59, 59), new GregorianCalendar(2099, 6, 25, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(2010, 11, 31, 23, 59, 59), new GregorianCalendar(2010, 11, 31, 23, 59, 59), TimeZone.getDefault());
        doTest(new GregorianCalendar(2012, 2, 1, 23, 59, 59), new GregorianCalendar(2012, 2, 1, 0, 0, 0), TimeZone.getDefault());
        
        doTestwithIncrement("-1", new GregorianCalendar(2001, 6, 24, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("1", new GregorianCalendar(2001, 6, 26, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+1d", new GregorianCalendar(2001, 6, 26, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+10d", new GregorianCalendar(2001, 7, 4, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));

        doTestwithIncrement("+3y", new GregorianCalendar(2004, 6, 25, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+3M", new GregorianCalendar(2001,  9, 25, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+3w", new GregorianCalendar(2001, 7, 15, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+3d", new GregorianCalendar(2001, 6, 28, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+3h", new GregorianCalendar(2001, 6, 26, 2, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+3m", new GregorianCalendar(2001, 6, 26, 0, 2, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));

        doTestwithIncrement("-3y", new GregorianCalendar(1998, 6, 25, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3M", new GregorianCalendar(2001, 3, 25, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3w", new GregorianCalendar(2001, 6, 4, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3d", new GregorianCalendar(2001, 6, 22, 23, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3h", new GregorianCalendar(2001, 6, 25, 20, 59, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3m", new GregorianCalendar(2001, 6, 25, 23, 56, 59), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
    }
    
    @Override
    String getFunctionName() 
    {
        return EndOfDayFunction.FUNCTION_END_OF_DAY;
    }

    @Override
    AbstractDateFunction getInstanceToTest() 
    {
        return getInstanceToTest(new ConstantClock(new Date()));
    }

    AbstractDateFunction getInstanceToTest(Clock clock) 
    {
        return new EndOfDayFunction(clock, timeZoneManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
    }
}
