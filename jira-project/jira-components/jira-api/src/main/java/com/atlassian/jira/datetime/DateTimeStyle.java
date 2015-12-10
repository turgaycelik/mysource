package com.atlassian.jira.datetime;

/**
 * The date styles that JIRA is capable of formatting to.
 *
 * @since v4.4
 */
public enum DateTimeStyle
{
    /**
     * Display the time only.
     */
    TIME,

    /**
     * Display the date (day, month, and year).
     * 
     * <strong>
     * You should use system time zone
     * when you use this type of formatter.
     * </strong>
     */
    DATE,

    /**
     * Display the date and time.
     */
    COMPLETE,

    /**
     * Display the date and time relative to now (e.g. using words such as "Today", "Yesterday", etc). Note that dates
     * will only be displayed in relative format if this feature is enabled in JIRA. Otherwise they will use the
     * COMPLETE format.
     */
    RELATIVE,

    /**
     * Displays the date and time in date picker format.
     */
    DATE_TIME_PICKER,

    /**
     * Displays the date in date picker format.
     */
    DATE_PICKER,

    /**
     * Displays the time only for relative dates.
     */
    RELATIVE_WITH_TIME_ONLY,

    /**
     * Displays all dates with time.
     */
    RELATIVE_ALWAYS_WITH_TIME,

    /**
     * Displays dates and never the time.
     */
    RELATIVE_WITHOUT_TIME,

    /**
     * The ISO8601 Date Time format. This format includes date and time information in UTC.
     */
    ISO_8601_DATE_TIME,

    /**
     * The ISO8601 Date format. This format includes the date only.
     */
    ISO_8601_DATE,

    /**
     * The RFC822 Date format.This format includes date,time and time zone information
     */
    RSS_RFC822_DATE_TIME,
}
