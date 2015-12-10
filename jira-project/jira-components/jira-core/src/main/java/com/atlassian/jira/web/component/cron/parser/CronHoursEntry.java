package com.atlassian.jira.web.component.cron.parser;

import com.atlassian.jira.util.MeridianHour;
import com.google.common.collect.ImmutableSet;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Represents the hours part of a cron string. This class is responsible for parsing and validating the hours entry.
 * The {@link #isValid()} method refers only to what is supported by the cron editor for the hours entry.
 * <p/>
 * Valid hours means a numerical hour (or range of the form x-y) with an optional trailing "/1", "/2" or "/3" to
 * indicate the repeat increment in hours. This expects that numeric hours to be in 24 hour time format.
 * <p/>
 * The hours and meridian attributes depend on whether this is a range entry or a "run once" entry. For ranges, use
 * the "from" and "to" methods.
 */
public class CronHoursEntry
{
    private static final Logger log = Logger.getLogger(CronHoursEntry.class);

    /**
     * cronEntry should only contain legal characters are '/', '*', '-', and digit
     */
    private static final String REGEX_VALID = "[\\d*/-]+";

    /**
     * Flag to indicate no increment part is present.
     */
    private static final int NO_INCREMENT_PART = -1;

    /**
     * The set of hour increments that we accept. This is derived from the minimum requirements of the cron spec editor.
     */
    private static final Collection<Integer> ACCEPTED_HOUR_INCREMENTS = ImmutableSet.of(NO_INCREMENT_PART, 1, 2, 3);

    static final String INCREMENT_DELIMITER = "/";
    static final String RANGE_DELIMITER = "-";

    private static final MeridianHour NULL_MERIDIAN_HOUR = new MeridianHour(NO_INCREMENT_PART, null);

    private MeridianHour fromMeridianHour = NULL_MERIDIAN_HOUR;
    private MeridianHour toMeridianHour = NULL_MERIDIAN_HOUR;

    private MeridianHour runOnceMeridianHour = NULL_MERIDIAN_HOUR;

    private int increment = NO_INCREMENT_PART;
    private boolean valid = true;

    /**
     * Parses the given value and establishes state based on this.
     * @param cronEntry the hours field of a cron string.
     */
    public CronHoursEntry(String cronEntry)
    {
        if (cronEntry == null)
        {
            throw new IllegalArgumentException("Can not create a cron entry from a null value.");
        }
        parseEntry(cronEntry);
    }

    /**
     * Returns true only if the hours entry is valid with respect to the editor.
     * @return true only if the editor can handle this hour part.
     */
    public boolean isValid()
    {
        return valid && ACCEPTED_HOUR_INCREMENTS.contains(new Integer(increment));
    }

    /**
     * Returns the lower bound of the hour range if this entry has a range.
     *
     * This end of the range is inclusive - e.g. if HoursFrom is 3PM and HoursTo is 5PM, the duration of the range
     * is 2 hours.
     *
     * @return the lower bound or -1 if this is not a range hour entry.
     */
    public int getFrom()
    {
        return fromMeridianHour.getHour();
    }

    /**
     * Returns the upper bound of the hour range if this entry has a range.
     *
     * This end of the range is exclusive - e.g. if HoursFrom is 3PM and HoursTo is 5PM, the duration of the range
     * is 2 hours.
     *
     * @return the upper bound or -1 if this is not a range hour entry.
     */
    public int getTo()
    {
        return toMeridianHour.getHour();
    }

    /**
     * Returns the meridian indicator @{link #AM} or @{link #AM} for the lower bound of a range entry.
     *
     * @return the meridian belonging to the lower bound hour or null if this is not a range entry.
     */
    public String getFromMeridian()
    {
        return fromMeridianHour.getMeridian();
    }

    /**
     * Returns the meridian indicator @{link #AM} or @{link #AM} for the upper bound of a range entry.
     *
     * @return the meridian belonging to the upper bound hour or null if this is not a range entry.
     */
    public String getToMeridian()
    {
        return toMeridianHour.getMeridian();
    }

    /**
     * Returns the single hour value for this entry if it is a run once entry.
     *
     * @return the hour value or -1 if this is not a run once hour entry.
     */
    public int getRunOnce()
    {
        return runOnceMeridianHour.getHour();
    }

    /**
     * Returns the meridian indicator @{link #AM} or @{link #AM} for the entry if it is a run once entry.
     *
     * @return the meridian belonging single hour value or null if this is not a run once entry.
     */
    public String getRunOnceMeridian()
    {
        return runOnceMeridianHour.getMeridian();
    }

    /**
     * Returns the increment or step size in hours or -1 if this entry has no increment.
     *
     * @return the period of repetition in hours.
     */
    public int getIncrement()
    {
        return increment;
    }

    /**
     * Indicates if this entry has an increment.
     *
     * @return true only if the entry has an increment part specified.
     */
    public boolean hasIncrement()
    {
        return increment != NO_INCREMENT_PART;
    }

    public boolean isRunOnce()
    {
        return !NULL_MERIDIAN_HOUR.equals(runOnceMeridianHour);
    }

    /**
     * Sets the state of this entry based on the given cron entry including the validity.
     *
     * @param cronEntry the cron entry part for hours.
     */
    private void parseEntry(String cronEntry)
    {
        if (!cronEntry.matches(REGEX_VALID))
        {
            valid = false;
        }
        else
        {
            // a '*' denotes "every hour", so it has an increment of 1, and starts at 12 AM and finishes at 12 AM
            if ("*".equals(cronEntry))
            {
                this.increment = 1;
                this.fromMeridianHour = parseMeridianHour("0");
                this.toMeridianHour = this.fromMeridianHour;
                return;
            }

            int slashIndex = cronEntry.indexOf(INCREMENT_DELIMITER);
            if (slashIndex >= 0)
            {
                String incrementStr = cronEntry.substring(slashIndex + 1, cronEntry.length());
                try
                {
                    this.increment = Integer.parseInt(incrementStr);
                }
                catch (NumberFormatException nfe)
                {
                    log.debug("The increment portion of the hour cron entry must be an integer.");
                    valid = false;
                }
                //Chop this off, we don't need it anymore
                cronEntry = cronEntry.substring(0, slashIndex);
            }

            int dashIndex = cronEntry.indexOf(RANGE_DELIMITER);
            if (dashIndex >= 0)
            {
                String fromStr = cronEntry.substring(0, dashIndex);
                this.fromMeridianHour = parseMeridianHour(fromStr);

                String toStr = cronEntry.substring(dashIndex + 1, cronEntry.length());
                // JRA-13503: since toMeridianHour is exclusive, but cron expressions are inclusive,
                // we need to increment the "to" hour in the cron expression by 1 to get the correct representation
                this.toMeridianHour = parseMeridianHour(incrementHourByOne(toStr));
            }
            // if we have specified a "24-range" e.g. 4am - 4am, the cron entry will not contain the range delimiter,
            // but it will have an increment.
            else if (hasIncrement())
            {
                this.fromMeridianHour = parseMeridianHour(cronEntry);
                this.toMeridianHour = parseMeridianHour(cronEntry);
            }
            else
            {
                runOnceMeridianHour = parseMeridianHour(cronEntry);
            }
        }
    }

    private MeridianHour parseMeridianHour(String twentyFourHour)
    {
        MeridianHour meridianHour;
        // '*' means any hour, so default to 12AM
        if ("*".equals(twentyFourHour))
        {
            twentyFourHour = "0";
        }
        meridianHour = MeridianHour.parseMeridianHour(twentyFourHour);
        if (meridianHour == null)
        {
            valid = false;
            meridianHour = NULL_MERIDIAN_HOUR;
        }
        return meridianHour;
    }

    private String incrementHourByOne(String hour)
    {
        // don't use modulo operator here, because if the hour has
        // exceeded the valid range, using modulo will change it
        int h = Integer.parseInt(hour);
        h = (h == 23) ? 0 : h + 1;
        return "" + h;
    }

}
