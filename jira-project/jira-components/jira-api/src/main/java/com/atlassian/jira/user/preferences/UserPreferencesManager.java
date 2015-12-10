package com.atlassian.jira.user.preferences;

import com.atlassian.annotations.PublicApi;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

/**
 * A simple manager for retrieving, caching and updating user preferences objects.
 *
 * @since 3.10
 */
@PublicApi
public interface UserPreferencesManager
{
    /**
     * Returns The user preferences for a user.
     * If {@code user} is {@code null}, then the preferences will be for an anonymous user and reflect only the
     * system's default settings.  The preferences for the anonymous user cannot be modified directly.
     *
     * @return The user preferences for a user.
     */
    ExtendedPreferences getExtendedPreferences(ApplicationUser user);

    /**
     * Returns The user preferences for a user.
     * If {@code user} is {@code null}, then the preferences will be for an anonymous user and reflect only the
     * system's default settings.  The preferences for the anonymous user cannot be modified directly.
     * Although not declared as such to avoid breaking API use of this method, the returned preferences
     * object is guaranteed to implement {@link ExtendedPreferences}.
     *
     * @return The user preferences for a user.
     * @deprecated Use {@link #getExtendedPreferences(ApplicationUser)} instead. Since v6.0.
     */
    Preferences getPreferences(ApplicationUser user);

    /**
     * Returns The user preferences for a user.
     * If {@code user} is {@code null}, then the preferences will be for an anonymous user and reflect only the
     * system's default settings.  The preferences for the anonymous user cannot be modified directly.
     * Although not declared as such to avoid breaking API use of this method, the returned preferences
     * object is guaranteed to implement {@link ExtendedPreferences}.
     *
     * @return The user preferences for a user.
     * @deprecated Use {@link #getExtendedPreferences(ApplicationUser)} instead. Since v6.0.
     */
    Preferences getPreferences(User user);

    /**
     * @deprecated These objects are flyweights so there is no longer any point in them being cached.  This method
     *          no longer does anything.  Since v6.2.
     */
    void clearCache();
}
