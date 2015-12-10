package com.atlassian.jira.web.component.cron.parser;

import java.util.StringTokenizer;

import com.atlassian.jira.web.component.cron.CronEditorBean;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a cron string with accessor methods to get at the individual fields. This is only used to back our
 * Cron editor. This will tell you via the {@link #isValidForEditor()} method whether the cron string this is constructed
 * with will be parseable via the editor. To populate the editor use the {@link #getCronEditorBean()} method.
 * <p/>
 * There are four modes that the editor supports:
 * <ol>
 * <li>Daily Mode</li>
 * <li>Days Per Week Mode</li>
 * <li>Days Per Month Mode</li>
 * <li>Advanced Mode</li>
 * </ol>
 * <p/>
 * If a cron string is not valid for the editor then the only available mode will be the advanced mode and the editor
 * state methods (e.g. {@link #getDayOfMonth()} , {@link #getHoursEntry()} ) will return details of the default state
 * represented by {@link #DEFAULT_CRONSTRING} as they are not able to represent the advanced cron string.
 * <p/>
 * The validation that this object performs is in the context of valid cron strings that will fit into the editor. This
 * object does not validate that the over all string is a valid cron string.
 */
public class CronExpressionParser
{
    /**
     * Cron string that puts the editor into the default state.
     */
    public static final String DEFAULT_CRONSTRING = "0 0 1 ? * *";

    private static final String VALID_DAY_OF_MONTH = "0123456789L";
    private static final String WILDCARD = "*";
    private static final String NOT_APPLICABLE = "?";
    private static final int MINUTES_IN_HOUR = 60;
    private static final int NUM_CRON_FIELDS = 7;
    private static final int NUM_CRON_FIELDS_NO_YEAR = NUM_CRON_FIELDS - 1;

    private CronMinutesEntry minutesEntry;
    private CronHoursEntry hoursEntry;
    private String dayOfMonth;
    private String month;
    private String daysOfWeek;
    private CronDayOfWeekEntry daysOfWeekEntry;
    private String year;
    private String cronString;
    private boolean isDaily;
    private boolean isDayPerWeek;
    private boolean isDaysPerMonth;
    private boolean isAdvanced;
    private boolean validForEditor;
    private String seconds; // only used for advanced mode


    /**
     * Creates a parser in default state using {@link #DEFAULT_CRONSTRING}.
     */
    public CronExpressionParser()
    {
        this(DEFAULT_CRONSTRING);
    }

    /**
     * Parses the given cronString to establish the state of this CronExpressionParser.
     *
     * @param cronString the cron string to parse.
     */
    public CronExpressionParser(String cronString)
    {
        this.cronString = cronString;
        parseAndValidateCronString(this.cronString);
    }

    /**
     * Will provide the {@link com.atlassian.jira.web.component.cron.CronEditorBean} which represents the state of the
     * form for the configured cron string.
     *
     * @return the bean
     */
    public CronEditorBean getCronEditorBean()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setCronString(cronString);

        cronEditorBean.setSeconds(seconds);

        cronEditorBean.setDayOfMonth(dayOfMonth);
        cronEditorBean.setIncrementInMinutes(Integer.toString(getIncrementInMinutes()));

        // Either set the time as runOnce or the from/to
        if (getIncrementInMinutes() == 0)
        {
            cronEditorBean.setHoursRunOnce(Integer.toString(getHoursEntry().getRunOnce()));
            cronEditorBean.setHoursRunOnceMeridian(getHoursEntry().getRunOnceMeridian());
        }
        else
        {
            // range
            cronEditorBean.setHoursFrom(Integer.toString(getHoursEntry().getFrom()));
            cronEditorBean.setHoursFromMeridian(getHoursEntry().getFromMeridian());
            cronEditorBean.setHoursTo(Integer.toString(getHoursEntry().getTo()));
            cronEditorBean.setHoursToMeridian(getHoursEntry().getToMeridian());
        }

        // Set the minute values
        cronEditorBean.setMinutes(Integer.toString(getMinutesEntry().getRunOnce()));

        // Set the day of week values + the ordinal
        cronEditorBean.setSpecifiedDaysOfWeek(getDaysOfWeekEntry().getDaysAsNumbers());
        cronEditorBean.setDayInMonthOrdinal(getDaysOfWeekEntry().getDayInMonthOrdinal());

        // Lastly lets set the mode
        if (isDailyMode())
        {
            cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        }
        else if (isDayPerWeekMode())
        {
            cronEditorBean.setMode(CronEditorBean.DAYS_OF_WEEK_SPEC_MODE);
        }
        else if (isDaysPerMonthMode())
        {
            // Set the sub-mode radio select for this mode
            cronEditorBean.setDayOfWeekOfMonth(isDayOfWeekOfMonth());
            cronEditorBean.setMode(CronEditorBean.DAYS_OF_MONTH_SPEC_MODE);
        }
        else
        {
            cronEditorBean.setMode(CronEditorBean.ADVANCED_MODE);
        }

        return cronEditorBean;
    }

    /**
     * Returns the cron string that the object was constructed with. This method does not guarantee that the returned
     * cron string is valid according the the {@link org.quartz.CronTrigger}.
     *
     * @return unmodified cronString passed into the constructor
     */
    public String getCronString()
    {
        return cronString;
    }

    /**
     * Returns true only if the cron string can be handled by the cron editor UI.
     * If this method returns false then all method but {@link #getCronString()} will throw an IllegalStateException
     * work properly.
     *
     * @return true only if the editor has a state that corresponds to this cron expression.
     */
    public boolean isValidForEditor()
    {
        return validForEditor;
    }

    /**
     * Will return true if the passed in cron string is not valid for the editor.
     *
     * @return true if the cron string can not be handled, false otherwise.
     */
    public boolean isAdvancedMode()
    {
        return isAdvanced;
    }

    /**
     * Will return true if the editors daily mode can handle the provided cron string.
     *
     * @return true only if the mode is daily.
     */
    public boolean isDailyMode()
    {
        return isDaily;
    }

    /**
     * Will return true if the editors day per week mode can handle the provided cron string.
     *
     * @return true only if we are in day per week mode.
     */
    public boolean isDayPerWeekMode()
    {
        return isDayPerWeek;
    }

    /**
     * Will return true if the editors days per month mode can handle the provided cron string.
     *
     * @return true only if we are in days per month mode.
     */
    public boolean isDaysPerMonthMode()
    {
        return isDaysPerMonth;
    }

    /**
     * Returns true if {@link #isDaysPerMonthMode()} is true and the string in the days of week field can be handled
     * by the editor.
     *
     * @return true only if we are in the Nth Xday of the month mode (where N is a week in month number and X is mon,tue etc)
     */
    public boolean isDayOfWeekOfMonth()
    {
        return notApplicable(dayOfMonth) && !isWild(daysOfWeek) && !notApplicable(daysOfWeek);
    }

    /**
     * Gets the day of month field specified in the cron string.
     *
     * @return 1-31 or L.
     */
    public String getDayOfMonth()
    {
        return dayOfMonth;
    }

    /**
     * Gets the {@link CronMinutesEntry} that represents the minutes cron field.
     *
     * @return the minutes part of the cron string.
     */
    public CronMinutesEntry getMinutesEntry()
    {
        return minutesEntry;
    }

    /**
     * Gets the {@link CronHoursEntry} that represents the hours cron field.
     *
     * @return the hours part of the cron string.
     */
    public CronHoursEntry getHoursEntry()
    {
        return hoursEntry;
    }

    /**
     * Gets the {@link CronDayOfWeekEntry} that represents the day of week cron field.
     *
     * @return the days of the week part of the cronstring.
     */
    public CronDayOfWeekEntry getDaysOfWeekEntry()
    {
        return daysOfWeekEntry;
    }

    /**
     * Used to determine the total increment in minutes that are implied by the crons hour and minutes field. If
     * the hours and minutes field have an increment then the increment will come into play. An increment of 0
     * implies that the increment is once per day.
     *
     * @return the increment of repetition in minutes or 0 if there is no repetition.
     */
    public int getIncrementInMinutes()
    {
        return calculateIncrementInMinutes();
    }

    /**
     * Note: if both hoursIncrement and minutesIncrement are set, and the hoursIncrement is more than 1, an increment
     * for the whole cron expression is not supported by the Cron Editor UI (only regular increments are supported).
     *
     * For example, the cron expression 0 0/30 1-6/2 means "on the 1st, 3rd and 5th hours, at the 0th and 30th minute".
     * This schedule is not of a regular period, due to the gaps in the 2nd and 4th hour, so it doesn't make sense to
     * have an increment at all.
     *
     * But, we still need to return something - just return 0, since the increment value returned here should not be
     * interpreted anywhere if the above case is true (Increment is only used for UI purposes when the editor is not in
     * "advanced mode")
     *
     * @return the increment of repetition in minutes or 0 if there is no repetition.
     */
    private int calculateIncrementInMinutes()
    {
        int incrementInMinutes = 0;
        final boolean minutesHasIncrement = minutesEntry.hasIncrement();
        final boolean hoursHasIncrement = hoursEntry.hasIncrement();
        final int minutesIncrement = minutesEntry.getIncrement();
        final int hoursIncrement = hoursEntry.getIncrement();

        if (minutesHasIncrement && hoursHasIncrement && hoursIncrement != 1)
        {
            incrementInMinutes = 0;
        }
        else if (minutesHasIncrement)
        {
            incrementInMinutes = minutesIncrement;
        }
        else if (hoursHasIncrement)
        {
            incrementInMinutes = hoursIncrement * MINUTES_IN_HOUR;
        }

        return incrementInMinutes;
    }

    private void parseAndValidateCronString(String cronString)
    {
        parseCronString(cronString);

        // See if this cronstring is valid for the cron editor
        updateEditorFlags();

        if (!validForEditor)
        {
            // If the cronstring is invalid that setup the object to use the default cron values
            parseCronString(DEFAULT_CRONSTRING);
        }
    }

    private void parseCronString(String cronString)
    {
        StringTokenizer st = new StringTokenizer(cronString);
        if (st.countTokens() != NUM_CRON_FIELDS && st.countTokens() != NUM_CRON_FIELDS_NO_YEAR)
        {
            throw new IllegalArgumentException("The provided cron string does not have " + NUM_CRON_FIELDS + " parts: " + cronString);
        }

        // Process the string

        // Skip the seconds field, we don't care
        this.seconds = st.nextToken();
        String minutes = st.nextToken();
        String hours = st.nextToken();
        this.dayOfMonth = st.nextToken();
        this.month = st.nextToken();
        this.daysOfWeek = st.nextToken();
        this.hoursEntry = new CronHoursEntry(hours);
        this.minutesEntry = new CronMinutesEntry(minutes);
        this.daysOfWeekEntry = new CronDayOfWeekEntry(daysOfWeek);
        //check if year field was provided.
        if(st.hasMoreTokens())
        {
            this.year = st.nextToken();
        }
    }

    /**
     * Sets the various flags (isDaily, isDayPerWeek, isDaysPerMonth, isAdvanced, validForEditor) based on the state of
     * the parsed cron expression.
     */
    private void updateEditorFlags()
    {
        isDaily = (isWild(dayOfMonth) || notApplicable(dayOfMonth)) && isWild(month) && (isWild(daysOfWeek) || notApplicable(daysOfWeek));
        isDayPerWeek = (isWild(dayOfMonth) || notApplicable(dayOfMonth)) && isWild(month) && daysOfWeekEntry.getDayInMonthOrdinal() == null && !isWild(daysOfWeek);

        boolean numericDayOfMonth = !notApplicable(dayOfMonth) && !isWild(dayOfMonth) && isWild(month) && notApplicable(daysOfWeek) && StringUtils.containsOnly(dayOfMonth.toUpperCase(), VALID_DAY_OF_MONTH);
        boolean dayOfWeekOfMonth = notApplicable(dayOfMonth) && isWild(month) && !isWild(daysOfWeek) && !notApplicable(daysOfWeek) && daysOfWeekEntry.getDayInMonthOrdinal() != null;
        isDaysPerMonth = dayOfWeekOfMonth || numericDayOfMonth;

        boolean isValidMode = isDaily || isDayPerWeek || isDaysPerMonth;

        boolean hoursAndMinutesAreValid = hoursEntry.isValid() && minutesEntry.isValid();
        boolean daysOfWeekAreValid = daysOfWeekEntry.isValid();

        // JRA-13675: if an increment is specified for both hours and minutes, then the increments are only valid if
        // the hours increment is 1. This forces the editor into Advanced Mode.
        boolean incrementsValid = !(hoursEntry.hasIncrement() && minutesEntry.hasIncrement()) || hoursEntry.getIncrement() == 1;

        // JRA-13503: if an increment is specified for minutes, and hours is run once (a single hour), this will
        // eventually be interpretted as having an increment and therefore having a range. However, since the hours
        // is constructed unaware of the minute increment, the From and To hour are not set to correctly represent this
        // case (because the Run Once is set instead), and so future attempts to display this expression in the UI will
        // produce incorrect results.
        // We must flag this case as invalid for the editor defensively, so that we don't lose any detail when trying to
        // display the expression in the UI. The alternative would be to modify the hoursEntry after the minutesEntry
        // was created and take into account this case.
        hoursAndMinutesAreValid = hoursAndMinutesAreValid && !(hoursEntry.isRunOnce() && minutesEntry.hasIncrement());

        validForEditor = "0".equals(seconds) && isWild(month) && isValidMode && hoursAndMinutesAreValid && daysOfWeekAreValid && incrementsValid && StringUtils.isEmpty(year);
        // If the string is not valid for the editor then we are in advanced mode and not in any other mode
        if (!validForEditor)
        {
            isDaily = false;
            isDayPerWeek = false;
            isDaysPerMonth = false;
            isAdvanced = true;
        }
    }

    private boolean isWild(String expressionPart)
    {
        return WILDCARD.equals(expressionPart);
    }

    private boolean notApplicable(String expressionPart)
    {
        return NOT_APPLICABLE.equals(expressionPart);
    }
}
