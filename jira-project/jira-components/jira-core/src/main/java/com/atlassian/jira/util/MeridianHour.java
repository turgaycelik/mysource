package com.atlassian.jira.util;

import org.apache.log4j.Logger;

/**
 * Holder of a 12 hour time with a meridian indicator (am/pm).
 */
public final class MeridianHour
{
    private static final Logger log = Logger.getLogger(MeridianHour.class);

    private static final int MERIDIAN_HOURS = 12;

    private final int hour;
    private final String meridian;

    public MeridianHour(int hour, String meridian)
    {
        this.hour = hour;
        this.meridian = meridian;
    }

    public int getHour()
    {
        return hour;
    }

    public String getMeridian()
    {
        return meridian;
    }

    /**
     * Takes a 24 hour time and parses the 12 hour time with meridian indicator into a MeridianHour object.
     * <p/>
     * Be aware that 12:00 is 12pm (noon), 00:00 is 12am and 24:00 doesn't exist.
     *
     * @param twentyFourHour a time value in 24 hour format ("0" - "23").
     * @return a MeridianHour representing the given time or null on arse failure.
     */
    public static MeridianHour parseMeridianHour(String twentyFourHour)
    {
        int hour;
        String meridian = JiraUtils.AM; // default to AM
        try
        {
            hour = Integer.parseInt(twentyFourHour);
            if (hour < 0 || hour > 23)
            {
                log.debug("The hour of the cron entry is out of range (0-23): " + twentyFourHour);
                return null;
            }
            else if (hour == 0)
            {
                hour = 12;
            }
            else if (hour == 12)
            {
                meridian = JiraUtils.PM;
            }
            else if (hour >= MERIDIAN_HOURS)
            {
                meridian = JiraUtils.PM;
                hour -= MERIDIAN_HOURS;
            }

            return new MeridianHour(hour, meridian);
        }
        catch (NumberFormatException nfe)
        {
            log.debug("The hour of the cron entry must be an integer, instead it is: " + twentyFourHour);
        }
        return null;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MeridianHour that = (MeridianHour) o;

        if (hour != that.hour)
        {
            return false;
        }
        if (meridian != null ? !meridian.equals(that.meridian) : that.meridian != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = hour;
        result = 31 * result + (meridian != null ? meridian.hashCode() : 0);
        return result;
    }
}
