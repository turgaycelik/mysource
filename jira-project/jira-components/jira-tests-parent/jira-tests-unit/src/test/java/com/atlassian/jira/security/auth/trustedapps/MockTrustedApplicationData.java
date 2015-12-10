package com.atlassian.jira.security.auth.trustedapps;

import java.security.PublicKey;
import java.util.Date;

/**
* @since v5.0
*/
class MockTrustedApplicationData extends TrustedApplicationData
{
    MockTrustedApplicationData(long id, String applicationId, String name, int timeout)
    {
        this(id, applicationId, name, timeout, null, null);
    }

    MockTrustedApplicationData(long id, String applicationId, String name, int timeout, String ipMatch, String urlMatch)
    {
        this(id, applicationId, name, timeout, new AuditLog("created", new Date()), new AuditLog("updated", new Date()), ipMatch, urlMatch);
    }

    MockTrustedApplicationData(long id, String applicationId, String name, int timeout, AuditLog created, AuditLog updated, String ipMatch, String urlMatch)
    {
        this(id, applicationId, name, KeyUtil.generateNewKeyPair("RSA").getPublic(), timeout, created, updated, ipMatch, urlMatch);
    }

    MockTrustedApplicationData(long id, String applicationId, String name, PublicKey publicKey, int timeout, AuditLog created, AuditLog updated, String ipMatch, String urlMatch)
    {
        super(id, applicationId, name, KeyFactory.encode(publicKey), timeout, created, updated, ipMatch, urlMatch);
    }
}
