package com.atlassian.jira.sharing.type;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.type.ShareType.Name;

import java.util.Collection;
import java.util.Comparator;

/**
 * Factory that supplies all {@link ShareType}s that are available in the system.
 *
 * @since v3.13
 */
@PublicApi
public interface ShareTypeFactory
{
    /**
     * Returns a Collection of all {@link ShareType} instnaces available.
     *
     * @return a Collection of all {@link ShareType} instnaces available.
     */
    Collection<ShareType> getAllShareTypes();

    /**
     * Returns a {@link ShareType} based on a given key.
     *
     * @param type a string representing type of {@link ShareType}
     * @return a {@link ShareType} based on a given key.
     */
    ShareType getShareType(Name type);

    /**
     * Return a comparator that can order {@link com.atlassian.jira.sharing.SharePermission}s for display.
     *
     * @return a comparator for permission ordering.
     */
    Comparator<SharePermission> getPermissionComparator();
}
