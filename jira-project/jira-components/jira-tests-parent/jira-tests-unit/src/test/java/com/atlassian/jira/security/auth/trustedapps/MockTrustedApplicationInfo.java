package com.atlassian.jira.security.auth.trustedapps;

import java.security.PublicKey;

/**
* @since v5.0
*/
class MockTrustedApplicationInfo extends TrustedApplicationInfo
{
    MockTrustedApplicationInfo(final long id, final String applicationId, final String name, final int timeout)
    {
        this(id, applicationId, name, timeout, null, "/some/url");
    }

    MockTrustedApplicationInfo(final long id, final String applicationId, final String name, final int timeout, final String ipMatch, final String urlMatch)
    {
        this(id, applicationId, name, timeout, ipMatch, urlMatch, KeyUtil.generateNewKeyPair("RSA").getPublic());
    }

    MockTrustedApplicationInfo(final long id, final String applicationId, final String name, final int timeout, final String ipMatch, final String urlMatch, final PublicKey publicKey)
    {
        super(id, applicationId, name, timeout, ipMatch, urlMatch, publicKey);
    }
}
