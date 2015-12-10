package com.atlassian.jira.user.preferences;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserPropertyManager;

/**
 * A simple implementation to cache user preferences objects.
 */
public class DefaultUserPreferencesManager implements UserPreferencesManager
{
    private final UserPropertyManager userPropertyManager;

    public DefaultUserPreferencesManager(final UserPropertyManager userPropertyManager)
    {
        this.userPropertyManager = userPropertyManager;
    }

    @Override
    public ExtendedPreferences getExtendedPreferences(final ApplicationUser user)
    {
        if (user == null)
        {
            return new JiraUserPreferences(null, null);
        }
        return new JiraUserPreferences(user.getKey(), userPropertyManager.getPropertySet(user));
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
}
