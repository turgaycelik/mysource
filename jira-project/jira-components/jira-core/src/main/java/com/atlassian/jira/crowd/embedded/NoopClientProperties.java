package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.model.authentication.ApplicationAuthenticationContext;
import com.atlassian.crowd.service.client.ClientProperties;

import java.util.Properties;

/**
 * We don't provide any of this functionality.  Just need to provide this guy because
 * Crowd Rest Plugin needs an implementation.
 *
 * @since v4.3
 */
public class NoopClientProperties implements ClientProperties
{
    @Override
    public void updateProperties(Properties properties)
    {
    }

    @Override
    public String getApplicationName()
    {
        return null;
    }

    @Override
    public String getApplicationPassword()
    {
        return null;
    }

    @Override
    public String getApplicationAuthenticationURL()
    {
        return null;
    }

    @Override
    public String getCookieTokenKey()
    {
        return null;
    }

    @Override
    public String getCookieTokenKey(String s)
    {
        return null;
    }

    @Override
    public String getSessionTokenKey()
    {
        return null;
    }

    @Override
    public String getSessionLastValidation()
    {
        return null;
    }

    @Override
    public long getSessionValidationInterval()
    {
        return 0;
    }

    @Override
    public ApplicationAuthenticationContext getApplicationAuthenticationContext()
    {
        return null;
    }

    @Override
    public String getHttpProxyPort()
    {
        return null;
    }

    @Override
    public String getHttpProxyHost()
    {
        return null;
    }

    @Override
    public String getHttpProxyUsername()
    {
        return null;
    }

    @Override
    public String getHttpProxyPassword()
    {
        return null;
    }

    @Override
    public String getHttpMaxConnections()
    {
        return null;
    }

    @Override
    public String getHttpTimeout()
    {
        return null;
    }

    @Override
    public String getSocketTimeout()
    {
        return null;
    }

    @Override
    public String getBaseURL()
    {
        return null;
    }

    @Override
    public String getSSOCookieDomainName()
    {
        return null;
    }
}
