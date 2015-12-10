package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.CurrentApplication;

/**
 * @since v5.0
 */
public class DefaultCurrentApplicationFactory implements CurrentApplicationFactory
{
    private final CurrentApplicationStore store;

    public DefaultCurrentApplicationFactory(CurrentApplicationStore store)
    {
        this.store = store;
    }

    @Override
    public CurrentApplication getCurrentApplication()
    {
        return store.getCurrentApplication();
    }
}
