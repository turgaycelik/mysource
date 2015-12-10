package com.atlassian.jira.license;

import com.atlassian.license.LicenseException;
import com.atlassian.license.LicensePair;

/**
 * @since v4.0
 */
public class LicenseStringFactoryImpl implements LicenseStringFactory
{
    public String create(final String message, final String hash)
    {
        try
        {
            return new LicensePair(message, hash).getOriginalLicenseString();
        }
        catch (LicenseException e)
        {
            return null;
        }
    }
}
