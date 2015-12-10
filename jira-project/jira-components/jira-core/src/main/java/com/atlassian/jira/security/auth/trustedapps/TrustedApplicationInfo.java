package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.util.dbc.Null;
import com.atlassian.security.auth.trustedapps.DefaultTrustedApplication;
import com.atlassian.security.auth.trustedapps.RequestConditions;
import com.atlassian.security.auth.trustedapps.TrustedApplication;

import java.security.PublicKey;

public class TrustedApplicationInfo extends DefaultTrustedApplication implements TrustedApplication
{
    private final long id;
    private final String name;
    private final long timeout;
    private final String ipMatch;
    private final String urlMatch;

    TrustedApplicationInfo(long id, String applicationId, String name, long timeout, String ipMatch, String urlMatch, PublicKey publicKey)
    {
        super(publicKey, applicationId, name, buildRequestConditions(timeout, ipMatch, urlMatch));

        Null.not("applicationId", applicationId);
        this.id = id;
        this.name = name;
        this.timeout = timeout;
        this.ipMatch = ipMatch;
        this.urlMatch = urlMatch;
    }

    public long getNumericId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getIpMatch()
    {
        return ipMatch;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public String getUrlMatch()
    {
        return urlMatch;
    }

    public boolean isValidKey()
    {
        return !(getPublicKey() instanceof KeyFactory.InvalidPublicKey);
    }

    private static RequestConditions buildRequestConditions(long timeout, String ipMatches, String urlMatches)
    {
        RequestConditions.RulesBuilder result = RequestConditions.builder().setCertificateTimeout(timeout);
        if (ipMatches != null)
        {
            for (String ipMatch : TrustedApplicationUtil.getLines(ipMatches))
            {
                result.addIPPattern(ipMatch);
            }

        }

        if (urlMatches != null)
        {
            for (String urlMatch : TrustedApplicationUtil.getLines(urlMatches))
            {
                result.addURLPattern(urlMatch);
            }
        }

        return result.build();
    }
}
