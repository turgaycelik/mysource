package com.atlassian.jira.user.preferences;

import javax.annotation.Nullable;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * @since v6.0
 */
public class MockUserPreferencesManager implements UserPreferencesManager
{
    @Override
    public ExtendedPreferences getExtendedPreferences(final ApplicationUser user)
    {
        return new JiraUserPreferences(ApplicationUsers.getKeyFor(user), getPropertySet(user));
    }

    @Override
    public Preferences getPreferences(ApplicationUser user)
    {
        return getExtendedPreferences(user);
    }

    @Override
    public Preferences getPreferences(User user)
    {
        return getExtendedPreferences(ApplicationUsers.from(user));
    }

    @Override
    public void clearCache()
    {
    }




    @Nullable
    private static PropertySet getPropertySet(final ApplicationUser user)
    {
        if (user == null)
        {
            return null;
        }
        return ComponentAccessor.getUserPropertyManager().getPropertySet(user);
    }
}
