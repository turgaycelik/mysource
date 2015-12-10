package com.atlassian.jira.timezone;

import com.atlassian.jira.bc.JiraServiceContext;

import java.util.TimeZone;

/**
 * Internal interface used for resolving the TimeZoneInfo to use. These are normally implemented by the TimeZoneService,
 * except when JIRA is still bootstrapping.
 *
 * @since v5.0
 */
public interface TimeZoneResolver
{
    /**
     * Returns the JIRA default time zone.
     *
     * @param serviceContext a JiraServiceContext
     * @return a TimeZone
     * @see TimeZoneService#getDefaultTimeZoneInfo(com.atlassian.jira.bc.JiraServiceContext)
     */
    TimeZone getDefaultTimeZone(JiraServiceContext serviceContext);

    /**
     * Returns a user's effective time zone.
     *
     * @param serviceContext a JiraServiceContext
     * @return a TimeZone
     * @see TimeZoneService#getUserTimeZoneInfo(com.atlassian.jira.bc.JiraServiceContext)
     */
    TimeZone getUserTimeZone(JiraServiceContext serviceContext);
}
