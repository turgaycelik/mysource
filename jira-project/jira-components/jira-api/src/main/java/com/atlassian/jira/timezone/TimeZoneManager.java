package com.atlassian.jira.timezone;

import com.atlassian.crowd.embedded.api.User;

import java.util.TimeZone;

/**
 * The TimeZoneManager can be used to retrieve the time zone of  a user or the logged in user.
 *
 * @since v4.4
 */
public interface TimeZoneManager
{

    /**
     * Return the time zone of the user who is currently logged in.
     *
     * @return the time zone.
     */
    TimeZone getLoggedInUserTimeZone();

    /**
     * Return the time zone of a user who is currently logged in.
     *
     * @param user a user.
     * @return the time zone.
     */
    TimeZone getTimeZoneforUser(User user);

    /**
     * @return default system time zone.
     */
    TimeZone getDefaultTimezone();
}
