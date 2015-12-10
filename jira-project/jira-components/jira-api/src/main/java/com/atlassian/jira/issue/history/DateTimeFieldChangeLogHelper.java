package com.atlassian.jira.issue.history;

import java.util.Date;

/**
 * A helper component which can be used by (custom)fields to store date/time information in the issue change history.
 *
 * @since v4.4
 */
public interface DateTimeFieldChangeLogHelper
{
    /**
     * Converts a Date into a string representation for the issue change history.
     * Call this method, if you want to store a change history value for a (custom)field which stores date AND time information.
     *
     * @param date the value Date of the (custome)field
     *
     * @return a string representation of the date, which contains date AND time.
     */
    public String createChangelogValueForDateTimeField(Date date);

    /**
     * Converts a Date into a string representation for the issue change history.
     * Call this method, if you want to store a change history value for a (custom)field which stores ONLY calendar date (day, month, year) information.
     *
     * @param date date the value Date of the (custome)field
     * @return a string representation of the date.
     */
    public String createChangelogValueForDateField(Date date);

    /**
     * Use this method to render a value for the issue change history that has been stored using the method createChangelogValueForDateField().
     * This value should contain only date information.
     * The {@link com.atlassian.jira.datetime.DateTimeStyle.DATE} formatter is used to render the change history value.
     *
     * @param dateValue the value of the {@link com.atlassian.jira.issue.changehistory.ChangeHistoryItem}
     * @param dateStr  the string of the {@link com.atlassian.jira.issue.changehistory.ChangeHistoryItem}, if it can't convert the dateValue it will return the dateStr.
     * @return either a formatted date string, if it failed to convert the value it return the dateStr.
     */
    public String renderChangeHistoryValueDate(String dateValue, String dateStr);

    /**
     * Use this method to render a value for the issue change history that has been stored using the method createChangelogValueForDateTimeField().
     * This value should contain time AND time information.
     * The {@link com.atlassian.jira.datetime.DateTimeStyle.DATE_TIME_PICKER} formatter is used to render the change history value.
     *
     * @param dateTimeValue the value of the {@link com.atlassian.jira.issue.changehistory.ChangeHistoryItem}
     * @param dateTimeString  the string of the {@link com.atlassian.jira.issue.changehistory.ChangeHistoryItem}, if it can't convert the dateTimeValue it will return the dateStr.
     * @return either a formatted date string, if it failed to convert the value it return the dateTimeString.
     */
    public String renderChangeHistoryValueDateTime(String dateTimeValue, String dateTimeString);

}
