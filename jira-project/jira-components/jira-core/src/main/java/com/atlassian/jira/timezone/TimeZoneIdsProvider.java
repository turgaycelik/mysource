package com.atlassian.jira.timezone;

import java.util.Set;
import java.util.TimeZone;

/**
 * Source for the list of time zone ids that JIRA supports.
 *
 * @since v5.0
 */
interface TimeZoneIdsProvider
{
    /**
     * Returns a list of the <b>canonical</b> time zone ids that JIRA supports.
     *
     * @return a list of time zone ids
     * @since v5.0
     */
    Set<String> getCanonicalIds();

    /**
     * Returns the <em>canonical</em> time zone for the given time zone.
     *
     * @param timeZone a TimeZone
     * @return a TimeZone that is the canonical time zone
     * @since v5.0
     */
    TimeZone canonicalise(TimeZone timeZone);
}
