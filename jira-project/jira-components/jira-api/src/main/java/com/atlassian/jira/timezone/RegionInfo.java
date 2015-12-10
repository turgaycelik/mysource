package com.atlassian.jira.timezone;

import com.atlassian.annotations.PublicApi;

/**
 * TimeZones are grouped by region.
 *
 * @since v4.4
 */
@PublicApi
public interface RegionInfo extends Comparable<RegionInfo>
{
    /**
     * @return The key for this region.
     */
    String getKey();

    /**
     * @return the i18n'ed display name for this region.
     */
    String getDisplayName();
}
