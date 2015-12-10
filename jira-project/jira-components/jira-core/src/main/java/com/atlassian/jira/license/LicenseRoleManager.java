package com.atlassian.jira.license;

import com.atlassian.annotations.ExperimentalApi;
import com.google.common.base.Optional;

import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Provides read and write capabilities regarding license roles.
 *
 * @see com.atlassian.jira.license.JiraLicenseManager
 * @see com.atlassian.jira.bc.license.LicenseRoleService
 * @since 6.3
 */
@ExperimentalApi
public interface LicenseRoleManager
{
    /**
     * Retrieves the groups for the specified license role.
     *
     * @param licenseRoleId the license role to retrieve the groups for.
     * @return an immutable collection of groups for this license role. Will not be null.
     */
    @Nonnull
    Set<String> getGroupsFor(@Nonnull LicenseRoleId licenseRoleId);

    /**
     * Determines whether a group is associated with a license role.
     *
     * @param licenseRoleId the license role to check for. Must not be null.
     * @param groupId the group to check for. Must not be null.
     * @return true when the group belongs to the license role.
     */
    boolean licenseRoleHasGroup(@Nonnull LicenseRoleId licenseRoleId, @Nonnull String groupId);

    /**
     * Returns the {@link Collection} of all currently defined {@link LicenseRoleDefinition}s. Specifically, this is all
     * {@link LicenseRoleDefinition}s that are declared to exist throughout the whole system, even if they do not have a valid license.
     *
     * @return the {@link Set} of currently known {@link LicenseRoleDefinition}s.
     */
    @Nonnull
    Set<LicenseRoleDefinition> getDefinedLicenseRoles();

    /**
     * Determines whether a provided license role is defined. Specifically, this will check all {@link LicenseRoleDefinition}s
     * that are declared to exist throughout the whole system, even if they do not have a valid license.
     *
     * @param licenseRoleId the license role to check for.
     * @return true when the provided license role is defined and installed.
     */
    boolean isLicenseRoleDefined(@Nonnull final LicenseRoleId licenseRoleId);

    /**
     * Return the LicenseRoleDefinition associated with the passed {@link com.atlassian.jira.license.LicenseRoleId} or
     * {@link com.google.common.base.Optional#absent} if it does not exist.
     *
     * @param licenseRoleId the license role to check for.
     * @return the LicenseRoleDefinition associated with the passed {@link com.atlassian.jira.license.LicenseRoleId}.
     */
    @Nonnull
    Optional<LicenseRoleDefinition> getLicenseRoleDefinition(@Nonnull final LicenseRoleId licenseRoleId);

    /**
     * Set the groups associated with the passed {@link com.atlassian.jira.license.LicenseRoleId}.
     *
     * @param licenseRoleId the id of the license role to update.
     * @param groups the groups to associated with the passed license role.
     */
    void setGroups(@Nonnull LicenseRoleId licenseRoleId, @Nonnull Iterable<String> groups);
}
