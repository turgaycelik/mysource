package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.util.dbc.Null;

/**
 * A simple data-object that contains the essential information about a trusted application. Immutable, thread-safe.
 *
 * @since v3.12
 */
class TrustedApplicationData
{
    private final long id;
    private final String applicationId;
    private final String name;
    private final String publicKey;
    private final long timeout;

    private final AuditLog created;
    private final AuditLog updated;

    private final String ipMatch;
    private final String urlMatch;

    TrustedApplicationData(String applicationId, String name, String publicKey, long timeout, AuditLog created, AuditLog updated, String ipMatch, String urlMatch)
    {
        this(0, applicationId, name, publicKey, timeout, created, updated, ipMatch, urlMatch);
    }

    TrustedApplicationData(long id, String applicationId, String name, String publicKey, long timeout, AuditLog created, AuditLog updated, String ipMatch, String urlMatch)
    {
        Null.not("applicationId", applicationId);
        Null.not("name", name);
        Null.not("publicKey", publicKey);
        Null.not("created", created);
        Null.not("updated", updated);

        this.id = id;
        this.applicationId = applicationId;
        this.name = name;
        this.publicKey = publicKey;
        this.timeout = timeout;
        this.created = created;
        this.updated = updated;
        this.ipMatch = ipMatch;
        this.urlMatch = urlMatch;
    }

    public long getId()
    {
        return id;
    }

    public String getApplicationId()
    {
        return applicationId;
    }

    public String getName()
    {
        return name;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public AuditLog getCreated()
    {
        return created;
    }

    public AuditLog getUpdated()
    {
        return updated;
    }

    public String getIpMatch()
    {
        return ipMatch;
    }

    public String getUrlMatch()
    {
        return urlMatch;
    }
}
