package com.atlassian.jira.bc.license;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

/**
 * A service for get license information. (readonly)
 *
 * @since v4.0
 */
@PublicApi
public interface JiraLicenseService
{
    /**
     * Gets the server ID of the JIRA instance, creates it if it doesn't already exists.
     *
     * @return the server ID for this JIRA instance.
     */
    String getServerId();

    /**
     * Returns a non null {@link com.atlassian.jira.license.LicenseDetails} object that represents the current license or an instance of {@code
     * com.atlassian.jira.license.NullLicenseDetails} if the license is not currently setup
     *
     * @return a non null {@link com.atlassian.jira.license.LicenseDetails} object
     * @deprecated since JIRA 6.3 - use getLicenses instead
     */
    @Deprecated
    LicenseDetails getLicense();


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

    /**
     * Validates the license String in preparation for setting the license. Populates the ValidationResult with errors
     * while validating.
     *
     * @param i18nHelper the helper for i18n
     * @param licenseString the license to validate
     * @return a validation result with the validated license and potential errors.
     */
    ValidationResult validate(final I18nHelper i18nHelper, final String licenseString);

    /**
     * This will confirm that user has agreed to proceed under Evaluation terms, typically when the license is too old
     * for the current JIRA build.
     *
     * @param userName the name of the user that made the confirmation
     */
    void confirmProceedUnderEvaluationTerms(String userName);

    /**
     * Holds the validated license and potential errors
     */
    @PublicApi
    interface ValidationResult
    {
        /**
         * @return a non null {@link com.atlassian.jira.util.ErrorCollection}
         */
        ErrorCollection getErrorCollection();

        /**
         * @return the input licence string
         */
        String getLicenseString();

        /**
         * @return the version of the license that was decoded, 0 if the license was not decoded.
         */
        int getLicenseVersion();

        /**
         * @return the total number of users in the JIRA system
         */
        int getTotalUserCount();

        /**
         * @return the active user count in JIRA
         */
        int getActiveUserCount();
    }
}
