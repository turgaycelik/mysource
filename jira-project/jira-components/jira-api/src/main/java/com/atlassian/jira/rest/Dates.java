package com.atlassian.jira.rest;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Utility class for formatting and parsing date and date/time objects in ISO8601 format.
 *
 * @since v4.2
 */
public class Dates
{
    /**
     * The format used for dates in the REST plugin.
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * The format used for times in the REST plugin.
     */
    public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Parser for rest times - this one - accepts Z as timezone.
     */
    public static final DateTimeFormatter ISO_DATE_TIME_PARSER = ISODateTimeFormat.dateTime();
    /**
     * Printer for rest time entries - this one should print +0000 for UTC timezone.
     */
    public static final DateTimeFormatter ISO_DATE_TIME_PRINTER = DateTimeFormat.forPattern(TIME_FORMAT);

    /**
     * Converts the given Date object to a String. The returned string is in the format <code>{@value
     * #TIME_FORMAT}</code>.
     *
     * @param date a Date
     * @return a String representation of the date and time
     */
    public static String asTimeString(@Nullable Date date)
    {
        return date != null ? new SimpleDateFormat(TIME_FORMAT).format(date) : null;
    }

    /**
     * Converts the given Timestamp object to a String. The returned string is in the format <code>{@value
     * #TIME_FORMAT}</code>.
     *
     * @param timestamp a Date
     * @return a String representation of the timestamp and time
     */
    public static String asTimeString(@Nullable Timestamp timestamp)
    {
        return timestamp != null ? Dates.asTimeString(new Date(timestamp.getTime())) : null;
    }

    /**
     * Converts the given date and time String to a Date object. The time parameter is expected to be in the format
     * <code>{@value #TIME_FORMAT}</code>.
     *
     * @param time a String representation of a date and time
     * @return a Date
     * @throws RuntimeException if there is an error parsing the date
     * @throws IllegalArgumentException if the input string is not in the expected format
     */
    public static Date fromTimeString(@Nullable String time) throws IllegalArgumentException
    {
        try
        {
            SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT);
            format.setLenient(false);
            return time != null ? format.parse(time) : null;
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Error parsing time: " + time, e);
        }
    }

    /**
     * Converts the given Date object to a String. The returned string is in the format <code>{@value
     * #DATE_FORMAT}</code>.
     *
     * @param date a Date
     * @return a String representation of the date
     */
    public static String asDateString(@Nullable Date date)
    {
        return date != null ? new SimpleDateFormat(DATE_FORMAT).format(date) : null;
    }

    /**
     * Converts the given Timestamp object to a String. The returned string is in the format <code>{@value
     * #DATE_FORMAT}</code>.
     *
     * @param timestamp a Date
     * @return a String representation of the timestamp
     */
    public static String asDateString(@Nullable Timestamp timestamp)
    {
        return timestamp != null ? asDateString(new Date(timestamp.getTime())) : null;
    }

    /**
     * Converts the given date String into a Date object. The date parameter is expected to be in the format
     * <code>{@value #DATE_FORMAT}</code>.
     *
     * @param date a String containing a date
     * @return a Date
     * @throws IllegalArgumentException if the input string is not in the expected format
     */
    public static Date fromDateString(@Nullable String date) throws IllegalArgumentException
    {
        try
        {
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
            format.setLenient(false);
            return date != null ? format.parse(date) : null;
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Error parsing date string: " + date, e);
        }
    }

    /**
     * Converts the given DateTime object to ISO String.
     */
    public static String asISODateTimeString(@Nullable DateTime dateTime)
    {
        return ISO_DATE_TIME_PRINTER.print(dateTime);
    }

    public static DateTime fromISODateTimeString(@Nullable String date) {
        final DateTime parsedDateTime = date==null ? null : ISO_DATE_TIME_PARSER.parseDateTime(date);

        return parsedDateTime;
    }

    private Dates()
    {
        // prevent instantiation
    }

    public static class DateAdapter extends XmlAdapter<String, Date>
    {
        @Override
        public Date unmarshal(String text) throws Exception
        {
            return Dates.fromDateString(text);
        }

        @Override
        public String marshal(Date date) throws Exception
        {
            return Dates.asDateString(date);
        }
    }

    public static class DateTimeAdapter extends XmlAdapter<String, Date>
    {
        @Override
        public Date unmarshal(final String s) throws Exception
        {
            return Dates.fromTimeString(s);
        }

        @Override
        public String marshal(final Date date) throws Exception
        {
            return Dates.asTimeString(date);
        }
    }
}
