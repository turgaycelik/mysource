package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;

/**
 * An interface for querying a user's default sharing preferences.
 *
 * @since v3.13
 */
public interface UserSharingPreferencesUtil
{
    /**
     * Return the default share permissions for the passed user. 
     *
     * @param user the user whose preferences should be queried.
     *
     * @return the default preferences for the user.
     */
    SharePermissions getDefaultSharePermissions(User user);
}
