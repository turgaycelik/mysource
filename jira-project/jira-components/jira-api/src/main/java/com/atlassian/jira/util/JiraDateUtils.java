/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class JiraDateUtils
{
    public static final long SECONDS_IN_MILLIS = 1000L;
    public static final long MINUTE_IN_MILLIS = 60 * SECONDS_IN_MILLIS;
    public static final String SECONDS = " seconds";
    private static final String MILLIS = " ms";

    public static String formatTime(long time)
    {
        if (time > MINUTE_IN_MILLIS)
        {
            return formatMinutes(time);
        }
        if (time > SECONDS_IN_MILLIS)
        {
            return formatSeconds(time);
        }
        else
        {
            return formatMillis(time);
        }
    }

    private static String formatMinutes(long time)
    {
        //todo - implement this method.
        return formatSeconds(time);
    }

    private static String formatSeconds(long time)
    {
        long kbsize = Math.round((float) time / SECONDS_IN_MILLIS); //format 0 decimal places
        return String.valueOf(kbsize) + SECONDS;
    }

    private static String formatMillis(long time)
    {
        return time + MILLIS;
    }

    public static Date copyDateNullsafe(Date date)
    {
        return date == null ? null : new Date(date.getTime());
    }

    public static Date copyOrCreateDateNullsafe(Date date)
    {
        return date == null ? new Date() : new Date(date.getTime());
    }

    public static Timestamp copyOrCreateTimestampNullsafe(Date date)
    {
        return date == null ? new Timestamp(System.currentTimeMillis()) : new Timestamp(date.getTime());
    }

    public static boolean hasTimeComponent(Date date, TimeZone timeZone)
    {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(timeZone);
        cal.setTime(date);
        //Forces the calendar to re-calculate
        cal.getTime();

        // only check hours and minutes
        return (cal.get(Calendar.HOUR_OF_DAY) != 0) || (cal.get(Calendar.MINUTE) != 0);
    }
}
