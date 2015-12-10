/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.plugin.report.impl;

/**
 * DurationFormatter that returns the number of minutes only.
 * Used in the Excel version of the Time Teacking Report for instance.
 *
 * @since v3.11
 */
class MinutesDurationFormatter implements DurationFormatter
{
    public String format(Long duration)
    {
        return (duration == null) ? "" : "" + duration.longValue() / 60;
    }

    public String shortFormat(Long duration)
    {
        return format(duration);
    }
}