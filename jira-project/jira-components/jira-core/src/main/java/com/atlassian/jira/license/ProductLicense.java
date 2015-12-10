package com.atlassian.jira.license;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since v6.3
 */
public class ProductLicense
{
    public static final String LICENSE = "license";

    private final String licenseKey;

    public ProductLicense(@Nonnull String licenseKey)
    {
        this.licenseKey = checkNotNull(licenseKey, "License key should not be null.");
    }

    public String getLicenseKey()
    {
        return licenseKey;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final ProductLicense that = (ProductLicense) o;

        if (!licenseKey.equals(that.licenseKey)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        return licenseKey.hashCode();
    }
}
