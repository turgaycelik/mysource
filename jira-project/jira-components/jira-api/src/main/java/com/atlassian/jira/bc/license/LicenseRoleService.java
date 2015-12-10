package com.atlassian.jira.bc.license;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.license.LicenseRole;
import com.atlassian.jira.license.LicenseRoleId;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides authorisation-related methods around the provision and consumption of license roles.
 *
 * @see com.atlassian.jira.license.JiraLicenseManager
 * @since 6.3
 */
@ExperimentalApi
public interface LicenseRoleService
{
    /**
     * The key for any group related errors reported.
     */
    final String ERROR_GROUPS = "groups";

    /**
     * Returns true if the given user has the given license role.
     *
     * @param user the user to check
     * @param licenseRoleId the license role to check
     * @return true if the given user has the given license role.
     */
    boolean userHasRole(@Nullable ApplicationUser user, @Nonnull LicenseRoleId licenseRoleId);

    /**
     * Return the collection of all the roles defined in the system or an error.
     *
     * @return the collection of all the roles defined in the system or an error.
     */
    @Nonnull
    ServiceOutcome<Set<LicenseRole>> getRoles();

    /**
     * Return the {@link com.atlassian.jira.license.LicenseRole} identified by the passed
     * {@link com.atlassian.jira.license.LicenseRoleId}.
     *
     * @param licenseRoleId the id of the role to find.
     *
     * @return the {@code LicenseRole} with the passed {@code LicenseRoleId} or an error if such a {@code LicenseRole}
     * is not currently installed in JIRA.
     */
    @Nonnull
    ServiceOutcome<LicenseRole> getRole(@Nonnull LicenseRoleId licenseRoleId);

    /**
     * Set the groups associated with the passed {@link com.atlassian.jira.license.LicenseRoleId}.
     *
     * @param licenseRoleId the id of the license role to update.
     * @param groups the groups to associated with the passed license role.
     * @return the updated {@link com.atlassian.jira.license.LicenseRole} or an error.
     */
    @Nonnull
    ServiceOutcome<LicenseRole> setGroups(@Nonnull LicenseRoleId licenseRoleId, @Nonnull Iterable<String> groups);
}
