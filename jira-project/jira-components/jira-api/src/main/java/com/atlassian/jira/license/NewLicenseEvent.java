package com.atlassian.jira.license;

import com.atlassian.annotations.ExperimentalApi;

/**
* Event fired when a new license is entered into JIRA. This can allow, e.g. plugins that
* cache license-related information to flush their cache.
* @since v5.0
*/
@ExperimentalApi
public final class NewLicenseEvent
{
    /**
     * The details of the newly entered license.
     */
    final public LicenseDetails licenseDetails;

    public NewLicenseEvent(LicenseDetails licenseDetails)
    {
        this.licenseDetails = licenseDetails;
    }
}
