package com.atlassian.jira.configurator.ssl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public interface KeyStoreAccessor
{
    /**
     * Returns the x509 certificate of a private key. In detail, it returns the first certificate of a private key's
     * certificate chain, if the alias denotes a key entry. Thus it returns only certificates of entries for which
     * a private key is existing. This obviously doesn't yield for public certificates.
     *
     * @param certificateDetails the information which references an entry in the key store
     * @return the first certificate of the private key's certificate chain; else <code>null</code>
     * @throws UnrecoverableKeyException if the password for the key store and the private key are different
     */
    X509Certificate loadCertificate(@Nonnull CertificateDetails certificateDetails) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException;
}
