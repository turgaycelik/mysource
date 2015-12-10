package com.atlassian.jira.web.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.MailDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import static com.atlassian.jira.datetime.DateTimeStyle.COMPLETE;
import static com.atlassian.jira.datetime.DateTimeStyle.DATE;
import static com.atlassian.jira.datetime.DateTimeStyle.DATE_PICKER;
import static com.atlassian.jira.datetime.DateTimeStyle.DATE_TIME_PICKER;
import static com.atlassian.jira.datetime.DateTimeStyle.TIME;

/**
 * Formats and parses dates in a variety of formats, including relative dates such as "Today" and "Last Wednesday".
 * <h4>Warning</h4>
 * As of JIRA 4.4, some of the methods in this class have been retrofitted to be time zone-aware. This means that if you
 * call one of these methods, this class will attempt to determine the time zone of the user that is currently logged
 * in, and will display times in that user's time zone if possible. If the user's time zone can not be determined, or
 * if there is no logged in user, the methods will use the JIRA default user time zone. This is different to the
 * previous behaviour, which was to use the JVM default timezone as returned by
 * {@linkplain java.util.TimeZone#getDefault()} when displaying dates. Please review each method's JavaDoc to determine
 * whether you are affected by this change in behaviour.
 *
 * @see DateTimeFormatterFactory
 */
public class OutlookDate
{
    private static final Logger log = LoggerFactory.getLogger(OutlookDate.class);

    /**
     * ISO8601 Date format. This format includes date and time information. If you need to use date and time use {@link
     * #FORMAT_ISO8601_DATE instead}.
     */
    private static final String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH-mm";

    /**
     * ISO8601 Date format. This format includes date information only. If you need to use date and time use {@link
     * #FORMAT_ISO8601 instead}.
     */
    private static final String FORMAT_ISO8601_DATE = "yyyy-MM-dd";


    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;
    public static final int TODAY = 0;
    public static final int YESTERDAY = 1;
    public static final int THIS_WEEK = 2;
    public static final int OTHER = 3;

    private final Locale locale;
    private final ApplicationProperties applicationProperties;
    private final FormatCache cache = new FormatCache();
    private final I18nHelper.BeanFactory i18nHelperFactory;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;

    /**
     * A formatter that automatically uses the currently logged in user's settings, if possible.
     */
    private final com.atlassian.jira.datetime.DateTimeFormatter userDateTimeFormatter;

    /**
     * Modes used by {@link OutlookDate#formatSmart(java.util.Date, com.atlassian.jira.web.util.OutlookDate.SmartFormatterModes)}
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter} instead. Since v5.0.
     */
    @Deprecated
    public static enum SmartFormatterModes
    {
        /**
         * always show time information.
         *
         * @deprecated use the {@link com.atlassian.jira.datetime.DateTimeFormatterFactory} and the {@link DateTimeStyle#RELATIVE_ALWAYS_WITH_TIME}
         */
        @Deprecated
        SHOW_TIME,

        /**
         * never show time information
         *
         * @deprecated use the {@link com.atlassian.jira.datetime.DateTimeFormatterFactory}  and the {@link DateTimeStyle#RELATIVE_WITHOUT_TIME}
         */
        @Deprecated
        HIDE_TIME,

        /**
         * shows times only on the days of the week (i.e. if printing "Last Wednesday" or "Tomorrow" but not "11/1/1984")
         *
         * @deprecated use the {@link com.atlassian.jira.datetime.DateTimeFormatterFactory} and the {@link DateTimeStyle#RELATIVE_WITH_TIME_ONLY}
         */
        @Deprecated
        SHOW_TIME_ONLY_ON_DAYS
    }

    public OutlookDate(final Locale locale, final ApplicationProperties applicationProperties, final I18nHelper.BeanFactory i18nHelperFactory, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        this.locale = locale;
        this.applicationProperties = applicationProperties;
        this.i18nHelperFactory = i18nHelperFactory;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.userDateTimeFormatter = dateTimeFormatterFactory != null ? dateTimeFormatterFactory.formatter().forLoggedInUser() : null;
    }

    /**
     * An old constructor which is left here only for backward compatibility.
     *
     * @param locale
     *
     * @deprecated Use {@link #OutlookDate(java.util.Locale, com.atlassian.jira.config.properties.ApplicationProperties, com.atlassian.jira.util.I18nHelper.BeanFactory, com.atlassian.jira.datetime.DateTimeFormatterFactory)} or better still {@link com.atlassian.jira.web.util.OutlookDateManager}. Since v4.3
     */
    public OutlookDate(final Locale locale)
    {
        this(locale, ComponentAccessor.getApplicationProperties(), ComponentAccessor.getI18nHelperFactory(), ComponentAccessor.getComponentOfType(DateTimeFormatterFactory.class));
    }

    /**
     * An old constructor which is left here only for backward compatibility.
     *
     * @param locale
     * @param applicationProperties
     *
     * @deprecated Use {@link #OutlookDate(java.util.Locale, com.atlassian.jira.config.properties.ApplicationProperties, com.atlassian.jira.util.I18nHelper.BeanFactory, com.atlassian.jira.datetime.DateTimeFormatterFactory)} or better still {@link com.atlassian.jira.web.util.OutlookDateManager}. Since v4.3
     */
    public OutlookDate(final Locale locale, final ApplicationProperties applicationProperties)
    {
        this(locale, applicationProperties, i18nFactory(applicationProperties), ComponentAccessor.getComponentOfType(DateTimeFormatterFactory.class));
    }

    /**
     * Hacky utility to implement old constructor in a way that should work in Unit Tests as well as production code.
     * This will be removed in the future when the constructor is removed.
     *
     * @param applicationProperties
     * @return I18nHelper.BeanFactory (sometimes null).
     *
     * @deprecated
     */
    private static I18nHelper.BeanFactory i18nFactory(ApplicationProperties applicationProperties)
    {
        if (applicationProperties == null)
        {
            // assume Unit Test - ComponentAccessor might not be initialised
            return null;
        }
        else
        {
            // assume production
            return ComponentAccessor.getI18nHelperFactory();
        }
    }

    /**
     * Formats the given date into ISO8601 format. This format contains date and time information and it is used by data
     * marked with hCalendar microformat. If you need to use date information only use {@link
     * #formatIso8601Date(java.util.Date)} method instead.
     *
     * @param date date to format
     * @return formatted date string
     */
    public String formatIso8601(final Date date)
    {
        return DateTimeFormat.forPattern(FORMAT_ISO8601).print(date.getTime());
    }

    /**
     * Formats the given date into ISO8601 format. This format contains date information only and it is used by data
     * marked with hCalendar microformat. If you need to use date and time information use {@link
     * #formatIso8601(java.util.Date)} method instead.
     *
     * @param date date to format
     * @return formatted date string
     */
    public String formatIso8601Date(final Date date)
    {
        return DateTimeFormat.forPattern(FORMAT_ISO8601_DATE).print(date.getTime());
    }

    /**
     * Returns the given date formatted as a String in the current user's time zone if possible, in the format specified
     * by {@link DateTimeStyle#DATE}.
     *
     * @param date a Date
     * @return a String containing the formatted date
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v4.4.
     */
    @Deprecated
    public String formatDMY(final Date date)
    {
        return userDateTimeFormatter.withStyle(DATE).format(date);
    }

    /**
     * Returns the given date formatted as a String in the current user's time zone if possible, in the format specified
     * by {@link DateTimeStyle#COMPLETE}.
     *
     * @param date a Date
     * @return a String containing the formatted date
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v5.0.
     */
    @Deprecated
    public String formatDMYHMS(final Date date)
    {
        return userDateTimeFormatter.withStyle(COMPLETE).format(date);
    }

    /**
     * Returns the given date formatted as a String in the current user's time zone if possible, in the format specified
     * by {@link DateTimeStyle#TIME}.
     *
     * @param date a Date
     * @return a String containing the formatted date
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v5.0.
     */
    @Deprecated
    public String formatTime(final Date date)
    {
        return userDateTimeFormatter.withStyle(TIME).format(date);
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v4.4.
     */
    @Deprecated
    public String formatDay(final Date date)
    {
        return createFormatterDay().print(date.getTime());
    }

    /**
     * Returns the given date formatted as a String in the current user's time zone if possible, in the format specified
     * by {@link DateTimeStyle#DATE_PICKER}.
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v4.4.
     */
    @Deprecated
    public String formatDatePicker(final Date date)
    {
        return userDateTimeFormatter.withStyle(DATE_PICKER).format(date);
    }

    /**
     * Returns the given date formatted as a String in the current user's time zone if possible, in the format specified
     * by {@link DateTimeStyle#DATE_TIME_PICKER}.
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v4.4.
     */
    @Deprecated
    public String formatDateTimePicker(final Date date)
    {
        return userDateTimeFormatter.withStyle(DATE_TIME_PICKER).format(date);
    }

    /**
     * Format a date for RSS feeds. Uses {@link MailDateFormat} to format the given date. Returns an empty string if
     * null is passed in.
     *
     * @param date date to format
     * @return formatted date or empty string
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since v5.0.
     */
    @Deprecated
    public String formatRss(final Date date)
    {
        return formatRssRfc822(date);
    }

    /**
     * Formats a date using the correct RFC822 format as indicated in the RSS v2 specification and not using the {@link
     * MailDateFormat} which is technically incorrect.  {@link MailDateFormat} will however parse this format.
     * <p/>
     * See <a href="http://cyber.law.harvard.edu/rss/rss.html">http://cyber.law.harvard.edu/rss/rss.html</a>
     * <p/>
     * See <a href="http://asg.web.cmu.edu/rfc/rfc822.html#sec-5.1">http://asg.web.cmu.edu/rfc/rfc822.html#sec-5.1</a>
     * <p/>
     * Correct : Wed, 22 Aug 2007 10:00:10 +1000
     * <p/>
     * Incorrect : Wed, 22 Aug 2007 10:00:10 +1000 (GMT+10:00)
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since v4.4.
     * @see #format(java.util.Date)
     *
     * @param date the date to format into a string
     * @return a date string in RFC822 format
     * @since v3.10.3
     */
    @Deprecated
    public String formatRssRfc822(final Date date)
    {
        return date == null ? "" : dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.RSS_RFC822_DATE_TIME).forLoggedInUser().format(date);
    }

    /**
     * Parse a date from RSS feeds. Uses {@link MailDateFormat} to parse the date.
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since v4.4.
     * @see #format(java.util.Date)
     *
     * @param rssDate RSS date
     * @return new Date created from the given string
     * @throws ParseException if string parsing fails
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#parse(String)} instead. Since v5.0.
     */
    @Deprecated
    public static Date parseRss(final String rssDate) throws ParseException
    {
        return new MailDateFormat().parse(rssDate);
    }

    /**
     * Returns the current date formatted as a String in the current user's time zone if possible, in the format specified
     * by {@link DateTimeStyle#RELATIVE}.
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since v4.4.
     * @see #format(java.util.Date)
     */
    @Deprecated
    public String format()
    {
        return format(new Date());
    }

    /**
     * Returns the given date formatted as a String in the current user's time zone if possible, in the format specified
     * by {@link DateTimeStyle#RELATIVE}.
     *
     * @param date the date to format
     * @return the date printed (i18n)
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v4.4.
     */
    @Deprecated
    public String format(final Date date)
    {
        return userDateTimeFormatter.format(date);
    }

    /**
     * Format the date "smartly", by using the day of the week if the date falls within a week in either direction of
     * the current date. If the date is in the past, we will use terminology such as "last Wednesday" to differentiate
     * it from "Wednesday" (which is in the future).
     *
     * @param date the date to format
     * @param mode whether or not to include the time in the printed date
     * @return the date printed (i18n)
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v5.0.
     */
    @Deprecated
    public String formatSmart(final Date date, final SmartFormatterModes mode)
    {
        return formatSmart(date, new Date(), mode);
    }

    /**
     * Returns the given date formatted as a String in the current user's time zone if possible.
     *
     * @param date the date to format
     * @param referenceDate the date to compare against
     * @param mode whether or not to include the time in the printed date
     * @return the date printed (i18n)
     * @see #formatSmart(java.util.Date, com.atlassian.jira.web.util.OutlookDate.SmartFormatterModes)
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v5.0.
     */
    @Deprecated
    String formatSmart(final Date date, final Date referenceDate, final SmartFormatterModes mode)
    {
        if (SmartFormatterModes.SHOW_TIME_ONLY_ON_DAYS.equals(mode))
        {
            return userDateTimeFormatter.withStyle(DateTimeStyle.RELATIVE_WITH_TIME_ONLY).format(date);
        }
        else if (SmartFormatterModes.HIDE_TIME.equals(mode))
        {
            return userDateTimeFormatter.withStyle(DateTimeStyle.RELATIVE_WITHOUT_TIME).format(date);
        }
        return userDateTimeFormatter.withStyle(DateTimeStyle.RELATIVE_ALWAYS_WITH_TIME).format(date);
    }

    /**
     * Returns new date
     *
     * @return new date
     * @deprecated Use {@link java.util.Date#Date()} instead. Since v4.4.
     */
    @Deprecated
    public Date getNow()
    {
        return new Date(System.currentTimeMillis());
    }

    /**
     * @deprecated Use {@link org.joda.time.Days#daysBetween(org.joda.time.ReadablePartial, org.joda.time.ReadablePartial)} instead. Since v4.4.
     */
    @Deprecated
    public int daysAgo(final Date date)
    {
        return daysAgo(date, new Date());
    }

    /**
     * The method determines whether the date (theDate) lies between the reference date and the previous midnight
     * ({@link #TODAY}), between the reference date and the midnight before previous ({@link #YESTERDAY}), or between
     * the reference date and 7 midnights ago ({@link #THIS_WEEK}). If the passed in date does not fall in any of the
     * mentioned ranges, the method returns {@link #OTHER}.
     *
     * @param theDate the date we are asking about
     * @param theReferenceDate the reference date (usually today's date)
     * @return {@link #TODAY}, {@link #YESTERDAY}, {@link #THIS_WEEK} or {@link #OTHER}
     *
     * @deprecated Use {@link org.joda.time.Days#daysBetween(org.joda.time.ReadablePartial, org.joda.time.ReadablePartial)} instead. Since v4.4.
     */
    @Deprecated
    public int daysAgo(final Date theDate, final Date theReferenceDate)
    {
        // note: need to use LocalDate objects here instead of DateTime because otherwise the daysBetween calculation
        // can be affected by Daylight Savings Time.
        final LocalDate refDt = new LocalDate(getTimeAtEndOfDate(theReferenceDate));
        final LocalDate timeDt = new LocalDate(theDate.getTime());
        final int daysBetween = Days.daysBetween(timeDt, refDt).getDays();

        if (daysBetween == 0)
        {
            return TODAY;
        }
        else if (daysBetween == 1)
        {
            return YESTERDAY;
        }
        else if ((daysBetween > 1) && (daysBetween < 7)) // seven is used as tempTime represents the previous midnight
        {
            return THIS_WEEK;
        }
        else
        {
            return OTHER;
        }
    }

    /**
     * Parses the given text in {@link DateTimeStyle#COMPLETE COMPLETE} format, using the current user's time zone and
     * locale if possible. If there is no currently logged in user, or if the currently logged in user has not
     * configured a time zone and/or locale the JIRA default time zone and/or locale is used.
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#parse(String)} instead. Since v4.4.
     */
    @Deprecated
    public Date parseCompleteDateTime(final String value) throws ParseException
    {
        return parse(COMPLETE, value);
    }

    /**
     * Parses the given text in {@link DateTimeStyle#DATE_PICKER DATE_PICKER} or {@link DateTimeStyle#DATE_TIME_PICKER
     * DATE_TIME_PICKER} format, using the current user's time zone and locale if possible. If there is no currently
     * logged in user, or if the currently logged in user has not configured a time zone and/or locale the JIRA default
     * time zone and/or locale is used.
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#parse(String)} instead. Since v4.4.
     */
    @Deprecated
    public Date parseDatePicker(final String value) throws ParseException
    {
        try
        {
            return parse(DATE_PICKER, value);
        }
        catch (final ParseException e)
        {
            return parseDateTimePicker(value);
        }
    }

    /**
     * Parses the given text in {@link DateTimeStyle#DATE_TIME_PICKER DATE_TIME_PICKER} format, using the current user's
     * time zone and locale if possible. If there is no currently logged in user, or if the currently logged in user has
     * not configured a time zone and/or locale the JIRA default time zone and/or locale is used.
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#parse(String)} instead. Since v4.4.
     */
    @Deprecated
    public Date parseDateTimePicker(final String value) throws ParseException
    {
        return parse(DATE_TIME_PICKER, value);
    }

    /*
     * JodaTime throws an IllegalArgumentException rather than a checked ParseException.
     * OutlookDate parsing throws the checked one, so we have to convert it.
     */
    private Date parse(DateTimeStyle style, String value) throws ParseException
    {
        try
        {
            return userDateTimeFormatter.withStyle(style).parse(value);
        }
        catch (final IllegalArgumentException e)
        {
            // cannot get the parse position from JodaTime's formatter
            throw new ParseException(e.getMessage(), 0);
        }
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#getFormatHint()} instead. Since v4.4.
     */
    @Deprecated
    public String getDatePickerFormat()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_DATE_PICKER_JAVA_FORMAT);
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v4.4.
     */
    @Deprecated
    public String getDatePickerFormatSample(final Date date)
    {
        return formatDatePicker(date);
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#format(java.util.Date)} instead. Since
     *             v4.4.
     */
    @Deprecated
    public String getDateTimePickerFormatSample(final Date date)
    {
        return formatDateTimePicker(date);
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#parse(String)} instead. Since v5.0.
     * @param value
     * @return
     */
    @Deprecated
    public boolean isDatePickerDate(final String value)
    {
        if (value == null)
        {
            return false;
        }
        try
        {
            parseDatePicker(value);
            return true;
        }
        catch (final IllegalArgumentException e)
        {
            return false;
        }
        catch (final ParseException e)
        {
            return false;
        }
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#getFormatHint()} instead. Since v4.4.
     */
    @Deprecated
    public String getDateTimePickerFormat()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_DATE_TIME_PICKER_JAVA_FORMAT);
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#getFormatHint()} instead. Since v4.4.
     */
    @Deprecated
    public String getCompleteDateTimeFormat()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_DATE_COMPLETE);
    }

    /**
     * This helper is here to assist with formatting emails etc, and should only be used by JIRAVelocityManager.
     *
     * @return a {@link DateFormat} based on the run-time value of {@link APKeys#JIRA_LF_DATE_COMPLETE}
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter#getFormatHint()} instead. Since v4.4.
     */
    @Deprecated
    public DateFormat getCompleteDateFormat()
    {
        return new SimpleDateFormat(getCompleteDateTimeFormat(), locale);
    }

    private DateTimeFormatter createFormatterDay()
    {
        return formatterForKey(APKeys.JIRA_LF_DATE_DAY);
    }

    private DateTimeFormatter formatterForKey(final String key)
    {
        return formatter(applicationProperties.getDefaultBackedString(key));
    }

    private DateTimeFormatter formatter(final String pattern)
    {
        return cache.get(pattern);
    }

    private static long getTimeAtEndOfDate(final Date referenceDate)
    {
        final Calendar startOfReference = new GregorianCalendar();
        startOfReference.setTime(referenceDate);
        startOfReference.set(Calendar.MILLISECOND, 999);
        startOfReference.set(Calendar.SECOND, 59);
        startOfReference.set(Calendar.MINUTE, 59);
        startOfReference.set(Calendar.HOUR_OF_DAY, 23);
        return startOfReference.getTime().getTime();
    }

    final class FormatCache implements Function<String, DateTimeFormatter>
    {
        ConcurrentMap<String, DateTimeFormatter> map = CopyOnWriteMap.newHashMap();

        public DateTimeFormatter get(final String pattern)
        {
            DateTimeFormatter result = map.get(pattern);
            while (result == null)
            {
                DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern).withLocale(locale);
                DateTimeFormatter prev = map.putIfAbsent(pattern, formatter);
                if (prev == null)
                {
                    if (log.isTraceEnabled())
                    {
                        log.trace("Added ({},{}) to formatter cache (size = {})", new Object[] { pattern, locale, map.size() });
                    }

                    result = formatter;
                }
                else
                {
                    result = map.get(pattern);
                }
            }

            return result;
        }
    }
}
