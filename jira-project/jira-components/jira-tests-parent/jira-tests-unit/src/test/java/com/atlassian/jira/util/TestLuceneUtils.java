/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.util;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class TestLuceneUtils
{
    @Test
    public void testRoundTripDates()
    {
        Date in = date(2007, 03, 27, 13, 12, 0);
        String indexValue = LuceneUtils.dateToString(in);
        Date out = LuceneUtils.stringToDate(indexValue);
        assertEquals(in, out);
    }

    static Date date(final int year, final int month, final int date)
    {
        return date(year, month, date, 0, 0, 0);
    }

    static Date date(final int year, final int month, final int date, final int hrs, final int min, final int sec)
    {
        return date(year, month, date, hrs, min, sec, 0);
    }

    static Date date(final int year, final int month, final int date, final int hrs, final int min, final int sec, final int milli)
    {
        return new DateTime(year, month, date, hrs, min, sec, milli, DateTimeZone.UTC).toDate();
    }
}
