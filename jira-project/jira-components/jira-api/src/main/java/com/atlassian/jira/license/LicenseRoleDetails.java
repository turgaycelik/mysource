package com.atlassian.jira.license;

import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;


/**
 * Encapsulates {@link LicenseRoleId license role} information for a given
 * {@link com.atlassian.jira.license.LicenseDetails license}.
 * </p>
 * <p>
 * Typical usage:
 * <pre>
 *     JiraLicenseManager jlm = ...;
 *     LicenseDetails licenseDetails = jlm.getLicense();
 *     LicenseRoleDetails roleDetails = licenseDetails.getLicenseRoleDetails();
 *
 *     Set<LicenseRoleId> licenseRoles = roleDetails.getLicenseRoles();
 *     for (LicenseRoleId role : licenseRoles)
 *     {
 *         int numSeats = roleDetails.getUserLimit( role );
 *         ...
 *     }
 * </pre>
 * </p>
 *
 * @since v6.3
 */
@ExperimentalApi
public interface LicenseRoleDetails
{
    /** The integer constant indicating an effectively unlimited number of users/seats. */
    public static final int UNLIMITED_USERS = -1;

    /**
     * Returns the {@link LicenseRoleId license roles} encoded in the present license.
     */
    @Nonnull
    public Set<LicenseRoleId> getLicenseRoles();

    /**
     * Returns the number of seats for the given {@link LicenseRoleId}.
     *
     * @return the number of users/seats for the given license role;
     * see also {@link #UNLIMITED_USERS}.
     */
    public int getUserLimit( LicenseRoleId role );
}
