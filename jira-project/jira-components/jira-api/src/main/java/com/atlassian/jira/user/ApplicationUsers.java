package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains utility methods for getting an {@link ApplicationUser} from a directory {@link User}.
 *
 * @since v5.1.1
 */
public final class ApplicationUsers
{
    /**
     * Obtains an ApplicationUser for the given directory User.
     *
     * @param user the directory User
     * @return the Application User, or {@code null} if the incoming user is {@code null}.
     * @throws IllegalStateException if {@code user} is not {@code null}, but the
     *      {@link UserKeyService} has no mapping for its {@link User#getName() username}.
     *      This should not occur during normal operation, as all users are assigned keys
     *      during the upgrade to v6.0 or when the user is created.
     */
    public static ApplicationUser from(@Nullable final User user)
    {
        if (user == null)
        {
            return null;
        }

        if (user instanceof BridgedDirectoryUser)
        {
            return ((BridgedDirectoryUser) user).toApplicationUser();
        }

        String key = ComponentAccessor.getUserKeyService().getKeyForUsername(user.getName());
        if (key == null)
        {
            // JRADEV-19499  The EvenWhenUnknown methods use a directory ID of -1 to indicate that this is a fake user
            // representing one that could not be found.  We don't want to throw IllegalStateException when that's what
            // we're dealing with; just handle it the same way that UserManager.getUserByNameEvenWhenUnknown would.
            if (user.getDirectoryId() != -1L)
            {
                throw new IllegalStateException("User '" + user.getName() + "' has no unique key mapping.");
            }
            key = user.getName();
        }
        return new DelegatingApplicationUser(key, user);
    }

    /**
     * Gets the user key for the given directory User.
     * <p>
     * This is a {@code null}-safe shorthand for
     * <code><pre>
     *     ApplicationUsers.from(user).getKey()
     * </pre></code>
     *
     * @param user the directory User
     * @return the application user Key for the given directory {@link User}, or
     *      {@code null} if the incoming user is {@code null} or has no mapping
     *      in the {@link UserKeyService}
     */
    @Nullable
    public static String getKeyFor(@Nullable final User user)
    {
        if (user == null)
        {
            return null;
        }

        if (user instanceof BridgedDirectoryUser)
        {
            return ((BridgedDirectoryUser) user).toApplicationUser().getKey();
        }

        return ComponentAccessor.getUserKeyService().getKeyForUsername(user.getName());
    }

    /**
     * Get the key from the given user in a null-safe manner
     * @param user the user (possibly null)
     * @return the key of the given user, or null if the user is null
     */
    @Nullable
    public static String getKeyFor(@Nullable ApplicationUser user)
    {
        return user == null ? null : user.getKey();
    }

    /**
     * Does a null-safe conversion from an application user to a directory user.
     *
     * @param user the ApplicationUser
     * @return the corresponding Directory user
     */
    public static @Nullable User toDirectoryUser(@Nullable ApplicationUser user)
    {
        return user == null ? null : user.getDirectoryUser();
    }

    /**
     * Performs {@code null}-safe conversion of a collection of directory users
     * to corresponding {@code ApplicationUser} objects.
     *
     * @param users the users to transform (may be {@code null} and may contain {@code null}
     * @return a list of application users
     * @throws IllegalStateException if any of the users exists in the crowd
     *      directory but has no mapping in the {@link UserKeyService} (see
     *      comments for {@link ApplicationUsers#from(User)}).
     */
    @Nullable
    public static List<ApplicationUser> from(@Nullable Collection<User> users)
    {
        if (users == null)
        {
            return null;
        }

        final List<ApplicationUser> applicationUsers = new ArrayList<ApplicationUser>(users.size());
        for (User user : users)
        {
            applicationUsers.add(from(user));
        }
        return applicationUsers;
    }

    /**
     * Performs {@code null}-safe conversion of a collection of application users
     * to corresponding directory {@link User} objects.
     *
     * @param applicationUsers the users to transform (may be {@code null} and may contain {@code null}
     * @return a list of directory users
     */
    @Nullable
    public static List<User> toDirectoryUsers(@Nullable Collection<ApplicationUser> applicationUsers)
    {
        if (applicationUsers == null)
        {
            return null;
        }

        final List<User> users = new ArrayList<User>(applicationUsers.size());
        for (ApplicationUser applicationUser : applicationUsers)
        {
            users.add(toDirectoryUser(applicationUser));
        }
        return users;
    }

    /**
     * Gets the {@link ApplicationUser} in a {@code null}-safe manner.  This is a convenient
     * shorthand for {@code ComponentAccessor.getUserManager().getUserByKey(key))}.
     * Prefer {@link UserManager#getUserByKey(String)} when the {@code UserManager} is
     * already available.
     *
     * @param key the user's key
     * @return the corresponding user, or {@code null} if no user exists with that key.
     */
    public static ApplicationUser byKey(@Nullable final String key)
    {
        if (key == null)
        {
            return null;
        }
        return ComponentAccessor.getUserManager().getUserByKey(key);
    }
}