package com.atlassian.jira.timezone;

import com.atlassian.jira.bc.JiraServiceContext;

import java.util.TimeZone;

/**
 * Time zone resolver used during the bootstrap process. Because there is no database access at this point, this
 * resolver is not able to look up the time zone that each user has configured. Therefore, it always returns the system
 * time zone.
 *
 * @since v5.0
 */
public class NoDatabaseTimeZoneResolver implements TimeZoneResolver
{
    /**
     * Returns the system default time zone.
     *
     * @param serviceContext a JiraServiceContext
     * @return a TimeZone
     */
    @Override
    public TimeZone getDefaultTimeZone(JiraServiceContext serviceContext)
    {
        return TimeZone.getDefault();
    }

    /**
     * Returns the system default time zone.
     *
     * @param serviceContext a JiraServiceContext
     * @return a TimeZone
     */
    @Override
    public TimeZone getUserTimeZone(JiraServiceContext serviceContext)
    {
        return getDefaultTimeZone(serviceContext);
    }
}
