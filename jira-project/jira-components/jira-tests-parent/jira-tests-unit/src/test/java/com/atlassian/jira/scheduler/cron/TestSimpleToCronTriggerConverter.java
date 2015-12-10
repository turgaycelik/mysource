package com.atlassian.jira.scheduler.cron;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class TestSimpleToCronTriggerConverter
{
    private static final long SECONDS = 1000 /* x MILLISECONDS */;
    private static final long MINUTES = 60 * SECONDS;
    private static final long HOURS = 60 * MINUTES;
    private static final long DAYS = 24 * HOURS;
    private static final long MONTHS = 28 * DAYS;
    private static final long YEARS = 12 * MONTHS;

    @Test
    public void testDetermineBaseTimeUnit()
    {
        SimpleToCronTriggerConverter converter = new SimpleToCronTriggerConverter();

        assertEquals(SECONDS, converter.determineBaseTimeUnit(500 /* x MILLISECONDS*/));
        assertEquals(SECONDS, converter.determineBaseTimeUnit(20 * SECONDS));
        assertEquals(MINUTES, converter.determineBaseTimeUnit(40 * SECONDS));
        assertEquals(MINUTES, converter.determineBaseTimeUnit(25 * MINUTES));
        assertEquals(MINUTES, converter.determineBaseTimeUnit(30 * MINUTES));
        assertEquals(HOURS, converter.determineBaseTimeUnit(45 * MINUTES));
        assertEquals(HOURS, converter.determineBaseTimeUnit(11 * HOURS));
        assertEquals(DAYS, converter.determineBaseTimeUnit(14 * HOURS));
        assertEquals(DAYS, converter.determineBaseTimeUnit(3 * DAYS));
        assertEquals(DAYS, converter.determineBaseTimeUnit(5 * DAYS));
        assertEquals(DAYS, converter.determineBaseTimeUnit(13 * DAYS));
        assertEquals(MONTHS, converter.determineBaseTimeUnit(20 * DAYS));
        assertEquals(MONTHS, converter.determineBaseTimeUnit(5 * MONTHS));
        assertEquals(YEARS, converter.determineBaseTimeUnit(9 * MONTHS));
        assertEquals(YEARS, converter.determineBaseTimeUnit(20 * YEARS));
    }

    @Test
    public void testMakeIncrementalCronElement()
    {
        SimpleToCronTriggerConverter converter = new SimpleToCronTriggerConverter();

        //In order for the trigger to fire the right number of times, we must pick the earliest possible start time where
        //START_TIME + N * FREQUENCY = TARGET_TIME, where N is some positive integer, holds true.
        assertEquals("*", converter.makeIncrementalCronElement(1, 1, 0));
        assertEquals("*", converter.makeIncrementalCronElement(59, 1, 0));
        assertEquals("0/2", converter.makeIncrementalCronElement(4, 2, 0));
        assertEquals("1/2", converter.makeIncrementalCronElement(5, 2, 0));
        assertEquals("6/12", converter.makeIncrementalCronElement(6, 12, 0));
        assertEquals("0/15", converter.makeIncrementalCronElement(45, 15, 0));
        assertEquals("10/30", converter.makeIncrementalCronElement(10, 30, 0));
        assertEquals("10/30", converter.makeIncrementalCronElement(10, 30, 0));
        assertEquals("0/3", converter.makeIncrementalCronElement(3, 3, 0));

        assertEquals("2/6", converter.makeIncrementalCronElement(2, 6, 1));
        assertEquals("2/5", converter.makeIncrementalCronElement(7, 5, 1));
        assertEquals("2/2", converter.makeIncrementalCronElement(10, 2, 1));

    }

    @Test
    public void testCalculateRoundedFrequency()
    {
        SimpleToCronTriggerConverter converter = new SimpleToCronTriggerConverter();

        //find a factor that: a) divides the next largest time unit (so we can use cron's '/' operator);
        //                    b) is a multiple of the base time unit; and
        //                    c) is nearest the previously defined interval.
        assertEquals(1, converter.roundInterval(5, SECONDS));
        assertEquals(1, converter.roundInterval(1400, SECONDS));
        assertEquals(20, converter.roundInterval(25 * MINUTES, MINUTES));
        assertEquals(20, converter.roundInterval(29 * MINUTES, MINUTES));
        assertEquals(30, converter.roundInterval(31 * MINUTES, MINUTES));
        assertEquals(30, converter.roundInterval(55 * MINUTES, MINUTES));
        assertEquals(4, converter.roundInterval(5 * HOURS, HOURS));
        assertEquals(8, converter.roundInterval(11 * HOURS, HOURS));
        assertEquals(1, converter.roundInterval(1 * DAYS, DAYS));
        assertEquals(2, converter.roundInterval(2 * DAYS, DAYS));
        assertEquals(2, converter.roundInterval(3 * DAYS, DAYS));
        assertEquals(4, converter.roundInterval(4 * DAYS, DAYS));
        assertEquals(4, converter.roundInterval(5 * DAYS, DAYS));
        assertEquals(1, converter.roundInterval(1 * MONTHS, MONTHS));
        assertEquals(2, converter.roundInterval(2 * MONTHS, MONTHS));
        assertEquals(3, converter.roundInterval(3 * MONTHS, MONTHS));
        assertEquals(4, converter.roundInterval(4 * MONTHS, MONTHS));
        assertEquals(4, converter.roundInterval(5 * MONTHS, MONTHS));
        assertEquals(6, converter.roundInterval(6 * MONTHS, MONTHS));
        assertEquals(1, converter.roundInterval(6 * YEARS, YEARS));
        assertEquals(1, converter.roundInterval(20 * YEARS, YEARS));
        // lossless
        assertEquals(2, converter.roundInterval(2 * HOURS, HOURS));
    }

    @Test
    public void testConvertToCronString()
    {
        SimpleToCronTriggerConverter converter = new SimpleToCronTriggerConverter();

        ConversionResult conversionResult = converter.convertToCronString(parseDate("2006-08-28 07:27:40.586"), 4838400000L);
        ConversionResult expected = new ConversionResult(true, "0 25 7 28 2/2 ?");
        assertEquals("should have no loss because 56 days is converted to 2x28 day months and should start in feb so it hits august", expected, conversionResult);
    }

    @Test
    public void testGetSucceedingTimeUnit()
    {
        SimpleToCronTriggerConverter converter = new SimpleToCronTriggerConverter();

        assertEquals(MINUTES, converter.getSucceedingTimeUnit(SECONDS));
        assertEquals(HOURS, converter.getSucceedingTimeUnit(MINUTES));
        assertEquals(DAYS, converter.getSucceedingTimeUnit(HOURS));
        assertEquals(MONTHS, converter.getSucceedingTimeUnit(DAYS));
        assertEquals(YEARS, converter.getSucceedingTimeUnit(MONTHS));
        try
        {
            converter.getSucceedingTimeUnit(YEARS);
            fail("Should fail if passed YEARS");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            converter.getSucceedingTimeUnit(15);
            fail("Should fail if passed a non-time unit number");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testConvertToCronStringPrintout()
    {
        assertCronConversionCorrect("2006-08-28 07:27:40.586", "4838400000", "0 25 7 28 2/2 ?", true);
        assertCronConversionCorrect("2006-09-12 12:01:08.57", "3628800000", "0 0 12 12 * ?", true);
        assertCronConversionCorrect("2006-08-07 17:36:59.11", "86400000", "0 35 17 * * ?", false);
        assertCronConversionCorrect("2006-08-08 12:27:13.406", "259200000", "0 25 12 2/2 * ?", true);
        assertCronConversionCorrect("2006-08-11 16:42:30.286", "604800000" ,"0 40 16 ? * 6", false);
        assertCronConversionCorrect("2006-08-07 16:56:11.01", "86400000", "0 55 16 * * ?", false);
        assertCronConversionCorrect("2006-08-09 23:38:02.306", "604800000", "0 35 23 ? * 4", false);
        assertCronConversionCorrect("2006-08-11 03:52:25.61", "1814400000","0 50 3 11 * ?", true);
        assertCronConversionCorrect("2006-09-12 21:45:23.943", "4233600000", "0 45 21 12 * ?", true);
        assertCronConversionCorrect("2006-08-09 11:17:42.17", "604800000", "0 15 11 ? * 4", false);
        assertCronConversionCorrect("2006-08-07 13:12:58.366", "3600000", "0 10 * * * ?", false);
        assertCronConversionCorrect("2006-08-11 06:02:40.84", "1814400000", "0 0 6 11 * ?", true);
        assertCronConversionCorrect("2006-08-08 12:31:52.036", "86400000", "0 30 12 * * ?", false);
        assertCronConversionCorrect("2006-08-08 03:15:10.19", "86400000", "0 15 3 * * ?", false);
        assertCronConversionCorrect("2006-08-08 05:15:01.246", "86400000", "0 15 5 * * ?", false);
        assertCronConversionCorrect("2006-08-07 17:36:32.196", "86400000", "0 35 17 * * ?", false);
        assertCronConversionCorrect("2006-08-07 17:36:46.626", "86400000", "0 35 17 * * ?", false);
    }

    public void assertCronConversionCorrect(String nextFire, String interval, String expected, boolean lossy)
    {
        SimpleToCronTriggerConverter converter = new SimpleToCronTriggerConverter();
        ConversionResult result = converter.convertToCronString(parseDate(nextFire), parseLong(interval));
        assertEquals(expected, result.cronString);
        assertEquals(lossy, result.hasLoss);

    }

    DateFormat SIMPLE_TRIGGER_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Date parseDate(String nextFire)
    {
        // strip off millis
        nextFire = nextFire.substring(0, nextFire.indexOf('.'));
        Date date;
        try
        {
            date = SIMPLE_TRIGGER_FORMAT.parse(nextFire);
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Bad test data: " + nextFire);
        }
        return date;
    }

    private long parseLong(String repeatInterval)
    {
        return Long.parseLong(repeatInterval);
    }

}
