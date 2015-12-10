package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;

import java.security.Principal;

/**
 * Represents a person who uses JIRA.  This differs from a {@link User}, which represents
 * a user in a user directory.  An ApplicationUser encompasses all users with the same
 * username (ignoring case) across all directories.
 * <p/>
 * Note that the v6.0 version of this interface differs from the v5.x experimental version
 * in one important regard: the v5.x version extended the {@link User} interface.  Unfortunately,
 * the two interfaces have incompatible contracts for {@link Object#equals(Object) equals},
 * so they have been divided.
 * <p/>
 * This incompatibility could lead, for example, to duplicate members in a {@code Set}
 * if you were to mix the two implementations.  Developers writing plugins that target
 * JIRA 5.x should be careful not to mix ApplicationUser objects with other implementations
 * of {@link User}; otherwise, unexpected results can occur (and it won't be compatible
 * with 6.0).
 * <p/>
 * {@link ApplicationUsers} is a utility class that allows you to easily switch between
 * {@link ApplicationUser} and {@link User}.  If you want to access the mapping between
 * user keys and usernames directly, use the {@link UserKeyService}.
 *
 * @since v5.1.1
 * @see ApplicationUsers
 * @see UserKeyService
 */
public interface ApplicationUser extends Principal
{
    /**
     * Returns the key which distinguishes the ApplicationUser as unique.  The same key is
     * shared by all {@code User}s with the same username (ignoring case) across all user
     * directories.
     *
     * @return the key which distinguishes the ApplicationUser as unique
     */
    String getKey();

    /**
     * @return the username (login) of the user; must never be {@code null}.
     * @see #getName()
     */
    String getUsername();

    /**
     * Synonym for {@link #getUsername()} and implementation of {@link java.security.Principal#getName()}.
     *
     * @return the username (login) of the user; must never be {@code null}.
     * @see #getUsername()
     */
    String getName();

    /**
     * @return the ID of the user directory that this user comes from.
     */
    long getDirectoryId();

    /**
     * @return <code>true<code> if this user is active.
     */
    boolean isActive();

    /**
     * @return email address of the user.
     */
    String getEmailAddress();

    /**
     * Returns the display name of the user.
     * This is sometimes referred to as "full name".
     *
     * @return display name of the user, must never be null.
     */
    String getDisplayName();

    /**
     * @return the user as seen by the particular user directory that this User is defined in.
     */
    User getDirectoryUser();

    /**
     * Implementations must ensure equality based on getKey().
     *
     * @param obj object to compare to.
     * @return {@code true} if and only if the key matches.
     */
    boolean equals(Object obj);

    /**
     * Implementations must produce a hashcode based on getKey().
     *
     * @return hashcode.
     */
    int hashCode();
}
