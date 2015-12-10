package com.atlassian.jira.plugin.report.impl;

/**
 * Duration formatter used in the Time Tracking Reports.
 *
 * @since v3.11
 */
interface DurationFormatter
{
    /**
     * Formats the time duration.
     *
     * @param duration the number of seconds duration to be formatted
     * @return formatted time duration, never null
     */
    String format(Long duration);

    /**
     * Formats the time duration in the most compact form possible.
     *
     * @param duration time duration to format in seconds
     * @return formatted time duration, never null
     */
    String shortFormat(Long duration);
}