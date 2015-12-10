package com.atlassian.jira.jql.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 */
public class DateRangeTest
{
    @Test
    public void testConstruction() throws Exception
    {
        try
        {
            new DateRange(null, new Date());
            fail("Bad lower");
        }
        catch (IllegalArgumentException expected)
        {
        }
        try
        {
            new DateRange(new Date(), null);
            fail("Bad upper");
        }
        catch (IllegalArgumentException expected)
        {
        }

    }

    @Test
    public void testReordering() throws Exception
    {

        Date lower = createDate(2010, 8, 8, 0, 0, 0);
        Date same = createDate(2010, 8, 8, 0, 0, 0);
        Date upper = createDate(2011, 8, 8, 0, 0, 0);

        DateRange dateRange = new DateRange(lower, upper);

        assertEquals(lower, dateRange.getLowerDate());
        assertEquals(upper, dateRange.getUpperDate());

        dateRange = new DateRange(upper, lower);

        assertEquals(lower, dateRange.getLowerDate());
        assertEquals(upper, dateRange.getUpperDate());

        dateRange = new DateRange(lower, same);

        assertEquals(lower, dateRange.getLowerDate());
        assertEquals(same, dateRange.getLowerDate());
        assertEquals(lower, dateRange.getUpperDate());
        assertEquals(same, dateRange.getUpperDate());

    }

    private static Date createDate(int year, int month, int day, int hour, int minute, int second)
    {
        return createDateInTimeZone(year, month, day, hour, minute, second, TimeZone.getDefault());
    }

    private static Date createDateInTimeZone(int year, int month, int day, int hour, int minute, int second, TimeZone timeZone)
    {
        final Calendar expectedCal = Calendar.getInstance();
        expectedCal.setTimeZone(timeZone);
        expectedCal.set(year, month - 1, day, hour, minute, second);
        expectedCal.set(Calendar.MILLISECOND, 0);
        return expectedCal.getTime();
    }

}
