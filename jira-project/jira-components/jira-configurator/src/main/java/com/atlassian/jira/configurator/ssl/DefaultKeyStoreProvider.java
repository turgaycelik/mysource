package com.atlassian.jira.configurator.ssl;

import com.atlassian.jira.util.IOUtil;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class DefaultKeyStoreProvider implements KeyStoreProvider
{
    @Nonnull
    @Override
    public KeyStore load(@Nonnull final CertificateDetails certificateDetails) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException
    {
        final String fileName = certificateDetails.getKeyStoreLocation();
        final String password = certificateDetails.getKeyStorePassword();
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        final FileInputStream fileInputStream = new FileInputStream(fileName);
        try
        {
            keyStore.load(fileInputStream, password.length() == 0 ? null : password.toCharArray());
        }
        finally
        {
            IOUtil.shutdownStream(fileInputStream);
        }
        return keyStore;
    }
}
