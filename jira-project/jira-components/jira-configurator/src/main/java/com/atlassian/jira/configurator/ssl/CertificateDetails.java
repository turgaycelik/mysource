package com.atlassian.jira.configurator.ssl;

import javax.annotation.Nonnull;

public class CertificateDetails
{
    private final String keyStoreLocation;
    private final String keyStorePassword;
    
    private final String keyAlias;

    public CertificateDetails(@Nonnull final String keyStoreLocation, @Nonnull final String keyStorePassword, @Nonnull final String keyAlias)
    {
        this.keyStoreLocation = keyStoreLocation;
        this.keyStorePassword = keyStorePassword;
        this.keyAlias = keyAlias;
    }

    @Nonnull
    public String getKeyStoreLocation()
    {
        return keyStoreLocation;
    }

    @Nonnull
    public String getKeyStorePassword()
    {
        return keyStorePassword;
    }

    @Nonnull
    public String getKeyAlias()
    {
        return keyAlias;
    }
}
