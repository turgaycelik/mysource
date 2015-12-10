package com.atlassian.jira.license;

import com.atlassian.annotations.ExperimentalApi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This manager is used to perform Licence related tasks using the spanking brand new Licencing 2.0
 *
 * @since v4.0
 */
public interface JiraLicenseManager
{
    /**
     * Gets the server ID of the JIRA instance, creates it if it doesn't already exists.
     *
     * @return the server ID for this JIRA instance.
     */
    String getServerId();

    /**
     * Gets the current license details of this instance.
     *
     * @return the JIRA license of this instance, or NULL_LICENSE_DETAILS if it doesn't exist.
     * @throws com.atlassian.extras.common.LicenseException if the stored license string cannot be decoded
     * @deprecated this method will disappear in v7.0, please use {@link #getLicenses()}; since v6.3
     */
    @Deprecated
    LicenseDetails getLicense();

    /**
     * This will decode the given string into a {@link LicenseDetails} object.  It is assumed that the string is valid.
     * You will wear the consequences if it is not.
     *
     * @param licenseString the license string
     * @return the JIRA license for the given string or NULL_LICENSE_DETAILS if it is blank
     * @throws com.atlassian.extras.common.LicenseException if the stored license string cannot be decoded
     */
    LicenseDetails getLicense(String licenseString);

    /**
     * Returns true if the given license role is present and licensed.
     *
     * @param role the role
     * @return true if the given license role is present and licensed.
     * @since 6.3
     */
    @ExperimentalApi
    boolean isLicensed(@Nonnull LicenseRoleId role);

    /**
     * This returns true if the provided licence string can be decoded into a valid licence
     *
     * @param licenseString the license string
     * @return true if it is can be decoded and false otherwise
     */
    boolean isDecodeable(String licenseString);

    /**
     * Sets the current license of this instance.
     * <p>
     * Note that this method will fire a {@link NewLicenseEvent}.
     *
     * @param licenseString the license string
     * @return the JIRA license of this instance, this shouldn't be null if the {@code license} is valid.
     */
    LicenseDetails setLicense(String licenseString);

    /**
     * Sets the current license of this instance.
     * <p>
     * This is a special version of {@link #setLicense(String)} that will not fire any event and is purely for use
     * during a Data Import.
     *
     * @param licenseString the license string
     * @return the JIRA license of this instance, this shouldn't be null if the {@code license} is valid.
     */
    LicenseDetails setLicenseNoEvent(String licenseString);

    /**
     * This will confirm that user has agreed to proceed under Evaluation terms, typically when the license is too old
     * for the current JIRA build.
     *
     * @param userName the name of the user that made the confirmation
     */
    void confirmProceedUnderEvaluationTerms(String userName);

    /**
     * Retrieve a list of all products licenses installed in this instance.
     * <p/>
     * In JIRA 6.3 this method returns an iterable containing at most one license. In later versions it may contain
     * more.
     *
     * @return all product licenses installed in this instance.
     * @since 6.3
     */
    @ExperimentalApi
    Iterable<LicenseDetails> getLicenses();
}
