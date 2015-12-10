package com.atlassian.jira.functest.framework.util.date;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * <p>Some utility methods for manipulating dates in functional tests.
 *
 * <p>This is often required to make sure that func tests run okay when executed in different time zones.
 *
 * @since v4.1
 */
public class DateUtil
{
    /**
     * <p>Returns a string representing the given date in the format suitable for timestamp fields in the data XML file,
     * for example <code>2008-06-02 09:43:58.788</code>.
     *
     * @param year year
     * @param month month
     * @param day day
     * @param hour hour
     * @param minute minute
     * @return String timestamp representation
     */
    public static String dateAsTimestamp(final int year, final int month, final int day, final int hour, final int minute)
    {
        return new Timestamp(createDate(year, month, day, hour, minute)).toString();
    }

    /**
     * <p>Returns a string representing the given date in milliseconds, for example <code>123456789</code>.
     *
     * @param year year
     * @param month month
     * @param day day
     * @param hour hour
     * @param minute minute
     * @return String millisecond representation 
     */
    public static String dateAsMillis(final int year, final int month, final int day, final int hour, final int minute)
    {
        return createDate(year, month, day, hour, minute).toString();
    }

    private static Long createDate(final int year, final int month, final int day, final int hour, final int minute)
    {
        final Calendar instance = Calendar.getInstance();
        instance.setLenient(true);
        instance.set(year, month, day, hour, minute, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTimeInMillis();
    }

}
