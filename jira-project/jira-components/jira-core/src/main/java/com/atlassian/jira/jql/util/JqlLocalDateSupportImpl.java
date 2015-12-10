package com.atlassian.jira.jql.util;

import com.atlassian.core.util.Clock;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.datetime.LocalDateFactory;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.RealClock;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation for {@link com.atlassian.jira.jql.util.JqlLocalDateSupport}
 *
 * @since v4.4
 */
@ThreadSafe
public final class JqlLocalDateSupportImpl implements JqlLocalDateSupport
{
    private static final Logger log = Logger.getLogger(JqlLocalDateSupportImpl.class);
    public static final String PATTERN_WITH_SLASH = "yyyy/MM/dd";
    public static final String PATTERN_WITH_HYPHEN = "yyyy-MM-dd";

    private final Clock clock;
    private final TimeZoneManager timeZoneManager;

    public JqlLocalDateSupportImpl(final Clock clock, TimeZoneManager timeZoneManager)
    {
        this.timeZoneManager = timeZoneManager;
        this.clock = notNull("clock", clock);
    }

    public JqlLocalDateSupportImpl(TimeZoneManager timeZoneManager)
    {
        this(RealClock.getInstance(), timeZoneManager);
    }

    public LocalDate convertToLocalDate(final String dateString)
    {
        notNull("dateString", dateString);
        final String trimLocalDate = StringUtils.trimToNull(dateString);
        if (trimLocalDate != null)
        {
            // need to validate the value as either:
            // period duration e.g. 4d 1h, -1w; or
            // yyyy/MM/dd; or
            // yyyy-MM-dd; or
            LocalDate returnLocalDate = parseDuration(trimLocalDate);
            if (returnLocalDate != null)
            {
                return returnLocalDate;
            }

            // parse literal formats
            return parseLocalDate(dateString);
        }

        if (log.isDebugEnabled())
        {
            log.debug("Unable to parse JQL date '" + dateString + "'.");
        }
        return null;
    }

    public LocalDate convertToLocalDate(final Long dateLong)
    {
        notNull("dateLong", dateLong);
        // intepret the point in time in the user's TZ to create a LocalDate
        TimeZone tz = timeZoneManager.getLoggedInUserTimeZone();
        return LocalDateFactory.getLocalDate(new Date(dateLong.longValue()), tz);
    }

    public String getIndexedValue(final LocalDate date)
    {
        return LuceneUtils.localDateToString(date);
    }

    public boolean validate(final String dateString)
    {
        final String trimDate = StringUtils.trimToNull(dateString);
        return trimDate != null && convertToLocalDate(trimDate) != null;
    }

    public String getLocalDateString(final LocalDate localDate)
    {
        notNull("date", localDate);
        return localDate.toString();
    }

    @Override
    public Date convertToDate(LocalDate date)
    {
        notNull("date", date);
        try
        {
            return toDate(PATTERN_WITH_HYPHEN, date.toString());
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    private LocalDate parseDuration(final String str)
    {
        try
        {
            final long offset = parseDurationOffset(str);
            final Date currentDate = clock.getCurrentDate();
            return convertToLocalDate(currentDate.getTime() + offset);
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

    private LocalDate parseDate(String pattern, String dateString)
    {
        try
        {
            Date date = toDate(pattern, dateString);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            // Calendar MONTH is 0-indexed
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            return new LocalDate(year, month, day);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    private Date toDate(String pattern, String dateString) throws IllegalArgumentException
    {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
        DateTime dateTime = fmt.parseDateTime(dateString);
        return dateTime.toDate();
    }

    private LocalDate parseLocalDate(final String dateString)
    {
        try
        {
            LocalDate localDate = parseDate(PATTERN_WITH_SLASH, dateString);
            return (localDate != null) ? localDate : parseDate(PATTERN_WITH_HYPHEN, dateString);
        }
        catch (RuntimeException ex)
        {
            // Some kind of parse exception - by contract we return null.
            return null;
        }
    }
}
