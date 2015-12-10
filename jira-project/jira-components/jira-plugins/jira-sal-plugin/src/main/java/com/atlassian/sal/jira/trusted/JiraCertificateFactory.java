package com.atlassian.sal.jira.trusted;

import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.core.trusted.CertificateFactory;
import com.atlassian.security.auth.trustedapps.EncryptedCertificate;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;

public class JiraCertificateFactory implements CertificateFactory
{

    public EncryptedCertificate createCertificate(String username)
    {
        return createCertificate(username, null);
    }

    @Override
    public EncryptedCertificate createCertificate(final String username, final String url)
    {
        TrustedApplicationsManager trustedApplicationManager = ComponentLocator.getComponent(TrustedApplicationsManager.class);
        return trustedApplicationManager.getCurrentApplication().encode(username, url);
    }

}
