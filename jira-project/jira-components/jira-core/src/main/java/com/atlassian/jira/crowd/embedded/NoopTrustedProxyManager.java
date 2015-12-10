package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.manager.proxy.TrustedProxyManager;

import java.util.Set;

/**
 * We don't provide any of this functionality.  Just need to provide this guy because
 * Crowd Rest Plugin needs an implementation.
 *
 * @since v4.3
 */
public class NoopTrustedProxyManager implements TrustedProxyManager
{
    @Override
    public boolean isTrusted(String s)
    {
        return false;
    }

    @Override
    public Set<String> getAddresses()
    {
        return null;
    }

    @Override
    public boolean addAddress(String s)
    {
        return false;
    }

    @Override
    public void removeAddress(String s)
    {
    }
}
