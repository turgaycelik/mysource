package com.atlassian.jira.crowd.embedded;

import java.security.Key;

import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.manager.property.PropertyManager;
import com.atlassian.crowd.manager.property.PropertyManagerException;
import com.atlassian.crowd.util.mail.SMTPServer;

/**
 * We don't provide any of this functionality.  Just need to provide this guy because
 * Crowd Rest Plugin needs an implementation.
 *
 * @since v4.3
 */
public class NoopPropertyManager implements PropertyManager
{
    @Override
    public long getCacheTime() throws PropertyManagerException
    {
        return 0;
    }

    @Override
    public void setCacheTime(long cacheTime)
    {
    }

    @Override
    public String getTokenSeed() throws PropertyManagerException
    {
        return null;
    }

    @Override
    public void setTokenSeed(String seed)
    {
    }

    @Override
    public String getDeploymentTitle() throws PropertyManagerException
    {
        return null;
    }

    @Override
    public void setDeploymentTitle(String title)
    {
    }

    @Override
    public String getDomain() throws PropertyManagerException
    {
        return null;
    }

    @Override
    public void setDomain(String domain)
    {
    }

    @Override
    public boolean isSecureCookie() throws PropertyManagerException
    {
        return false;
    }

    @Override
    public void setSecureCookie(boolean secure)
    {
    }

    @Override
    public void setCacheEnabled(boolean enabled)
    {
    }

    @Override
    public boolean isCacheEnabled()
    {
        return false;
    }

    @Override
    public long getSessionTime()
    {
        return 0;
    }

    @Override
    public void setSessionTime(long time)
    {
    }

    @Override
    public SMTPServer getSMTPServer() throws PropertyManagerException
    {
        return null;
    }

    @Override
    public void setSMTPServer(SMTPServer server)
    {
    }

    @Override
    public Key getDesEncryptionKey() throws PropertyManagerException
    {
        return null;
    }

    @Override
    public void generateDesEncryptionKey() throws PropertyManagerException
    {
    }

    @Override
    public void setSMTPTemplate(String template)
    {
    }

    @Override
    public String getSMTPTemplate() throws PropertyManagerException
    {
        return null;
    }

    @Override
    public void setCurrentLicenseResourceTotal(int total)
    {
    }

    @Override
    public int getCurrentLicenseResourceTotal()
    {
        return 0;
    }

    @Override
    public void setNotificationEmail(String notificationEmail)
    {
    }

    @Override
    public String getNotificationEmail() throws PropertyManagerException
    {
        return null;
    }

    @Override
    public boolean isGzipEnabled() throws PropertyManagerException
    {
        return false;
    }

    @Override
    public void setGzipEnabled(boolean gzip)
    {
    }

    @Override
    public Integer getBuildNumber() throws PropertyManagerException
    {
        return null;
    }

    @Override
    public void setBuildNumber(Integer buildNumber)
    {
    }

    @Override
    public String getTrustedProxyServers() throws PropertyManagerException
    {
        return null;
    }

    @Override
    public void setTrustedProxyServers(String proxyServers)
    {
    }

    @Override
    public boolean isUsingDatabaseTokenStorage() throws PropertyManagerException
    {
        return false;
    }

    @Override
    public void setUsingDatabaseTokenStorage(boolean usingDatabaseTokenStorage)
    {
    }

    @Override
    public void removeProperty(String name)
    {
    }

    @Override
    public String getProperty(String name) throws ObjectNotFoundException
    {
        return null;
    }

    @Override
    public void setProperty(String name, String value)
    {
    }

    @Override
    public boolean isIncludeIpAddressInValidationFactors()
    {
        return false;
    }

    @Override
    public void setIncludeIpAddressInValidationFactors(boolean b)
    {
    }

    @Override
    public String getString(final String property, final String defaultValue)
    {
        return "";
    }

    @Override
    public boolean getBoolean(final String property, final boolean defaultValue)
    {
        return false;
    }

    @Override
    public int getInt(final String property, final int defaultValue)
    {
        return 0;
    }
}
