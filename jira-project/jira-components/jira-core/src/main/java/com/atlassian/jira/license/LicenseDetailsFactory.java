package com.atlassian.jira.license;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.Internal;

/**
 * Provides the ability to extract a LicenseDetails object from a license String.
 */
@Internal
public interface LicenseDetailsFactory
{
    /**
     * Converts an encrypted license string to a license details. This method never returns null, instead it returns a
     * placeholder license details if the license string cannot be converted to a valid license. This placeholder is
     * typically a {@link com.atlassian.jira.license.NullLicenseDetails}.
     *
     * @param licenseString the (usually encrypted) license string
     * @return the corresponding license details
     */
    @Nonnull public LicenseDetails getLicense(@Nullable String licenseString);

    /**
     * Determines whether a license string can be decoded by this factory. If this returns true, then getLicense is
     * guaranteed t return a valid non-placeholder LicenseDetails object.
     *
     * @param licenseString the (usually encrypted) license string to test
     * @return true if the license string can be interpreted by this factory, false otherwise
     */
    public boolean isDecodeable(@Nullable String licenseString);
}
