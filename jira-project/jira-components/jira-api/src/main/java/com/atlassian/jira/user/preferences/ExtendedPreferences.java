package com.atlassian.jira.user.preferences;


import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.user.preferences.Preferences;

/**
 * Adding the abilities to store, retrieve text from property set and to check if preference is stored at all
 *
 * @since v6.0
 */
public interface ExtendedPreferences extends Preferences
{

    String getText(String key);

    void setText(String key, String value) throws AtlassianCoreException;

    /**
     * Checks if key exists (defaults doesn't count)
     *
     * @param key key to be checked
     * @return if value for key exists
     */
    boolean containsValue(final String key);

    /**
     * Returns the user's key, or {@code null} if the preferences are for an anonymous user.
     * @return the user's key, or {@code null} if the preferences are for an anonymous user.
     * @since 6.2
     */
    String getUserKey();

    /**
     * Preferences objects are equal iff they are preferences for the same user.
     *
     * @param other the object to check for equality
     * @return {@code true} if the the preferences are for the same user; {@code false} otherwise
     */
    @Override
    boolean equals(Object other);

    /**
     * Returns a hash code based on the user's key, or {@code 0} if the preferences are for an anonymous user.
     * @return a hash code based on the user's key, or {@code 0} if the preferences are for an anonymous user.
     */
    @Override
    int hashCode();
}
