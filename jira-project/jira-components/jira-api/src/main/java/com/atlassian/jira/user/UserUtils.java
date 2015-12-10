package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A static helper class for User operations.
 *
 * Normally you should use dependency injection to get hold of a UserManager or a UserUtil instance.
 *
 * @since v4.4
 */
public class UserUtils
{
    /**
     * Checks if a user with given username exists.
     * Returns true if such user exists, false otherwise.
     *
     * @param username username to look up
     * @return true if user found, false otherwise
     */
    public static boolean userExists(String username)
    {
        return getUser(username) != null;
    }

    /**
     * Retrieves and returns the user by given username.
     *
     * @param username the username to get
     * @return user or null if not found
     */
    public static User getUser(String username)
    {
        return getUserManager().getUserObject(username);
    }

    /**
     * Returns a {@link User} based on user name.
     * <p>
     * If a null username is passed, then a null User object is returned, but it is guaranteed to return a non-null User in all other cases.<br>
     * If the username is not null, but the User is not found then a proxy unknown immutable User object is returned.
     *
     * @param username the user name of the user
     * @return the User object, or proxy unknown immutable User object (null iff the username is null).
     * @since v4.4
     */
    public static User getUserEvenWhenUnknown(String username)
    {
        return getUserManager().getUserEvenWhenUnknown(username);
    }

    /**
     * Return the <em>first</em> user found that matches the given email address (or null if not found).
     * <p>
     * The email address is matched case insensitive.
     *
     * @param emailAddress user email address
     * @return The first user that matches the email address or null if not found or the given email address is null.
     */
    public static User getUserByEmail(String emailAddress)
    {
        // Trim down the email address to remove any whitespace etc.
        emailAddress = StringUtils.trimToNull(emailAddress);
        if (emailAddress == null)
        {
            return null;
        }

        for (User user : getAllUsers())
        {
            if (emailAddress.equalsIgnoreCase(user.getEmailAddress()))
            {
                return user;
            }
        }
        return null;
    }

    /**
     * Finds the users by the given e-mail address. E-mail address look-up is
     * done case insensitively. Leading or trailing spaces in the given e-mail
     * address are trimmed before look up.
     *
     * @param email e-mail address
     * @return always returns a list of users found (even if empty)
     */
    public static List getUsersByEmail(String email)
    {
        List users = new ArrayList();
        String emailAddress = StringUtils.trimToNull(email);
        if (emailAddress != null)
        {
            for (User user : getAllUsers())
            {
                if (emailAddress.equalsIgnoreCase(user.getEmailAddress()))
                {
                    users.add(user);
                }
            }
        }
        return users;
    }

    /**
     * Returns all users.
     * <p>
     *     WARNING: this could be very high in some JIRA installations, and may therefore pose a performance issue.
     *
     * @return all users.
     */
    public static Collection<User> getAllUsers()
    {
        return getUserManager().getUsers();
    }

    public static UserManager getUserManager()
    {
        return ComponentAccessor.getUserManager();
    }
}
