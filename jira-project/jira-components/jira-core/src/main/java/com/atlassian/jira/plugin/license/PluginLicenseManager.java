package com.atlassian.jira.plugin.license;

import com.atlassian.license.LicenseException;

/**
 * This interface needs to be implemented by plugin modules that wish to allow JIRA to
 * push a license into them.
 *
 * @since v4.4
 */
public interface PluginLicenseManager
{
    /**
     * Set the license wusing this license string.
     * @param licenseString A license String
     * @throws LicenseException The Exception message should be in expanded form that can be directly show to the user.
     */
    public void setLicense(final String licenseString) throws LicenseException;
}
