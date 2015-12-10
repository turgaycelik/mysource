package com.atlassian.jira.configurator.ssl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class KeyStoreAccessorImpl implements KeyStoreAccessor
{
    private final KeyStoreProvider keyStoreProvider;

    public KeyStoreAccessorImpl(@Nonnull final KeyStoreProvider keyStoreProvider) {

        this.keyStoreProvider = keyStoreProvider;
    }

    @Override
    public X509Certificate loadCertificate(@Nonnull final CertificateDetails certificateDetails) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException
    {
        final KeyStore keyStore = keyStoreProvider.load(certificateDetails);
        try
        {
            final String keyStorePassword = certificateDetails.getKeyStorePassword();
            final String keyAlias = certificateDetails.getKeyAlias();
            final Key key = keyStore.getKey(keyAlias, keyStorePassword.toCharArray());
            if (key != null)
            {
                final Certificate certificate = keyStore.getCertificate(keyAlias);
                if (certificate instanceof X509Certificate)
                {
                    return (X509Certificate) certificate;
                }
            }
            return null;
        }
        catch (KeyStoreException e)
        {
            return null;
        }
    }
}
