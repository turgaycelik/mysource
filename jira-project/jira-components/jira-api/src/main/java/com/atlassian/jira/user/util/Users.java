package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nullable;

/**
 * Static utility methods pertaining to {@link com.atlassian.crowd.embedded.api.User} instances.
 *
 * @since v4.4
 */
public class Users
{
    /**
     * Whether the specified user is anonymous.
     *
     * @param user The user to check.
     * @return true if the specified user is anonymous; otherwise, false.
     */
    public static boolean isAnonymous(@Nullable final User user)
    {
        return user == null;
    }

    public static boolean isAnonymous(@Nullable final ApplicationUser user)
    {
        return user == null;
    }
}
