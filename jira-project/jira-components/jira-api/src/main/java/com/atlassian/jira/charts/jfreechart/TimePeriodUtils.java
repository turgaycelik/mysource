package com.atlassian.jira.charts.jfreechart;

import com.atlassian.jira.timezone.TimeZoneManager;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimePeriod;

import javax.annotation.concurrent.NotThreadSafe;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Utility class for JFreeChart time periods.
 * <p/>
 * This class is <em>not thread safe</em>.
 *
 * @since v4.4
 */
@NotThreadSafe
public final class TimePeriodUtils
{
    /**
     * The pattern to use for formatting Hour instances.
     *
     * @see SimpleDateFormat
     */
    private static final String HOUR_PATTERN = "EEE MMM dd HH:mm:ss z yyyy";

    /**
     * The formatter used for hours.
     */
    private final DateFormat hourDateFormat;

    /**
     * The TimeZoneManager.
     */
    private final TimeZoneManager timeZoneManager;

    /**
     * Creates a new TimePeriodUtils.
     *
     * @param timeZoneManager a TimeZoneManager
     */
    public TimePeriodUtils(TimeZoneManager timeZoneManager)
    {
        this.timeZoneManager = timeZoneManager;
        this.hourDateFormat = createFormatter(HOUR_PATTERN, timeZoneManager);
    }

    /**
     * Prints the time period parameter as a String suitable for presenting in a graph's data table, in the currently
     * logged in user's time zone.
     *
     * @param timePeriod a TimePeriod
     * @return a String
     */
    public String prettyPrint(TimePeriod timePeriod)
    {
        if (timePeriod instanceof Hour)
        {
            return hourDateFormat.format(timePeriod.getStart());
        }

        return String.valueOf(timePeriod);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        TimePeriodUtils that = (TimePeriodUtils) o;

        return !(timeZoneManager != null ? !timeZoneManager.equals(that.timeZoneManager) : that.timeZoneManager != null);
    }

    @Override
    public int hashCode()
    {
        return timeZoneManager != null ? timeZoneManager.hashCode() : 0;
    }

    /**
     * Creates a new SimpleDateFormat using the default hour pattern.
     *
     * @param pattern a String containing the date format to use
     * @param timeZoneManager a TimeZoneManager
     * @return a SimpleDateFormat
     */
    private SimpleDateFormat createFormatter(String pattern, TimeZoneManager timeZoneManager)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(timeZoneManager.getLoggedInUserTimeZone());

        return simpleDateFormat;
    }
}
