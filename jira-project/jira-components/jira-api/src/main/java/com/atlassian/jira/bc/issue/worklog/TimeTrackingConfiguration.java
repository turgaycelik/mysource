package com.atlassian.jira.bc.issue.worklog;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import java.math.BigDecimal;

/**
 * @since 4.0
 */
public interface TimeTrackingConfiguration
{
    /**
     * Is time tracking enabled?
     * @return true if time tracking is enabled
     */
    boolean enabled();

    /**
     * @return the current mode of operation
     * @since v4.2
     */
    Mode getMode();

    /**
     * Get the currently configured default duration to use when parsing duration string for time tracking.
     * (i.e. does "3" mean three minutes or three hours or three days)
     * @return default duration
     */
    DateUtils.Duration getDefaultUnit();

    /**
     * Get the currently configured number of hours in a day. Because this can be a non-integral number we return
     * a BigDecimal.
     * @return the number of hours in a working day
     */
    BigDecimal getHoursPerDay();

    /**
     * Get the currently configured number of days in a week. Because this can be a non-integral number we return
     * a BigDecimal
     * @return the number of days in a working week
     */
    BigDecimal getDaysPerWeek();

    /**
     * Should the comment entered on a transition screen be copied to the work description when logging work?
     *
     * @return true or false
     * @since v4.2
     */
    boolean copyCommentToWorkDescriptionOnTransition();

    /**
     * Describes the modes of operation for Time Tracking in JIRA.
     *
     * @since v4.2
     */
    enum Mode
    {
        /**
         * Original and Remaining Estimate are presented in one field which "switches" its behaviour depending on the
         * issue's state of work.
         */
        LEGACY,

        /**
         * Original and Remaining Estimate are presented individually and are at all times independently editable.
         */
        MODERN
    }

    /**
     * Implementation that is registered with PICO. Looks for configuration information stored in ApplicationProperties.
     */
    public static class PropertiesAdaptor implements TimeTrackingConfiguration
    {
        public final ApplicationProperties properties;

        public PropertiesAdaptor(final ApplicationProperties properties)
        {
            this.properties = properties;
        }

        public boolean enabled()
        {
            return properties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        }

        public Mode getMode()
        {
            return properties.getOption(APKeys.JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR) ? Mode.LEGACY : Mode.MODERN;
        }

        public DateUtils.Duration getDefaultUnit()
        {
            DateUtils.Duration defaultUnit;
            try
            {
                defaultUnit = DateUtils.Duration.valueOf(properties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DEFAULT_UNIT));
            }
            catch (IllegalArgumentException e)
            {
                defaultUnit = DateUtils.Duration.MINUTE;
            }
            catch (NullPointerException e)
            {
                defaultUnit = DateUtils.Duration.MINUTE;
            }
            return defaultUnit;
        }

        public BigDecimal getHoursPerDay()
        {
            return new BigDecimal(properties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));
        }

        public BigDecimal getDaysPerWeek()
        {
            return new BigDecimal(properties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
        }

        public boolean copyCommentToWorkDescriptionOnTransition()
        {
            return properties.getOption(APKeys.JIRA_TIMETRACKING_COPY_COMMENT_TO_WORK_DESC_ON_TRANSITION);
        }
    }
}