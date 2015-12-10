package com.atlassian.jira.my_home;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeChangedEvent;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeUpdateException;
import com.atlassian.jira.user.preferences.UserPreferencesManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Loads and stores the My JIRA Home in the user's preferences.
 */
public class MyJiraHomeStorageImpl implements MyJiraHomeStorage
{
    private static final String MY_JIRA_HOME_PREFERENCE_KEY = "my.jira.home";

    private final UserPreferencesManager userPreferencesManager;
    private final EventPublisher eventPublisher;

    public MyJiraHomeStorageImpl(@Nonnull final UserPreferencesManager userPreferencesManager, @Nonnull final EventPublisher eventPublisher) {
        this.userPreferencesManager = userPreferencesManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Nonnull
    public String load(@Nonnull final User user)
    {
        final Preferences preferences = getPreferences(user);
        if (preferences == null)
        {
            return "";
        }

        return doLoad(preferences);
    }

    @Override
    public void store(@Nonnull User user, @Nonnull String home)
    {
        final Preferences preferences = getPreferences(user);
        if (preferences == null)
        {
            throw new MyJiraHomeUpdateException("failed to load user's preferences: " + user);
        }

        final String currentHome = doLoad(preferences);
        
        if (isNullOrEmpty(home.trim()))
        {
            doRemove(preferences);
        }
        else
        {
            doUpdate(preferences, home);
        }

        eventPublisher.publish(new MyJiraHomeChangedEvent(user, currentHome, home));
    }

    @Nullable
    private Preferences getPreferences(@Nullable final User user)
    {
        return userPreferencesManager.getPreferences(user);
    }

    private String doLoad(@Nonnull final Preferences preferences)
    {
        return nullToEmpty(preferences.getString(MY_JIRA_HOME_PREFERENCE_KEY));
    }

    private void doRemove(@Nonnull final Preferences preferences)
    {
        try
        {
            preferences.remove(MY_JIRA_HOME_PREFERENCE_KEY);
        }
        catch (AtlassianCoreException e)
        {
            throw new MyJiraHomeUpdateException(e);
        }
    }

    private void doUpdate(@Nonnull final Preferences preferences, @Nonnull final String home)
    {
        try
        {
            preferences.setString(MY_JIRA_HOME_PREFERENCE_KEY, home);
        }
        catch (AtlassianCoreException e)
        {
            throw new MyJiraHomeUpdateException(e);
        }
    }

}
