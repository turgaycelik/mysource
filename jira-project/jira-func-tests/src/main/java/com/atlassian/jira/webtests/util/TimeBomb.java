package com.atlassian.jira.webtests.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Allows you to ignore a failing test for a limited amount of time.
 * After the "explosion date", the test will be run again.
 * <p>
 * Usage:
 * <pre>
 * // TODO: fix the test and remove the Time Bomb
 * if (new TimeBomb("31/3/2010").ignoreTest())
 * {
 *     log("Ignoring MyTest.testStuff() temporarily.");
 *     return;
 * }
 * </pre>
 *
 * @since v4.3
 */
public class TimeBomb
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private final Date explosionDate;

    /**
     *  Creates a TimeBomb with teh given explosion date.
     * @param date Date to stop ignoring test in format dd/MM/yyyy
     */
    public TimeBomb(String date)
    {
        try
        {
            this.explosionDate = DATE_FORMAT.parse(date);
        }
        catch (ParseException ex)
        {
            throw new RuntimeException("Failed to parse date '" + date + "'.", ex);
        }
    }

    public boolean ignoreTest()
    {
        // Ignore the test if "now" is before the explosion date.
        return (new Date().before(explosionDate));
    }

    public boolean runTest()
    {
        return !ignoreTest();
    }

    public static boolean runAfter(final String explosionDate)
    {
        Date explosion;
        try
        {
            explosion = DATE_FORMAT.parse(explosionDate);
        }
        catch (ParseException ex)
        {
            throw new RuntimeException("Failed to parse date '" + explosionDate + "'.", ex);
        }
        return (new Date().after(explosion));
    }
}
