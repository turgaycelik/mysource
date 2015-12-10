package com.atlassian.jira.functest.framework;

/**
 * Interface for carrying out user profile operations
 *
 * @since v3.13
 */
public interface UserProfile
{
    /**
     * Changes the current user's notification preferences.
     *
     * @param useHtml Set to True for HTML notifications, False for plain text notifications
     */
    void changeUserNotificationType(boolean useHtml);

    /**
     * Changes the user's default sharing settings.
     *
     * @param global true if default sharing should be public, false otherwise.
     */
    void changeUserSharingType(boolean global);

    /**
     * Changes the default sharing settings for users that have not configured any.
     *
     * @param global true if default sharing should be public, false otherwise.
     */
    void changeDefaultSharingType(boolean global);

    /**
     * Changes the current user's language preferences.
     *
     * @param lang the full language text to change to e.g. "German (Germany)"
     */
    void changeUserLanguage(String lang);

    /**
     * Changes the current user's language preferences by value.
     *
     * Note that the JIRA (not system) default will have a value of -1. Consider
     * {@link #changeUserLanguageToJiraDefault()}
     *
     * @param langValue the language value to change to e.g. "en_AU"
     */
    void changeUserLanguageByValue(String langValue);

    /**
     * Changes the current user's language preferences to the system default. 
     *
     */
    void changeUserLanguageToJiraDefault();

    /**
     * Goto the current user's profile page.
     */
    void gotoCurrentUserProfile();

    /**
     * Go to the user's profile page.
     * @param userName The user name of the other user.
     */
    void gotoUserProfile(String userName);

    /**
     * Changes the current user's time zone. A null time zone means that this user will use the default JIRA user time
     * zone.
     *
     *
     * @param timeZoneID@return this
     */
    UserProfile changeUserTimeZone(String timeZoneID);

    /**
     * Changes the current user would like to be notified by their own changes.
     *
     * @param notify
     */
    UserProfile changeNotifyMyChanges(boolean notify);

    /**
     * Changes the current user's autowatch preference.
     *
     * @param autowatch true if autowatch should be enabled
     */
    UserProfile changeAutowatch(boolean autowatch);

    /**
     * Retrieves the username on the profile for the currently logged in user.
     *
     * @return the username on the profile for the currently logged in user.
     */
    String userName();

    /**
     * Retrieves the user profile link on the JIRA Header.
     * @return the user profile link on the JIRA Header.
     */
    Link link();

    public static interface Link
    {
        /**
         * Whether the link to the user's profile is present on the JIRA Header.
         * @return {@code true} if the link to the user's profile is present on the JIRA Header; otherwise
         * {@code false}.
         */
        boolean isPresent();
    }
}
