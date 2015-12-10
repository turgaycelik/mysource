package com.atlassian.jira.configurator.ssl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public interface KeyStoreProvider
{
    @Nonnull
    KeyStore load(@Nonnull CertificateDetails certificateDetails) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException;
}
