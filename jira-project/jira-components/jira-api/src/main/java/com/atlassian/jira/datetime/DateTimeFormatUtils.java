package com.atlassian.jira.datetime;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;

/**
 * Helper methods for dealing with the configured date and date-time formats.
 *
 * @since v5.0 - originally extracted from CustomFieldUtils
 */
public class DateTimeFormatUtils
{
    /**
     * Returns the configured Javascript date picker format.
     * <p>
     * ie the format stored in the "jira.date.picker.javascript.format" application property.
     *
     * @return the configured Javascript date picker format.
     */
    public static String getDateFormat()
    {
        return ComponentAccessor.getApplicationProperties().getDefaultBackedString(APKeys.JIRA_DATE_PICKER_JAVASCRIPT_FORMAT);
    }

    /**
     * Returns the configured Javascript date-time picker format.
     * <p>
     * ie the format stored in the "jira.date.time.picker.javascript.format" application property.
     *
     * @return the configured Javascript date-time picker format.
     */
    public static String getDateTimeFormat()
    {
        return ComponentAccessor.getApplicationProperties().getDefaultBackedString(APKeys.JIRA_DATE_TIME_PICKER_JAVASCRIPT_FORMAT);
    }

    /**
     * Returns "12" or "24" from the Javascript date-time picker format.
     *
     * @return "12" or "24" from the Javascript date-time picker format.
     *
     * @see #getDateTimeFormat()
     */
    public static String getTimeFormat()
    {
        final String dateTimeFormat = getDateTimeFormat();
        if (dateTimeFormat != null)
        {
            if ((dateTimeFormat.indexOf("%H") > -1) || (dateTimeFormat.indexOf("%R") > -1) || (dateTimeFormat.indexOf("%k") > -1))
            {
                return "24";
            }
        }

        return "12";
    }
}
