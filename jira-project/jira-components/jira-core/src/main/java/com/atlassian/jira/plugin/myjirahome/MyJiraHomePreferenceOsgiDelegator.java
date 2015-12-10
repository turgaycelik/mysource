package com.atlassian.jira.plugin.myjirahome;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Delegates all incoming calls to a service provided by the OSGI bundle. In case the service is not available
 * this class uses {@link MyJiraHomePreferenceFallBack}.
 *
 * @since 5.1
 */
public class MyJiraHomePreferenceOsgiDelegator implements MyJiraHomePreference
{
    private final MyJiraHomePreferenceFallBack fallBack = new MyJiraHomePreferenceFallBack();

    @Nonnull
    @Override
    public String findHome(@Nullable final User user)
    {
        return retrieveDelegate().findHome(user);
    }

    @Nonnull
    private MyJiraHomePreference retrieveDelegate()
    {
        final MyJiraHomePreference delegate = ComponentAccessor.getOSGiComponentInstanceOfType(MyJiraHomePreference.class);
        return delegate != null ? delegate : fallBack;
    }

}
