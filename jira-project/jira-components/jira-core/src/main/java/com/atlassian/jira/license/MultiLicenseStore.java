package com.atlassian.jira.license;

import javax.annotation.Nonnull;

/**
 * A store to save multiple licenses to a persistent location.
 *
 * This class replaces {@link com.atlassian.jira.license.JiraLicenseStore}.
 *
 * @since v6.3
 */
public interface MultiLicenseStore
{
    /**
     * Retrieves all product license strings. The returned Iterable is immutable and is ordered by the ID in the
     * database. The returned Iterable is not null and does not contain null elements. The absence of a license is
     * indicated by an empty list.
     *
     * @return all current license strings from persistence
     */
    @Nonnull Iterable<String> retrieve();

    /**
     * Persists a new state for all licenses. This will overwrite all licenses currently stored.
     * <p/>
     * This method will do some cursory checking of inputs but will still store most invalid licenses if requested. The
     * caller should test licenses strings for validity before attempting to store them. This method may throw
     * an unspecified runtime exception if the argument is invalid, for example if it is null, empty, or contains a null
     * element.
     * <p/>
     * Until the license roles dark feature is enabled, MultiLicenseStores may operate in legacy mode. In legacy mode a
     * single license is arbitrarily chosen and stored in the backing legacy license store, typically as an
     * ApplicationProperties property. It is the caller's responsibility to ensure that no more than one license is
     * attempted to be stored when in legacy mode.
     *
     * @param licenseString the license strings to store
     */
    void store(@Nonnull Iterable<String> licenseString);

    /**
     * Gets the server ID from the persistence backend
     *
     * @return the server ID, {@code null} if not found
     */
    String retrieveServerId();

    /**
     * Stores the server ID to the persistent backend
     *
     * @param serverId the server ID to store
     */
    void storeServerId(String serverId);
}
