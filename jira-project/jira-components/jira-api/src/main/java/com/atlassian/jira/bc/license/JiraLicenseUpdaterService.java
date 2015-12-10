package com.atlassian.jira.bc.license;

import com.atlassian.jira.license.LicenseDetails;

/**
 * A service for setting license information.
 *
 * @since v4.0
 */
public interface JiraLicenseUpdaterService extends JiraLicenseService
{
    /**
     * Sets the license from the given ValidationResult as the current JIRA license. The validation result must have no
     * errors.
     *
     * @param validationResult the validation result to get the license from.
     * @return the new LicenseDetails created from the validated license.
     * @throws java.lang.IllegalArgumentException if the validation result object has any error.
     */
    LicenseDetails setLicense(ValidationResult validationResult);

    /**
     * Sets the license from the given ValidationResult as the current JIRA license. The validation result must have no
     * errors.
     *
     * Note that this is a special method specifically for resetting a license during Data Import which is why it will
     * specifically not fire an Event.
     *
     * @param validationResult the validation result to get the license from.
     * @return the new LicenseDetails created from the validated license.
     * @throws java.lang.IllegalArgumentException if the validation result object has any error.
     */
    LicenseDetails setLicenseNoEvent(ValidationResult validationResult);
}
