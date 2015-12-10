package com.atlassian.jira.web.component.cron.parser;

import com.atlassian.core.util.map.EasyMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parser for the day of week part of a cron string. This class is responsible for parsing and validating the day of
 * week entry. The {@link #isValid()} method refers only to what is supported by the cron editor for the day of week entry.
 * <p/>
 * Valid day of week means a numerical day of week (1-7) or string representation (MON-SUN) which can be separated
 * by a ',' to indicate a list of days. You can also specify a single day (2) followed by '#' and a number (either
 * 1, 2, 3, or 4). This represents the first, second, third, or fourth week in the month for that day (e.g. 2#2 means
 * the second Monday of the month). This can also be a single day (2) followed by the character 'L' which indicates
 * the last of that day in the month (e.g. 2L means the last monday in the month).
 */
public class CronDayOfWeekEntry
{

    private static final Logger log = Logger.getLogger(CronDayOfWeekEntry.class);

    private static final String ORDINAL_SEPARATOR = "#";
    private static final String LIST_SEPARATOR = ",";

    private static final String LAST = "L";

    private static final Map<String, String> VALID_DAYS_MAP = EasyMap.build("MON", "2", "TUE", "3", "WED", "4", "THU", "5", "FRI", "6", "SAT", "7", "SUN", "1");
    private static final Collection<String> VALID_NUMERIC_ORDINAL_VALUES = ImmutableSet.of("1", "2", "3", "4");

    /** All the characters that are derived from valid day values, the ordinal values, the separators. */
    private static final String VALID_CHARACTERS = "MONTUEWEDTHUFRISATSUN1234567L#,?*";

    private boolean valid = true;
    private String ordinal = null;
    private final List specifiedDays;

    /**
     * Parses the given cron entry.
     *
     * @param dayOfWeekEntry e.g. MON#2, 2#2 or MON,WED or 1,2,3 or 2L.
     */
    public CronDayOfWeekEntry(String dayOfWeekEntry)
    {
        specifiedDays = new ArrayList();
        parseEntry(dayOfWeekEntry);
    }

    /**
     * Will tell you if a day has been specified in the cron string.
     *
     * @param dayStr can be any of 1-7, MON-SUN, mon-sun.
     * @return true if the cron spec specified the passed in day, false otherwise.
     */
    public boolean isDaySpecified(String dayStr)
    {
        String day = getDayForValue(dayStr);
        return day != null && specifiedDays.contains(day);
    }

    /**
     * Returns a number that represents the first, second third etc. day of the week in a month.
     *
     * @return the ordinal or -1 if this entry doesn't specify it.
     */
    public String getDayInMonthOrdinal()
    {
        return ordinal;
    }

    /**
     * Will create a comma separated list of the days' numeric values that are specifed by the cron string.
     *
     * @return string representing days (e.g. "1,2,3").
     */
    public String getDaysAsNumbers()
    {
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Iterator iterator = specifiedDays.iterator(); iterator.hasNext(); i++)
        {
            String day = (String) iterator.next();
            result.append(day);
            if (i + 1 < specifiedDays.size())
            {
                result.append(",");
            }
        }
        return result.toString();
    }

    /**
     * Returns true if the editor can handle the day of week field entry.
     */
    public boolean isValid()
    {
        return valid;
    }

    private void parseEntry(String dayOfWeekEntry)
    {
        if (StringUtils.isBlank(dayOfWeekEntry))
        {
            log.debug("Tried to create a CronDayOfWeek with empty or null string.");
            valid = false;
        }
        else if (!StringUtils.containsOnly(dayOfWeekEntry.toUpperCase(), VALID_CHARACTERS))
        {
            log.debug("Tried to create a CronDayOfWeek with invalid characters: " + dayOfWeekEntry);
            valid = false;
        }
        else
        {
            dayOfWeekEntry = dayOfWeekEntry.toUpperCase();
            // This is the case where we have an ordinal value and only one day specified
            if (StringUtils.contains(dayOfWeekEntry, ORDINAL_SEPARATOR))
            {
                parseOrdinalValue(dayOfWeekEntry);
            }
            // This is the case where we only have a comma separated list of days
            else if (StringUtils.contains(dayOfWeekEntry, LIST_SEPARATOR))
            {
                parseDaysOfWeek(dayOfWeekEntry);
            }
            else if (StringUtils.contains(dayOfWeekEntry, LAST))
            {
                parseLastDayOfWeek(dayOfWeekEntry);
            }
            else {
                specifiedDays.add(dayOfWeekEntry);
            }
        }
    }

    private void parseLastDayOfWeek(String dayOfWeekEntry)
    {
        if (!dayOfWeekEntry.endsWith(LAST))
        {
            log.debug("The L character which specifies last is not at the end of the day of week string.");
            valid = false;
        }
        else
        {
            ordinal = LAST;
            String dayOfWeekStr = dayOfWeekEntry.substring(0, dayOfWeekEntry.length() - 1);
            String dayOfWeek = getDayForValue(dayOfWeekStr);
            if (dayOfWeek != null)
            {
                specifiedDays.add(dayOfWeek);
            }
            else
            {
                log.debug("The value specfied as a day of week was invalid: " + dayOfWeekStr);
                valid = false;
            }
        }
    }

    private void parseDaysOfWeek(String dayOfWeekEntry)
    {
        String[] days = StringUtils.split(dayOfWeekEntry, LIST_SEPARATOR);
        if (days == null || days.length > 7)
        {
            log.debug("The days of week has specified more than 7, this is not valid: " + dayOfWeekEntry);
            valid = false;
        }
        else
        {
            for (String dayStr : days)
            {
                String day = getDayForValue(dayStr);
                if (day != null)
                {
                    specifiedDays.add(day);
                }
                else
                {
                    log.debug("A day of week was specified that can not be mapped: " + dayStr);
                    valid = false;
                    break;
                }
            }
        }
    }

    private void parseOrdinalValue(String dayOfWeekEntry)
    {
        String[] strings = StringUtils.split(dayOfWeekEntry, ORDINAL_SEPARATOR);
        if (strings == null || strings.length != 2)
        {
            log.debug("The ordinal value specifed was not of the correct form: " + dayOfWeekEntry);
            valid = false;
        }
        else
        {
            // The first string is the day
            String dayString = getDayForValue(strings[0]);
            // Only continue if we can map the day string to a day of week
            if (dayString != null)
            {
                specifiedDays.add(dayString);

                // The second is the ordinal value
                String secondString = strings[1].toUpperCase();
                if (VALID_NUMERIC_ORDINAL_VALUES.contains(secondString))
                {
                    ordinal = secondString;
                }
                else
                {
                    log.debug("invalid ordinal value " + ordinal);
                    valid = false;
                }
            }
        }
    }

    private String getDayForValue(String dayString)
    {
        if (VALID_DAYS_MAP.values().contains(dayString.toUpperCase()))
        {
            return dayString;
        }
        else if (VALID_DAYS_MAP.containsKey(dayString))
        {
            return VALID_DAYS_MAP.get(dayString);
        }
        log.debug("Unable to resolve a day of week for the string: " + dayString);
        valid = false;
        return null;
    }
}
