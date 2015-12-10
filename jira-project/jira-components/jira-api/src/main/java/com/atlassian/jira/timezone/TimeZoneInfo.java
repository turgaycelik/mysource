package com.atlassian.jira.timezone;

import com.atlassian.annotations.PublicApi;

import java.util.TimeZone;

/**
 * The TimeZoneInfo holds information about a timezone.
 * @since v4.4
 */
@PublicApi
public interface TimeZoneInfo extends Comparable<TimeZoneInfo>
{
    /**
     * @return the id of the timezone
     */
    String getTimeZoneId();

    /**
     * @return the i18n'ed display name for this timezone.
     */
    String getDisplayName();

    /**
     * @return the GMT offset in the format (GMT[+|-]hh:mm)
     */
    String getGMTOffset();

    /**
     * @return the name of the city for this timezone.
     */
    String getCity();

    /**
     * @return the key of the region
     */
    String getRegionKey();

    /**
     * @return the JAVA TimeZone object for this timezone.
     */
    TimeZone toTimeZone();
}
