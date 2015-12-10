package com.atlassian.jira.jql.util;

import com.atlassian.core.util.Clock;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.RealClock;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation for {@link com.atlassian.jira.jql.util.JqlDateSupport}
 *
 * @since v4.0
 */
@ThreadSafe
public final class JqlDateSupportImpl implements JqlDateSupport
{
    private static final Logger log = Logger.getLogger(JqlDateSupportImpl.class);
    private static final String YYYY_MM_DD1 = "yyyy/MM/dd";
    private static final String YYYY_MM_DD2 = "yyyy-MM-dd";
    private static final String YYYY_MM_DD_HH_MM1 = YYYY_MM_DD1 + " HH:mm";
    private static final String YYYY_MM_DD_HH_MM2 = YYYY_MM_DD2 + " HH:mm";
    private static final String ATLASSIAN_DURATION = "AD";
    private static final String[] ACCEPTED_FORMATS = new String[] { YYYY_MM_DD_HH_MM1, YYYY_MM_DD_HH_MM2, YYYY_MM_DD1, YYYY_MM_DD2 };
    private static final Pattern DURATION_PATTERN = Pattern.compile("(?:\\d+(?:\\.\\d+)?|\\.\\d+)(.)?", Pattern.CASE_INSENSITIVE);

    private final Clock clock;
    private final TimeZoneManager timeZoneManager;

    enum Precision
    {
        MINUTES
        {
            Calendar setToStart(Calendar calendar)
            {
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);

                return calendar;
            }

            Calendar setToEnd(Calendar calendar)
            {
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 59);

                return calendar;
            }
        },
        HOURS
        {
            Calendar setToStart(Calendar calendar)
            {
                MINUTES.setToStart(calendar).set(Calendar.MINUTE, 0);
                return calendar;
            }

            Calendar setToEnd(Calendar calendar)
            {
                MINUTES.setToEnd(calendar).set(Calendar.MINUTE, 59);
                return calendar;
            }
        },
        DAYS
        {
            Calendar setToStart(Calendar calendar)
            {
                HOURS.setToStart(calendar).set(Calendar.HOUR_OF_DAY, 0);
                return calendar;
            }

            Calendar setToEnd(Calendar calendar)
            {
                HOURS.setToEnd(calendar).set(Calendar.HOUR_OF_DAY, 23);
                return calendar;
            }
        };

        abstract Calendar setToStart(Calendar calendar);
        abstract Calendar setToEnd(Calendar calendar);

        DateRange createRange(Date date, TimeZone zone)
        {
            Calendar calendar = Calendar.getInstance(zone);
            calendar.setTime(date);

            return new DateRange(setToStart(calendar).getTime(), setToEnd(calendar).getTime());
        }
    }

    public JqlDateSupportImpl(final Clock clock, final TimeZoneManager timeZoneManager)
    {
        this.timeZoneManager = timeZoneManager;
        this.clock = notNull("clock", clock);
    }

    public JqlDateSupportImpl(final TimeZoneManager timeZoneManager)
    {
        this(RealClock.getInstance(), timeZoneManager);
    }

    public Date convertToDate(final String dateString)
    {
        return convertToDate(dateString, timeZoneManager.getLoggedInUserTimeZone());
    }

    @Override
    public Date convertToDate(String dateString, TimeZone timeZone)
    {
        notNull("dateString", dateString);
        final String trimDate = StringUtils.trimToNull(dateString);
        if (trimDate != null)
        {
            // need to validate the value as either:
            // period duration e.g. 4d 1h, -1w; or
            // yyyy/MM/dd; or
            // yyyy-MM-dd; or
            // yyyy/MM/dd hh:mm; or
            // yyyy-MM-dd hh:mm
            Date returnDate = parseDuration(trimDate);
            if (returnDate != null)
            {
                return returnDate;
            }

            // parse regular formats
            for (String acceptedFormat : ACCEPTED_FORMATS)
            {
                returnDate = parseDateForFormat(acceptedFormat, trimDate, timeZone);
                if (returnDate != null)
                {
                    return returnDate;
                }
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug("Unable to parse JQL date '" + dateString + "'.");
        }
        return null;
    }

    @Override
    public DateRange convertToDateRangeWithImpliedPrecision(String dateString)
    {
        notNull("dateString", dateString);
        final String trimDate = StringUtils.trimToNull(dateString);
        if (trimDate != null)
        {
            // need to validate the value as either:
            // period duration e.g. 4d 1h, -1w; or
            // yyyy/MM/dd; or
            // yyyy-MM-dd; or
            // yyyy/MM/dd hh:mm; or
            // yyyy-MM-dd hh:mm
            Date returnDate = parseDuration(trimDate);
            if (returnDate != null)
            {
                // This is still not correct.  We havent yet done the work
                // to implement the precision of an atlassian duration.
                //
                // Currently thinking is that is implied by the unit of time qualifier.
                // d,w,m,y is 24 hour precision.  h is hour duration so say -24h is
                // 1 day ago but with hourly precision
                //
                // ANyway I digress because we havent written this yet!
                //
                return toPrecision(returnDate, ATLASSIAN_DURATION, dateString);
            }

            // parse regular formats
            for (String acceptedFormat : ACCEPTED_FORMATS)
            {
                returnDate = parseDateForFormat(acceptedFormat, trimDate, timeZoneManager.getLoggedInUserTimeZone());
                if (returnDate != null)
                {
                    return toPrecision(returnDate, acceptedFormat, null);
                }
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug("Unable to parse JQL date '" + dateString + "'.");
        }
        return null;
    }

    /**
     * This will round out a date to a precision based on the input string.  We need to do this
     * to be more meaningful to the user when the type in x = '2011-10-13'.
     *
     * @param returnDate the parsed data in its specific form
     * @param acceptedFormat the format we used to
     * @param dateString
     * @return
     */
    private DateRange toPrecision(Date returnDate, String acceptedFormat, String dateString)
    {
        Precision precision = null;

        if (YYYY_MM_DD_HH_MM1.equals(acceptedFormat) || YYYY_MM_DD_HH_MM2.equals(acceptedFormat))
        {
            precision = Precision.MINUTES;
        }
        else if (YYYY_MM_DD1.equals(acceptedFormat) || YYYY_MM_DD2.equals(acceptedFormat))
        {
            precision = Precision.DAYS;
        }
        else if (ATLASSIAN_DURATION.equals(acceptedFormat))
        {
            // ok its an atlassian duration
            precision = reverseParseAtlassianDuration(dateString.toLowerCase(Locale.ENGLISH));
        }

        if (precision != null)
        {
            return precision.createRange(returnDate, timeZoneManager.getLoggedInUserTimeZone());
        }
        else
        {
            // Is this even possible? probably not but here we are so something sensible
            return new DateRange(returnDate, returnDate);
        }
    }

    /**
     * Our rule is that least significant date unit determines the resolution.
     *
     * @param durationStr the duration string to check
     * @return the data unit
     */
    Precision reverseParseAtlassianDuration(String durationStr)
    {
        Precision rank = null;
        
        Matcher matcher = DURATION_PATTERN.matcher(durationStr);
        while (matcher.find())
        {
            Precision current;
            String util = StringUtils.stripToNull(matcher.group(1));
            if (util == null || util.equals("m"))
            {
                //If we find minutes then just return immediately.
                return Precision.MINUTES;
            }
            else if (util.equals("h"))
            {
                current = Precision.HOURS;
            }
            else if (util.equals("d") || util.equals("w"))
            {
                current = Precision.DAYS;
            }
            else
            {
                //Assume minutes in the worst possible case.
                return Precision.MINUTES;
            }

            if (rank == null || rank.compareTo(current) > 0)
            {
                rank = current;
            }
        }
        return rank == null ? Precision.MINUTES : rank;
    }

    public Date convertToDate(final Long dateLong)
    {
        return new Date(notNull("dateLong", dateLong));
    }

    @Override
    public DateRange convertToDateRange(Long dateLong)
    {
        Date date = new Date(notNull("dateLong", dateLong));
        return new DateRange(date, date);
    }

    public String getIndexedValue(final Date date)
    {
        return LuceneUtils.dateToString(date);
    }

    public boolean validate(final String dateString)
    {
        notNull("dateString", dateString);
        final String trimDate = StringUtils.trimToNull(dateString);
        return trimDate != null && convertToDate(trimDate) != null;
    }

    public String getDateString(final Date date)
    {
        return getDateString(date, timeZoneManager.getLoggedInUserTimeZone());
    }

    @Override
    public String getDateString(Date date, TimeZone timeZone)
    {
        notNull("date", date);
        final DateFormat format;
        if (isMidnightDate(date, timeZone))
        {
            format = new SimpleDateFormat(YYYY_MM_DD2);
            format.setTimeZone(timeZone);
        }
        else
        {
            format = new SimpleDateFormat(YYYY_MM_DD_HH_MM2);
            format.setTimeZone(timeZone);
        }
        return format.format(date);
    }

    @Override
    public boolean isDuration(String dateString)
    {
        String trimDate = StringUtils.trimToNull(dateString);
        if (trimDate == null)
        {
            return false;
        }

        try
        {
            parseDurationOffset(dateString);
            return true;
        }
        catch (InvalidDurationException e)
        {
            return false;
        }
        catch (NumberFormatException ne)
        {
            return false;
        }
    }

    public static String getDurationString(final long duration)
    {
        return DateUtils.getDurationStringWithNegative(TimeUnit.MILLISECONDS.toSeconds(duration));
    }

    private Date parseDuration(final String str)
    {
        try
        {
            final long offset = parseDurationOffset(str);
            final Date currentDate = clock.getCurrentDate();
            return new Date(currentDate.getTime() + offset);
        }
        catch (InvalidDurationException e)
        {
            return null;
        }
        catch (NumberFormatException ne)
        {
            return null;
        }
    }

    private long parseDurationOffset(final String str) throws InvalidDurationException
    {
        return TimeUnit.SECONDS.toMillis(DateUtils.getDurationWithNegative(str));
    }

    private Date parseDateForFormat(final String format, final String date, TimeZone timeZone)
    {
        // test date string matches format structure using regex
        // - weed out illegal characters and enforce 4-digit year
        // - create the regex based on the local format string
        String reFormat = Pattern.compile("d+|M+|H+|m+").matcher(Matcher.quoteReplacement(format)).replaceAll("\\\\d{1,2}");
        reFormat = Pattern.compile("y+").matcher(reFormat).replaceAll("\\\\d{4,}");
        if (Pattern.compile(reFormat).matcher(date).matches())
        {

            // date string matches format structure,
            // - now test it can be converted to a valid date
            SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance();
            sdf.applyPattern(format);
            sdf.setTimeZone(timeZone);
            sdf.setLenient(false);
            try
            {
                return sdf.parse(date);
            }
            catch (ParseException e)
            {
                return null;
            }
        }
        return null;
    }

    private static boolean isMidnightDate(final Date value, TimeZone timeZone)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        cal.setTimeZone(timeZone);
        return cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 &&
                cal.get(Calendar.SECOND) == 0 && cal.get(Calendar.MILLISECOND) == 0;
    }
}
