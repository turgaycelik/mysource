package com.atlassian.jira.configurator.config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SslSettings
{
    private final String httpsPort;
    private final String keystoreFile;
    private final String keystorePass;
    private final String keystoreType;
    private final String keyAlias;

    public SslSettings(@Nonnull final String httpsPort, @Nullable final String keystoreFile, @Nonnull final String keystorePass, @Nonnull final String keystoreType, @Nullable final String keyAlias)
    {
        this.httpsPort = httpsPort;
        this.keystoreFile = keystoreFile;
        this.keystorePass = keystorePass;
        this.keystoreType = keystoreType;
        this.keyAlias = keyAlias;
    }

    @Nonnull
    public String getHttpsPort()
    {
        return httpsPort;
    }

    /**
     * In case this is, <code>null</code> it denotes the <code>.keystore</code> file in the operation system home
     * directory of the user that is running Tomcat.
     *
     * @return the key store containing the key and the certificate
     */
    @Nullable
    public String getKeystoreFile()
    {
        return keystoreFile;
    }

    @Nonnull
    public String getKeystorePass()
    {
        return keystorePass;
    }

    @Nonnull
    public String getKeystoreType()
    {
        return keystoreType;
    }

    /**
     * In case the returned value is <code>null</code>, it denotes the first certificate found in the key store.
     *
     * @return either the concrete alias or the first key in the key store
     */
    @Nullable
    public String getKeyAlias()
    {
        return keyAlias;
    }

    @Override
    public int hashCode()
    {
        int result = httpsPort != null ? httpsPort.hashCode() : 0;
        result = 31 * result + (keystoreFile != null ? keystoreFile.hashCode() : 0);
        result = 31 * result + (keystorePass != null ? keystorePass.hashCode() : 0);
        result = 31 * result + (keystoreType != null ? keystoreType.hashCode() : 0);
        result = 31 * result + (keyAlias != null ? keyAlias.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SslSettings that = (SslSettings) o;

        if (httpsPort != null ? !httpsPort.equals(that.httpsPort) : that.httpsPort != null) { return false; }
        if (keyAlias != null ? !keyAlias.equals(that.keyAlias) : that.keyAlias != null) { return false; }
        if (keystoreFile != null ? !keystoreFile.equals(that.keystoreFile) : that.keystoreFile != null)
        {
            return false;
        }
        if (keystorePass != null ? !keystorePass.equals(that.keystorePass) : that.keystorePass != null)
        {
            return false;
        }
        if (keystoreType != null ? !keystoreType.equals(that.keystoreType) : that.keystoreType != null)
        {
            return false;
        }

        return true;
    }
}
