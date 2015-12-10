package com.atlassian.jira.web.component.cron.parser;

import com.google.common.collect.ImmutableSet;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Represents the minutes part of a cron string. This class is responsible for parsing and validating this entry.
 * The {@link #isValid()} method refers only to what is supported by the cron editor for the minutes entry.
 * <p/>
 * Valid minutes include only those that are either: a multiple of 5 between 0 and 55 inclusive; or "0/15" or "0/30"
 * meaning every 15 minutes or every 30 minutes.
 */
public class CronMinutesEntry
{
    private static final Logger log = Logger.getLogger(CronMinutesEntry.class);

    static final String MINUTE_INCREMENT_SEPARATOR = "/";

    /**
     * cronEntry should only contain legal characters '/', and digit
     */
    private static final String REGEX_VALID = "[\\d/]+";

    private static final int UNSET_FLAG = -1;

    private static final int MAX_MINUTES = 59;

    /** we only support minute values that are divisible by this. */
    private static final int MINUTE_FACTOR = 5;

    /**
     * We are only supporting these minute increments.
     */
    private static final Collection<Integer> VALID_INCREMENT_IN_MINUTES = ImmutableSet.of(15, 30);
    private int runOnce = UNSET_FLAG;
    private boolean valid = true;

    private int increment = UNSET_FLAG;

    /**
     * Parses the given minute field into this CronMinutesEntry state.
     * @param cronEntry the minute field of a cron string.
     */
    public CronMinutesEntry(String cronEntry)
    {
        if (cronEntry == null)
        {
            throw new IllegalArgumentException("Can not create a cron entry from a null value.");
        }
        parseEntry(cronEntry);
    }

    /**
     * Indicates that the minute field is able to be handled by the editor, concerns
     * both the minute value and the optional increment value.
     * @return true only if the minute field is valid.
     */
    public boolean isValid()
    {
        boolean validIncrement = (increment == UNSET_FLAG) || (VALID_INCREMENT_IN_MINUTES.contains(new Integer(increment)) && runOnce == 0);

        return valid && runOnce <= MAX_MINUTES && runOnce >= 0 && (runOnce % MINUTE_FACTOR == 0) && validIncrement;
    }


    /**
     * The minute value for a single fire time.
     * @return the minute value.
     */
    public int getRunOnce()
    {
        return runOnce;
    }

    private void parseEntry(String cronEntry)
    {

        if (!cronEntry.matches(REGEX_VALID))
        {
            valid = false;
        }
        else
        {
            int separator = cronEntry.indexOf(MINUTE_INCREMENT_SEPARATOR);
            if (separator >= 0)
            {
                String incrementStr = cronEntry.substring(separator + 1, cronEntry.length());
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
                cronEntry = cronEntry.substring(0, separator);
            }
            try
            {
                this.runOnce = Integer.parseInt(cronEntry);
            }
            catch (NumberFormatException nfe)
            {
                log.debug("The minute of the cron entry must be an integer, instead it is: " + cronEntry);
                valid = false;
            }
        }
    }


    public int getIncrement()
    {
        return increment;
    }

    public boolean hasIncrement()
    {
        return increment > 0;
    }
}
