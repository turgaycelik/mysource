package com.atlassian.jira.configurator.ssl;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestKeystoreAccessorImpl
{
    private static final String ALIAS = "alias";
    private static final String PASSWORD = "secret";
    private static final CertificateDetails DETAILS = createDetails(PASSWORD);

    private final KeyStoreProvider mockKeyStoreProvider = mock(KeyStoreProvider.class);

    private final KeyStoreAccessorImpl keyStoreAccessor = new KeyStoreAccessorImpl(mockKeyStoreProvider);

    @Test
    public void loadNotExistingCertificate() throws Exception
    {
        when(mockKeyStoreProvider.load(DETAILS)).thenReturn(createKeyStore());

        assertNull(keyStoreAccessor.loadCertificate(DETAILS));
    }

    @Test
    public void loadExistingCertificate() throws Exception
    {
        when(mockKeyStoreProvider.load(DETAILS)).thenReturn(createKeyStoreWithEntry());

        assertNotNull(keyStoreAccessor.loadCertificate(DETAILS));
    }

    @Test(expected = UnrecoverableKeyException.class)
    public void loadExistingCertificateDifferentPassword() throws Exception
    {
        final CertificateDetails details = createDetails("different-password");
        when(mockKeyStoreProvider.load(details)).thenReturn(createKeyStoreWithEntry());

        assertNull(keyStoreAccessor.loadCertificate(details));
    }

    private KeyStore createKeyStoreWithEntry() throws Exception
    {
        final KeyStore keyStore = createKeyStore();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setIssuerDN(new X500Principal("CN=Issuer"));
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 10000));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 10000));
        certGen.setSubjectDN(new X500Principal("CN=Subject"));
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
        certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
        certGen.addExtension(X509Extensions.SubjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.rfc822Name, "test@test.test")));

        final X509Certificate cert = certGen.generate(keyPair.getPrivate(), "BC");

        keyStore.setEntry(ALIAS, new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), new Certificate[] {cert}), new KeyStore.PasswordProtection(PASSWORD.toCharArray()));
        return keyStore;
    }

    private static CertificateDetails createDetails(@Nonnull final String keyStorePassword)
    {
        return new CertificateDetails("/some/where", keyStorePassword, ALIAS);
    }

    private KeyStore createKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException
    {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        return keyStore;
    }
}
