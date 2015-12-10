package com.atlassian.jira.web.component.cron;

import com.atlassian.jira.util.JiraUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the form state for the CronEditorWebComponent.
 */
public class CronEditorBean
{

    // Defines the modes
    public static final String DAILY_SPEC_MODE = "daily";
    public static final String DAYS_OF_WEEK_SPEC_MODE = "daysOfWeek";
    public static final String DAYS_OF_MONTH_SPEC_MODE = "daysOfMonth";
    public static final String ADVANCED_MODE = "advanced";
    public static final String DOT = ".";

    // Defines the params that get submitted via the cron editor form
    private static final String CRON_STRING = "cronString";
    private static final String DAILY_WEEKLY_MONTHLY = "dailyWeeklyMonthly";
    private static final String RUN_ONCE_MINS = "runOnceMins";
    private static final String RUN_ONCE_HOURS = "runOnceHours";
    private static final String RUN_ONCE_MERIDIAN = "runOnceMeridian";
    private static final String RUN_FROM_HOURS = "runFromHours";
    private static final String RUN_FROM_MERIDIAN = "runFromMeridian";
    private static final String RUN_TO_HOURS = "runToHours";
    private static final String RUN_TO_MERIDIAN = "runToMeridian";
    private static final String WEEKDAY = "weekday";
    private static final String DAY = "day";
    private static final String WEEK = "week";
    private static final String DAYS_OF_MONTH_OPT = "daysOfMonthOpt";
    private static final String MONTH_DAY = "monthDay";
    private static final String INTERVAL = "interval";
    private static final String DAY_OF_WEEK_OF_MONTH = "dayOfWeekOfMonth";

    private Map params;
    private String cronString;
    private String mode;
    private boolean dayOfWeekOfMonth;
    private String dayOfMonth;
    private String minutes;
    private String hoursRunOnce;
    private String hoursRunOnceMeridian;
    private String hoursFrom;
    private String hoursFromMeridian;
    private String hoursTo;
    private String hoursToMeridian;
    private String specifiedDaysOfWeek;
    private String dayInMonthOrdinal;
    private String incrementInMinutes;
    private String seconds;

    public CronEditorBean()
    {
        this.params = new HashMap();
    }

    /**
     * Initialises to the state defined by the given params, which are identified
     * by the presence of paramPrefix on the key.
     *
     * @param paramPrefix the prefix used on each of the params.
     * @param params      state parameters to use.
     */
    public CronEditorBean(String paramPrefix, /*<String, String[]>*/ Map params)
    {
        this.params = params;
        this.cronString = getParam(paramPrefix, CRON_STRING);
        this.mode = getParam(paramPrefix, DAILY_WEEKLY_MONTHLY);
        // note that we don't expect seconds from the UI
        this.minutes = getParam(paramPrefix, RUN_ONCE_MINS);
        this.hoursRunOnce = getParam(paramPrefix, RUN_ONCE_HOURS);
        this.hoursRunOnceMeridian = getParam(paramPrefix, RUN_ONCE_MERIDIAN);
        this.hoursFrom = getParam(paramPrefix, RUN_FROM_HOURS);
        this.hoursFromMeridian = getParam(paramPrefix, RUN_FROM_MERIDIAN);
        this.hoursTo = getParam(paramPrefix, RUN_TO_HOURS);
        this.hoursToMeridian = getParam(paramPrefix, RUN_TO_MERIDIAN);
        String[] daysOfWeek = (String[]) params.get(paramPrefix + DOT + WEEKDAY);
        if (DAYS_OF_MONTH_SPEC_MODE.equals(mode))
        {
            this.specifiedDaysOfWeek = getParam(paramPrefix, DAY);
            this.dayInMonthOrdinal = getParam(paramPrefix, WEEK);
            // Find the sub-mode
            String dayOfWeekOfMonthString = getParam(paramPrefix, DAYS_OF_MONTH_OPT);
            this.dayOfWeekOfMonth = DAY_OF_WEEK_OF_MONTH.equals(dayOfWeekOfMonthString);
        }
        else if (DAYS_OF_WEEK_SPEC_MODE.equals(mode))
        {
            this.specifiedDaysOfWeek = StringUtils.join(daysOfWeek, ',');
        }

        this.dayOfMonth = getParam(paramPrefix, MONTH_DAY);
        this.incrementInMinutes = getParam(paramPrefix, INTERVAL);
    }

    /**
     * Used to validate the hours to see that the from hour is before the to hour, if specified.
     * Note: to allow the range to run "all-day" (eg. from 12AM to 12AM), we need to also allow the from and to times to
     * be equal.
     *
     * @return true if from is before to or if they have not been specified.
     */
    public boolean isRangeHoursValid()
    {
        if (hoursFrom != null && hoursFromMeridian != null && hoursTo != null && hoursToMeridian != null && incrementInMinutes != null && !incrementInMinutes.equals("0"))
        {
            try
            {
                int hoursFromInt = Integer.parseInt(hoursFrom);
                int hoursToInt = Integer.parseInt(hoursTo);
                return JiraUtils.get24HourTime(hoursFromMeridian, hoursFromInt) <= JiraUtils.get24HourTime(hoursToMeridian, hoursToInt);
            }
            catch (NumberFormatException e)
            {
                // Something is freaky with the hours, hmmmm....
                return false;
            }
        }
        return true;
    }

    /**
     * Indicates that a range of time is being specified.
     *
     * @return true only if the form state indicates a from and to time.
     */
    public boolean isRange()
    {
        return !incrementInMinutes.equals("0") && (isDailyMode() || isDayPerWeekMode());
    }

    /**
     * Indicates that the range of time specified spans across the full 24 hours in a day (which is indicated by the
     * computed hoursFrom in 24 hour time being equal to the computed hoursTo in 24 hour time).
     *
     * @return true only if we have specified a range, and that range spans across the 24 hours.
     */
    public boolean is24HourRange()
    {
        boolean result = false;
        if (isRange())
        {
            int hoursFromInt = Integer.parseInt(hoursFrom);
            int hoursToInt = Integer.parseInt(hoursTo);
            result = JiraUtils.get24HourTime(hoursFromMeridian, hoursFromInt) == JiraUtils.get24HourTime(hoursToMeridian, hoursToInt);
        }
        return result;
    }

    /**
     * Returns the cron string that the object was constructed with. This method does not guarantee that the returned
     * cron string is valid.
     *
     * @return unmodified cronString
     */
    public String getCronString()
    {
        return cronString;
    }

    /**
     * Will return true if the passed in cron string is not valid for the editor.
     *
     * @return true if the cron string can not be handled, false otherwise.
     */
    public boolean isAdvancedMode()
    {
        return ADVANCED_MODE.equals(mode);
    }

    /**
     * Will return true if the editors daily mode can handle the provided cron string.
     *
     * @return if we're in daily mode.
     */
    public boolean isDailyMode()
    {
        return DAILY_SPEC_MODE.equals(mode);
    }

    /**
     * Will return true if the editors day per week mode can handle the provided cron string.
     *
     * @return true if {@link #mode} is equal to {@link #DAYS_OF_WEEK_SPEC_MODE}, false otherwise
     */
    public boolean isDayPerWeekMode()
    {
        return DAYS_OF_WEEK_SPEC_MODE.equals(mode);
    }

    /**
     * Will return true if the editors days per month mode can handle the provided cron string.
     *
     * @return true if {@link #mode} is equal to {@link #DAYS_OF_MONTH_SPEC_MODE}, false otherwise
     */
    public boolean isDaysPerMonthMode()
    {
        return DAYS_OF_MONTH_SPEC_MODE.equals(mode);
    }

    /**
     * Returns true if {@link #isDaysPerMonthMode()} is true and the sub-mode of day of week per month has been
     * selected.
     */
    public boolean isDayOfWeekOfMonth()
    {
        return dayOfWeekOfMonth;
    }

    /**
     * Gets the day of month field specified in the cron string. Should be between 1-31 or L.
     */
    public String getDayOfMonth()
    {
        return dayOfMonth;
    }

    /**
     * Gets the minutes specified. Should be between 0-59.
     */
    public String getMinutes()
    {
        return minutes;
    }

    /**
     * Returns the lower bound of the hour range if this entry has a range.
     *
     * This end of the range is inclusive - e.g. if HoursFrom is 3PM and HoursTo is 5PM, the duration of the range
     * is 2 hours.
     *
     * @return the lower bound or null if this is not a range hour entry.
     */
    public String getHoursFrom()
    {
        return hoursFrom;
    }

    /**
     * Returns the upper bound of the hour range if this entry has a range.
     *
     * This end of the range is exclusive - e.g. if HoursFrom is 3PM and HoursTo is 5PM, the duration of the range
     * is 2 hours.
     *
     * @return the upper bound or null if this is not a range hour entry.
     */
    public String getHoursTo()
    {
        return hoursTo;
    }

    /**
     * Returns the meridian indicator @{link #AM} or @{link #PM} for the lower bound of a range entry.
     *
     * @return the meridian belonging to the lower bound hour or null if this is not a range entry.
     */
    public String getHoursFromMeridian()
    {
        return hoursFromMeridian;
    }

    /**
     * Returns the meridian indicator @{link #AM} or @{link #PM} for the upper bound of a range entry.
     *
     * @return the meridian belonging to the upper bound hour or null if this is not a range entry.
     */
    public String getHoursToMeridian()
    {
        return hoursToMeridian;
    }

    /**
     * Returns the single hour value for this entry if it is a run once entry.
     *
     * @return the hour value or null if this is not a run once hour entry.
     */
    public String getHoursRunOnce()
    {
        return hoursRunOnce;
    }

    /**
     * Returns the meridian indicator @{link #AM} or @{link #PM} for the entry if it is a run once entry.
     *
     * @return the meridian belonging single hour value or null if this is not a run once entry.
     */
    public String getHoursRunOnceMeridian()
    {
        return hoursRunOnceMeridian;
    }

    /**
     * Returns true if the passed in day has been specified, false otherwise.
     *
     * @param dayStr a string representing a day (e.g. 1-7).
     * @return true if the day has been specified, false otherwise.
     */
    public boolean isDaySpecified(String dayStr)
    {
        return specifiedDaysOfWeek != null && StringUtils.contains(specifiedDaysOfWeek, dayStr);
    }

    /**
     * Returns a number that represents the first, second third etc. day of the week in a month.
     *
     * @return the ordinal or null if this entry doesn't specify it.
     */
    public String getDayInMonthOrdinal()
    {
        return dayInMonthOrdinal;
    }

    /**
     * Returns all the days that have been specified in a comma separated list.
     *
     * @return string representing days (e.g. "1,2,3").
     */
    public String getSpecifiedDaysPerWeek()
    {
        return specifiedDaysOfWeek;
    }

    /**
     * Used to determine the total increment in minutes derived by the hour and minutes fields' increment parts.
     * An increment of 0 indicates no repetition based on the hours or minutes fields and will happen if the
     * repetition is once a day.
     *
     * @return a minute increment or "0"
     */
    public String getIncrementInMinutes()
    {
        return incrementInMinutes;
    }

    /**
     * The full cron string that may have been specified. This can come from the advanced tab where the user has
     * specified their own cron string. This does not validate the cron string.
     *
     * @param cronString a valid cron string.
     */
    public void setCronString(String cronString)
    {
        this.cronString = cronString;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public void setDayOfWeekOfMonth(boolean dayOfWeekOfMonth)
    {
        this.dayOfWeekOfMonth = dayOfWeekOfMonth;
    }

    public void setDayOfMonth(String dayOfMonth)
    {
        this.dayOfMonth = dayOfMonth;
    }

    public void setMinutes(String minutes)
    {
        this.minutes = minutes;
    }

    public void setHoursFrom(String hoursFrom)
    {
        this.hoursFrom = hoursFrom;
    }

    public void setHoursTo(String hoursTo)
    {
        this.hoursTo = hoursTo;
    }

    public void setHoursFromMeridian(String hoursFromMeridian)
    {
        this.hoursFromMeridian = hoursFromMeridian;
    }

    public void setHoursToMeridian(String hoursToMeridian)
    {
        this.hoursToMeridian = hoursToMeridian;
    }

    public void setHoursRunOnce(String hoursRunOnce)
    {
        this.hoursRunOnce = hoursRunOnce;
    }

    public void setHoursRunOnceMeridian(String hoursRunOnceMeridian)
    {
        this.hoursRunOnceMeridian = hoursRunOnceMeridian;
    }

    public void setSpecifiedDaysOfWeek(String specifiedDaysOfWeek)
    {
        this.specifiedDaysOfWeek = specifiedDaysOfWeek;
    }

    public void setDayInMonthOrdinal(String dayInMonthOrdinal)
    {
        this.dayInMonthOrdinal = dayInMonthOrdinal;
    }

    /**
     * Set the interval of repetition. "0" indicates there is no repetition.
     *
     * @param incrementInMinutes the interval or "0"
     */
    public void setIncrementInMinutes(String incrementInMinutes)
    {
        this.incrementInMinutes = incrementInMinutes;
    }

    /**
     * Looks up a single String value with the given prefixed key.
     *
     * @param paramPrefix prefix the parameter must have
     * @param key         key value to use for lookup.
     * @return the first String or null.
     */
    private String getParam(String paramPrefix, String key)
    {
        String[] paramArr = (String[]) params.get(paramPrefix + DOT + key);
        if (paramArr != null && paramArr.length == 1)
        {
            return paramArr[0];
        }
        return null;
    }

    public void setSeconds(String seconds)
    {
        this.seconds = seconds;
    }

    public String getSeconds()
    {
        return seconds;
    }
}
