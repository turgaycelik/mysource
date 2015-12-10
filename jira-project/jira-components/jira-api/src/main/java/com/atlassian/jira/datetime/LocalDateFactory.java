package com.atlassian.jira.datetime;

import org.joda.time.Chronology;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.GJChronology;

import java.util.Date;
import java.util.TimeZone;

/**
 * Used for creating {@link LocalDate} objects.
 *
 * @since v4.4
 */
public class LocalDateFactory
{
    /**
     * Constructs a new LocalDate from the given {@code java.util.Date} by interpreting the Date in the system (JVM) timezone.
     * <p>
     * NB will return null output for null input
     *
     * @param date the date to convert
     * @return a new LocalDate from the given {@code java.util.Date} by interpreting the Date in the system (JVM) timezone. (null output for null input)
     */
    public static LocalDate from(Date date)
    {
        if (date == null)
        {
            return null;
        }

        DateMidnight ld = new DateMidnight(date, GJChronology.getInstance());

        return from(ld);
    }

    /**
     * Returns the LocalDate for a point in time in a given TimeZone.
     * <p>
     * NB will return null output for null input
     *
     * @param date The point in time
     * @param timeZone The TimeZone
     * @return the LocalDate for a point in time in a given TimeZone
     */
    public static LocalDate getLocalDate(Date date, TimeZone timeZone)
    {
        if (date == null)
        {
            return null;
        }

        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);
        Chronology chronology = GJChronology.getInstance(dateTimeZone);
        DateMidnight ld = new DateMidnight(date.getTime(), dateTimeZone).withChronology(chronology);

        return from(ld);
    }

    private static LocalDate from(DateMidnight ld)
    {
        // Check for BCE:
        if (ld.getEra() != DateTimeConstants.AD)
        {
            throw new IllegalArgumentException("LocalDate only handles the Common Era - no BC dates are allowed.");
        }
        int year = ld.getYearOfEra();
        // Calendar MONTH is 0-indexed
        int month = ld.getMonthOfYear();
        int day = ld.getDayOfMonth();
        return new LocalDate(year, month, day);
    }


    /**
     * Turns an "ISO Basic" formatted date (ie YYYYMMDD) into a LocalDate.
     *
     * @param isoDate an "ISO Basic" formatted date
     *
     * @return the LocalDate represented by the given ISO format String
     */
    public static LocalDate fromIsoBasicFormat(String isoDate)
    {
        if (isoDate == null)
        {
            return null;
        }
        if (isoDate.length() != 8)
        {
            throw new IllegalArgumentException("Input must be in the format 'YYYYMMDD'.");
        }
        int year = Integer.parseInt(isoDate.substring(0, 4));
        int month = Integer.parseInt(isoDate.substring(4, 6));
        int day = Integer.parseInt(isoDate.substring(6, 8));
        return new LocalDate(year, month, day);
    }

    /**
     * Formats the given LocalDate in "ISO Basic" format (ie YYYYMMDD).
     * @param localDate the date to format.
     *
     * @return the "ISO Basic" format (or null for null input)
     */
    public static String toIsoBasic(LocalDate localDate)
    {
        if (localDate == null)
        {
            return null;
        }

        return format4(localDate.getYear()) + format2(localDate.getMonth()) + format2(localDate.getDay());
    }

    private static String format4(int i)
    {
        // We just put the most likely case first for preformance reasons (This is for formatting years)
        if (i >= 1000 && i <= 9999)
        {
            return String.valueOf(i);
        }
        if (i >= 0 && i < 10)
        {
            return "000" + i;
        }
        if (i >= 10 && i < 100)
        {
            return "00" + i;
        }
        if (i >= 100 && i < 1000)
        {
            return "0" + i;
        }
        throw new IllegalArgumentException("Invalid value " + i + " must be between 0 and 9999");
    }

    private static String format2(int i)
    {
        if (i > 0 && i < 10)
        {
            return "0" + i;
        }
        if (i >= 10 && i < 100)
        {
            return String.valueOf(i);
        }
        throw new IllegalArgumentException("Invalid value " + i + " must be between 1 and 99");
    }
}
