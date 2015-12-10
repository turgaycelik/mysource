package com.atlassian.jira.scheduler.cron;

import java.util.Calendar;
import java.util.Date;

/**
 * From <a href="http://extranet.atlassian.com/display/JIRADEV/Quartz+notification+scheduler+improvements+Spec">
 * the extranet spec page</a>:<br /><br />
 * <p/>
 * There are a number of things a cronspec cannot handle cleanly when converting a arbitrary fixed millisecond repeat
 * from a specific DateTime into a cronspec. Things such as "every 15 min from 9.53am Jan 23rd 2006" (closest cronspec
 * would be "every 15min").<br /><br />
 * <p/>
 * Basic parsing rules for the algorithm are that the main Unit that will be used is the largest unit it is close to
 * (minutes, hours, days, week, months) and then rounded to the nearest neighbour that is a denominator of the next
 * largest unit. So an unit of 5 hours is specified in hours, but as 5 is not a denominator of 24 it must be rounded
 * to 4 or 6. Similarly 15 hours would be rounded down to 12 and 20 hours would be rounded up to 24 (daily).<br /><br />
 * <p/>
 * Once the time period is specified as a number of days there are some fairly intractable issues. Firstly, the number
 * of days in a week - 7 - is prime and has no denominators apart from 1 and itself, and months may have 28, 29, 30 or
 * 31 days so exact arithmetic is not possible. For these periods we will need to round periods above 7 days and up to
 * 15 days down to 7 days, and of over 15 days to one month. Multi-month periods can be supported in the 2, 3, 4, 6 and
 * 12 month periods. We will not support multi-year periods and any currently specified will be rounded down to a single
 * year period.
 * <p/>
 */
public class SimpleToCronTriggerConverter
{
    //Time units
    private static final long SECONDS = 1000 /* x MILLISECONDS */;
    private static final long MINUTES = 60 * SECONDS;
    private static final long HOURS = 60 * MINUTES;
    private static final long DAYS = 24 * HOURS;
    private static final long MONTHS = 28 * DAYS;
    private static final long YEARS = 12 * MONTHS;
    private static final long[] TIME_UNITS = new long[]{SECONDS, MINUTES, HOURS, DAYS, MONTHS, YEARS};

    //Range of frequencies that should be rounded to 'per week'
    private static final long WEEK_FREQ_LOWER_BOUND = 4 * DAYS;
    private static final long WEEK_FREQ_UPPER_BOUND = 10 * DAYS;

    //Cron element positions
    private static final int CRON_SECONDS = 0;
    private static final int CRON_MINUTES = 1;
    private static final int CRON_HOURS = 2;
    private static final int CRON_DAYOFMONTH = 3;
    private static final int CRON_MONTH = 4;
    private static final int CRON_DAYOFWEEK = 5;
    private static final String CRON_WILDCARD = "*";
    private static final String CRON_NOT_APPLICABLE = "?";

    private static final int ZERO_BASED = 0;
    private static final int ONE_BASED = 1;

    /**
     * Converts trigger data taken from the database entries into a
     * <a href="http://quartz.sourceforge.net/javadoc/org/quartz/CronTrigger.html">quartz CronTrigger String</a>.
     * Note that this conversion is sometimes lossy. Not all inputs can result
     * in an equivalent cron string such that the triggers will fire at the same
     * time.
     *
     * @param nextFireDate a time a trigger will next fire
     * @param intervalMs   a time delay in milliseconds between trigger firing
     * @return a {@link ConversionResult} which contains composed of the following cron fields:<br />
     *         <strong>SECONDS MINUTES HOURS DAY_OF_MONTH MONTH DAY_OF_WEEK</strong>
     */
    public ConversionResult convertToCronString(Date nextFireDate, long intervalMs)
    {
        Calendar nextFire = Calendar.getInstance();
        nextFire.setTime(nextFireDate);
        nextFire.set(Calendar.SECOND, 0);
        //get a multiple of 5 for the minutes value. This means more triggers will be
        //converted to a format that the Cron-Editor understands.
        nextFire.set(Calendar.MINUTE, (nextFire.get(Calendar.MINUTE)/5)*5);

        long baseTimeUnit = determineBaseTimeUnit(intervalMs);
        long roundedInterval = roundInterval(intervalMs, baseTimeUnit);
        boolean hasLoss = (roundedInterval * baseTimeUnit != intervalMs) || baseTimeUnit > (7 * DAYS);

        String[] cronArray = new String[6];

        //Special case: if (4 Days < frequency < 10 Days) round to one week and use DAY_OF_WEEK cron element
        //ex. 0 30 12 ? * 3     (every [MON|TUE|WED|THU|FRI|SAT|SUN])
        if (roundedInterval * baseTimeUnit > WEEK_FREQ_LOWER_BOUND && roundedInterval * baseTimeUnit < WEEK_FREQ_UPPER_BOUND)
        {
            cronArray[CRON_SECONDS] = String.valueOf(nextFire.get(Calendar.SECOND));
            cronArray[CRON_MINUTES] = String.valueOf(nextFire.get(Calendar.MINUTE));
            cronArray[CRON_HOURS] = String.valueOf(nextFire.get(Calendar.HOUR_OF_DAY));
            cronArray[CRON_DAYOFMONTH] = CRON_NOT_APPLICABLE;
            cronArray[CRON_MONTH] = CRON_WILDCARD;
            cronArray[CRON_DAYOFWEEK] = String.valueOf(nextFire.get(Calendar.DAY_OF_WEEK));
        }

        //ex. 0/2 * * * * ?     (every # seconds)
        else if (baseTimeUnit == SECONDS)
        {
            cronArray[CRON_SECONDS] = makeIncrementalCronElement(nextFire.get(Calendar.SECOND), roundedInterval, ZERO_BASED);
            cronArray[CRON_MINUTES] = CRON_WILDCARD;
            cronArray[CRON_HOURS] = CRON_WILDCARD;
            cronArray[CRON_DAYOFMONTH] = CRON_WILDCARD;
            cronArray[CRON_MONTH] = CRON_WILDCARD;
            cronArray[CRON_DAYOFWEEK] = CRON_NOT_APPLICABLE;
        }

        //ex. 30 0/15 * * * ?   (every # minutes)
        else if (baseTimeUnit == MINUTES)
        {
            cronArray[CRON_SECONDS] = String.valueOf(nextFire.get(Calendar.SECOND));
            cronArray[CRON_MINUTES] = makeIncrementalCronElement(nextFire.get(Calendar.MINUTE), roundedInterval, ZERO_BASED);
            cronArray[CRON_HOURS] = CRON_WILDCARD;
            cronArray[CRON_DAYOFMONTH] = CRON_WILDCARD;
            cronArray[CRON_MONTH] = CRON_WILDCARD;
            cronArray[CRON_DAYOFWEEK] = CRON_NOT_APPLICABLE;
        }

        //ex. 0 0 1 * * ?       (every # hours)
        else if (baseTimeUnit == HOURS)
        {
            cronArray[CRON_SECONDS] = String.valueOf(nextFire.get(Calendar.SECOND));
            cronArray[CRON_MINUTES] = String.valueOf(nextFire.get(Calendar.MINUTE));
            cronArray[CRON_HOURS] = makeIncrementalCronElement(nextFire.get(Calendar.HOUR_OF_DAY), roundedInterval, ZERO_BASED);
            cronArray[CRON_DAYOFMONTH] = CRON_WILDCARD;
            cronArray[CRON_MONTH] = CRON_WILDCARD;
            cronArray[CRON_DAYOFWEEK] = CRON_NOT_APPLICABLE;
        }

        //ex. 0 30 12 1/2 * ?   (every # days of the month)
        else if (baseTimeUnit == DAYS)
        {
            cronArray[CRON_SECONDS] = String.valueOf(nextFire.get(Calendar.SECOND));
            cronArray[CRON_MINUTES] = String.valueOf(nextFire.get(Calendar.MINUTE));
            cronArray[CRON_HOURS] = String.valueOf(nextFire.get(Calendar.HOUR_OF_DAY));
            cronArray[CRON_DAYOFMONTH] = makeIncrementalCronElement(nextFire.get(Calendar.DAY_OF_MONTH), roundedInterval, ONE_BASED);
            cronArray[CRON_MONTH] = CRON_WILDCARD;
            cronArray[CRON_DAYOFWEEK] = CRON_NOT_APPLICABLE;
        }

        //ex. 0 0 12 1 1/6 ?    (every # months)
        else if (baseTimeUnit == MONTHS)
        {
            cronArray[CRON_SECONDS] = String.valueOf(nextFire.get(Calendar.SECOND));
            cronArray[CRON_MINUTES] = String.valueOf(nextFire.get(Calendar.MINUTE));
            cronArray[CRON_HOURS] = String.valueOf(nextFire.get(Calendar.HOUR_OF_DAY));
            cronArray[CRON_DAYOFMONTH] = String.valueOf(nextFire.get(Calendar.DAY_OF_MONTH));
            cronArray[CRON_MONTH] = makeIncrementalCronElement(nextFire.get(Calendar.MONTH) + 1, roundedInterval, ONE_BASED);
            cronArray[CRON_DAYOFWEEK] = CRON_NOT_APPLICABLE;
        }

        //ex. 0 0 9 1 1 ?       (once per year)
        else if (baseTimeUnit == YEARS)
        {
            cronArray[CRON_SECONDS] = String.valueOf(nextFire.get(Calendar.SECOND));
            cronArray[CRON_MINUTES] = String.valueOf(nextFire.get(Calendar.MINUTE));
            cronArray[CRON_HOURS] = String.valueOf(nextFire.get(Calendar.HOUR_OF_DAY));
            cronArray[CRON_DAYOFMONTH] = String.valueOf(nextFire.get(Calendar.DAY_OF_MONTH));
            cronArray[CRON_MONTH] = (String.valueOf(nextFire.get(Calendar.MONTH) + 1));
            cronArray[CRON_DAYOFWEEK] = CRON_NOT_APPLICABLE;
        }
        else
        {
            throw new RuntimeException("Invalid base time unit: " + baseTimeUnit + "ms");
        }

        StringBuilder cronString = new StringBuilder();
        for (final String aCronArray : cronArray)
        {
            cronString.append(aCronArray);
            cronString.append(" ");
        }

        return new ConversionResult(hasLoss, cronString.toString());
    }

    /**
     * Make a cron element of the form 'START_TIME''/''FREQUENCY'.
     * <p/>
     * In order for the trigger to fire the right number of times, we must pick the earliest possible start time where
     * START_TIME + N * FREQUENCY = TARGET_TIME, where N is some positive integer, holds true.
     *
     * @param targetTime The time that the trigger should fire on, parsed from the nextFire quartz trigger field
     * @param frequency  The delay between trigger firings
     * @param base The base of the cron timeunit: 0 or 1. Seconds [0-59], minutes [0-59] & hours [0-23] are zero-based.
     *                  Day of Month [1-31], Months [1-12] and Days of Week [1-7] are one-based.
     * @return a String of the form "s/f". This represents START_TIME/FREQUENCY.
     */
    protected String makeIncrementalCronElement(int targetTime, long frequency, int base)
    {
        if (base != ZERO_BASED && base != ONE_BASED)
        {
            throw new RuntimeException("Invalid base (" + base + ") for cron element.");
        }

        if (frequency == 1)
        {
            return CRON_WILDCARD;
        }

        long cronStart;
        if (targetTime >= frequency)
        {
            cronStart = (targetTime % frequency);

            if (base == ONE_BASED && cronStart == 0)
            {
                cronStart = frequency;
            }
        }
        else
        {
            cronStart = targetTime;
        }

        return cronStart + "/" + frequency;
    }

    /**
     * Round the interval to the nearest time unit multiple that is a denominator of the succeeding time unit.
     * That is, find the set of intervals in milliseconds that satisfies the following:
     * <ul>
     * <li>divides the next largest time unit (so we can use cron's '/' operator);</li>
     * <li>is a multiple of the base time unit;</li>
     * </ul>
     * and select the interval length that is nearest the length of the supplied interval.</li>
     * </ul>
     *
     * @param intervalMs   the original interval in milliseconds
     * @param baseTimeUnit the time unit that is the major component of the cron string (e.g. SECONDS, HOURS, etc.)
     * @return the rounded interval length
     */
    protected long roundInterval(long intervalMs, long baseTimeUnit)
    {
        //if the base time unit is greater than the interval we're at the limit of our precision
        if (baseTimeUnit >= intervalMs)
        {
            return 1;
        }

        //if the base time unit is a year then return a year - We don't "do" multi-year frequencies...
        if (baseTimeUnit == YEARS)
        {
            return 1;
        }

        //otherwise calculate the nearest quantity of the base time unit that is a factor of it's succeeding time unit
        long nextTimeUnit = getSucceedingTimeUnit(baseTimeUnit);

        // We start our divisor at 2 and then work our way up making the potentialFactor smaller and smaller, until
        // it is smaller or equal to the interval.
        for (int i = 2; ; i++)
        {
            // We don't want our division to round, it makes no sense we are looking for factors
            if (nextTimeUnit % i == 0)
            {
                long potentialFactor = nextTimeUnit / i;

                //find a factor that: a) divides the next largest time unit (so we can use cron's '/' operator);
                //                    b) is a multiple of the base time unit; and
                //                    c) is nearest the previously defined interval.
                if (potentialFactor % baseTimeUnit == 0 && intervalMs >= potentialFactor)
                {
                    return potentialFactor / baseTimeUnit;
                }
            }
            if (i > nextTimeUnit / baseTimeUnit)
            {
                throw new RuntimeException("calculateRoundedFrequency malfunction for nextTimeUnit=" + nextTimeUnit + " baseTimeUnit=" + baseTimeUnit);
            }
        }

    }

    /**
     * Given a unit of time (e.g. MINUTES), this method returns the next unit of time from the unit of time sequence
     * (e.g. HOURS).
     *
     * @param timeUnit a unit of time, defined at the top of this class
     * @return the next unit of time in the sequence, or the last unit of time (YEARS) if we're already at the end of
     *         the sequence
     */
    protected long getSucceedingTimeUnit(long timeUnit)
    {
        if (timeUnit >= YEARS)
        {
            throw new IllegalArgumentException("Years are the upper limit of our subscription service granularity.");
        }

        for (int i = 0; i < TIME_UNITS.length - 1; i++) //don't step off the end
        {
            if (timeUnit == TIME_UNITS[i])
            {
                return TIME_UNITS[i + 1];
            }
        }

        throw new IllegalArgumentException("Invalid time unit.");
    }

    /**
     * Return the time unit (SECONDS, MINUTES, MONTHS etc.) that the supplied interval (ms) is closest to.
     *
     * @param intervalMs the supplied interval in milliseconds
     * @return the unit of time that matches the supplied interval the closest
     */
    protected long determineBaseTimeUnit(long intervalMs)
    {
        for (int i = 0; i < TIME_UNITS.length - 1; i++)
        {
            long nextTimeUnit = TIME_UNITS[i + 1];

            if (intervalMs > nextTimeUnit)
            {
                //find largest time unit that the interval is greater than
                continue;
            }

            long timeUnit = TIME_UNITS[i];

            //determine which time unit we are closer to
            return (nextTimeUnit - intervalMs < intervalMs - timeUnit) ? nextTimeUnit : timeUnit;
        }

        //interval is greater than maximum time unit, return max time unit
        return TIME_UNITS[TIME_UNITS.length - 1];
    }

}
