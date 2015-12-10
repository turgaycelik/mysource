package com.atlassian.jira.user.util;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.api.IncompatibleReturnType;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Simple user utilities that do not require an implementation with too many dependencies.
 *
 * @since v4.0
 */
public interface UserManager
{
    /**
     * Returns the total number of users defined in JIRA, regardless of whether they are active or not.
     *
     * @return the total number of users defined in JIRA
     * @since v4.0
     */
    int getTotalUserCount();

    /**
     * Returns all users defined in JIRA, regardless of whether they are active or not.
     *
     * @return the set of all users
     * @since v4.0
     */
    @Nonnull
    Set<User> getAllUsers();

    /**
     * Returns all users defined in JIRA, regardless of whether they are active or not.
     * <p/>
     * Legacy synonym for {@link #getAllUsers()}.
     *
     * @return the collection of all users
     * @since v4.3
     * @see #getAllUsers()
     */
    @Nonnull
    Collection<User> getUsers();

    /**
     * Returns all users defined in JIRA, regardless of whether they are active or not.
     *
     * @return the set of all users
     * @since v6.0
     */
    @Nonnull
    Collection<ApplicationUser> getAllApplicationUsers();

    /**
     * Returns a {@link User} based on user name.
     * <p/>
     *
     * @param userName the user name of the user
     * @return the User object, or null if the user cannot be found including null userName.
     * @since v4.0
     *
     * @deprecated Use {@link #getUserByKey(String)} or {@link #getUserByName(String)} instead. Since v6.0.
     */
    User getUser(final @Nullable String userName);

    /**
     * Returns a {@link User} based on user name.
     * <p>
     *
     * @param userName the user name of the user
     * @return the User object, or null if the user cannot be found including null userName.
     * @since v4.3
     *
     * @deprecated Use {@link #getUserByKey(String)} or {@link #getUserByName(String)} instead. Since v6.0.
     */
    User getUserObject(final @Nullable String userName);

    /**
     * Returns an {@link ApplicationUser} based on user key.
     *
     * @param userKey the key of the user
     * @return the ApplicationUser object
     * @since v5.1.1
     */
    @Nullable
    ApplicationUser getUserByKey(final @Nullable String userKey);

    /**
     * Returns an {@link ApplicationUser} based on user name.
     *
     * @param userName the user name of the user
     * @return the ApplicationUser object
     * @throws IllegalStateException if the {@link com.atlassian.crowd.embedded.api.CrowdService CrowdService}
     *      is able to resolve {@code userName} to a {@link User}, but the
     *      {@link com.atlassian.jira.user.UserKeyService UserKeyService} does not have a key mapped for it.
     *      This is not a valid configuration.
     * @since v5.1.1
     */
    @Nullable
    ApplicationUser getUserByName(final @Nullable String userName);

    /**
     * Returns an {@link ApplicationUser} based on user key.
     * <p/>
     * If you want to check if given user is known user - please use {@link #isUserExisting(com.atlassian.jira.user.ApplicationUser)}
     *
     * @param userKey the key of the user
     * @return the ApplicationUser object, or proxy unknown immutable ApplicationUser object (null iff the key is null).
     * @since v6.0
     */
    @Nullable
    ApplicationUser getUserByKeyEvenWhenUnknown(final @Nullable String userKey);

    /**
     * Returns an {@link ApplicationUser} based on user name.
     * <p/>
     * If you want to check if given user is known user - please use {@link #isUserExisting(com.atlassian.jira.user.ApplicationUser)}
     *
     * @param userName the user name of the user
     * @return the ApplicationUser object, or proxy unknown immutable ApplicationUser object (null iff the username is null).
     * @throws IllegalStateException if the {@link com.atlassian.crowd.embedded.api.CrowdService CrowdService}
     *      is able to resolve {@code userName} to a {@link User}, but the
     *      {@link com.atlassian.jira.user.UserKeyService UserKeyService} does not have a key mapped for it.
     *      This is not a valid configuration.
     * @since v6.0
     */
    @Nullable
    ApplicationUser getUserByNameEvenWhenUnknown(final @Nullable String userName);

    /**
     * Returns a {@link User} based on user name and directoryId
     *
     * @param userName the user name of the user
     * @param directoryId the Directory to look in
     * @return the User object, or null if the user cannot be found including null userName.
     * @since v4.3.2
     */
    @Nullable
    User findUserInDirectory(@Nullable String userName, Long directoryId);

    /**
     * Returns a {@link User} based on user name.
     * <p>
     * If a null username is passed, then a null User object is returned, but it is guaranteed to return a non-null User in all other cases.<br>
     * If the username is not null, but the User is not found then a proxy unknown immutable User object is returned.
     *
     * @param userName the user name of the user
     * @return the User object, or proxy unknown immutable User object (null iff the username is null).
     * @since v4.3
     * @deprecated Use {@link #getUserByKeyEvenWhenUnknown(String)} or {@link #getUserByNameEvenWhenUnknown(String)} instead. Since v6.0.
     */
    @Nullable
    User getUserEvenWhenUnknown(@Nullable String userName);

    /**
     * Test if this user can be updated, i.e. is in a writable directory.
     * This relies upon the local directory configuration and does not guarantee that the actual remote directory, e.g. the
     * remote LDAP directory, will actually allow the user to be updated.
     * <p>
     * If the "External user management" setting is on, then you cannot update the user.
     *
     * @param user The user to update.
     * @return true if the user is not {@code null} and can be updated.
     * @deprecated Use {@link #canUpdateUser(ApplicationUser)} instead. Since v6.0.
     */
    boolean canUpdateUser(@Nullable User user);

    /**
     * Test if this user can be updated, i.e. is in a writable directory.
     * This relies upon the local directory configuration and does not guarantee that the actual remote directory, e.g. the
     * remote LDAP directory, will actually allow the user to be updated.
     *
     * @param user The user to update.
     * @return true if the user can be updated.
     */
    boolean canUpdateUser(@Nonnull ApplicationUser user);

    /**
     * Check if this user is allowed to update their own user details.
     * <p>
     * Returns true if the given user is in a read-write directory AND the "External user management" setting is off.
     *
     * @param user The user
     * @return true if the given user is in a read-write directory AND the "External user management" setting is off.
     *
     * @since 6.4
     */
    @ExperimentalApi
    boolean userCanUpdateOwnDetails(@Nonnull ApplicationUser user);

    /**
     * Test if this user can be renamed.  In addition to the constraints of {@link #canUpdateUser(ApplicationUser)},
     * renaming a user is only allowed when:
     * <p/>
     * <ol>
     * <li>The user is in either an {@link com.atlassian.crowd.embedded.api.DirectoryType#INTERNAL INTERNAL}
     *       or {@link com.atlassian.crowd.embedded.api.DirectoryType#DELEGATING DELEGATING} user directory;
     *       <strong>AND</strong></li>
     * <li>Either JIRA is not configured as a crowd server, or
     *      {@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_USER_CROWD_ALLOW_RENAME}
     *      is enabled to bypass this check.</li>
     * </ol>
     *
     * @param user The user to rename.
     * @return true if the user is not {@code null} and can be renamed.
     * @since v6.0
     */
    boolean canRenameUser(@Nullable ApplicationUser user);

    /**
     * Updates the {@link User}. The user must have non-null names and email address.
     *
     * @param user The user to update.
     *
     * @throws com.atlassian.crowd.exception.runtime.UserNotFoundException If the supplied user does not exist in the {@link User#getDirectoryId() directory}.
     * @throws com.atlassian.crowd.exception.runtime.OperationFailedException If the underlying directory implementation failed to execute the operation.
     * @throws IllegalArgumentException If something is wrong with the provided user object
     * @since v5.0
     */
    void updateUser(User user);

    /**
     * Updates the {@link ApplicationUser}. The user must have non-null names and email address.  If the user's name
     * does not match the name that is currently associated with the {@link ApplicationUser#getKey() key}, then
     * this is implicitly treated as a request to rename the user.
     *
     * @param user The user to update.
     *
     * @throws com.atlassian.crowd.exception.runtime.UserNotFoundException If the supplied user does not exist in the {@link User#getDirectoryId() directory}.
     * @throws com.atlassian.crowd.exception.runtime.OperationFailedException If the underlying directory implementation failed to execute the operation.
     * @throws IllegalArgumentException If something is wrong with the provided user object
     * @since v6.0
     */
    void updateUser(ApplicationUser user);

    /**
     * Test if this user's password can be updated, i.e. is in a writable directory
     * which is not a Delegated LDAP directory.
     * This relies upon the local directory configuration and does not guarantee that the actual remote directory, e.g. the
     * remote LDAP directory, will actually allow the user to be updated.
     * <p>
     * If the "External user management", or "External password management" setting is on, then you cannot update the password.
     *
     * @param user The user to update.
     * @return true if the user is not {@code null} and the user's password can be updated.
     */
    boolean canUpdateUserPassword(@Nullable User user);

    /**
     * Test if this user's group membership can be updated, i.e. is in a writable directory or
     * a directory with Local Group support.
     * This relies upon the local directory configuration and does not guarantee that the actual remote directory, e.g. the
     * remote LDAP directory, will actually allow the user membership to be updated.
     *
     * @param user The user to update.
     * @return true if the user is not {@code null} and can be updated.
     */
    boolean canUpdateGroupMembershipForUser(User user);

    /**
     * Returns all groups defined in JIRA.
     * <p/>
     * <b>Warning:</b> previous incarnations of this method returned <code>com.opensymphony.user.User</code>. This class
     * has now been removed from the JIRA API, meaning that the 5.0 version is not binary or source compatible with
     * earlier versions.
     *
     * @return the set of all groups
     * @since v4.0
     */
    @IncompatibleReturnType (since = "5.0", was = "java.util.Set<com.opensymphony.user.Group>")
    Set<Group> getAllGroups();

    /**
     * Returns all groups defined in JIRA.
     * <p/>
     * Legacy synonym for {@link #getAllGroups()}.
     *
     * @return the set of all groups
     * @since v4.3
     * @see #getAllGroups()
     */
    Collection<Group> getGroups();

    /**
     * Returns a {@link Group} based on user name.
     * <p/>
     * <b>Warning:</b> previous incarnations of this method returned <code>com.opensymphony.user.User</code>. This class
     * has now been removed from the JIRA API, meaning that the 5.0 version is not binary or source compatible with
     * earlier versions.
     *
     * @param groupName the user name of the group
     * @return the Group object, or null if the group cannot be found including null groupName.
     * @since v4.0
     */
    @IncompatibleReturnType (since = "5.0", was = "com.opensymphony.user.User")
    Group getGroup(final @Nullable String groupName);

    /**
     * Returns a {@link Group} based on user name.
     * <p/>
     * Legacy synonym for {@link #getGroup(String)}.
     *
     * @param groupName the user name of the group
     * @return the Group object, or null if the group cannot be found including null groupName.
     * @since v4.3
     * @see #getGroup(String)
     */
    Group getGroupObject(final @Nullable String groupName);

    /**
     * Returns an ordered list of directories that have "read-write" permission.
     * ie those directories that we can add a user to.
     *
     * @return an ordered list of directories that have "read-write" permission.
     *
     * @see #hasWritableDirectory()
     */
    @Nonnull
    List<Directory> getWritableDirectories();

    /**
     * Returns true if at least one User Directory has "read-write" permission.
     * <p>
     * This is equivalent to:<br>
     *     <tt>&nbsp;&nbsp;getWritableDirectories().size() > 0</tt>
     *
     *
     * @return true if at least one User Directory has "read-write" permission.
     *
     * @see #getWritableDirectories()
     * @see #hasPasswordWritableDirectory()
     * @see #hasGroupWritableDirectory()
     */
    boolean hasWritableDirectory();

    /**
     * Returns true if any of the directories have permission to update user passwords, false if otherwise.
     * <p>
     * Note that this is not quite the same as {@link #hasWritableDirectory()} because of "Internal with LDAP Authentication" directories.
     * These directories are generally read-write but passwords are read-only.
     *
     * @return true if any of the directories have permission to update user passwords, false if otherwise.
     *
     * @see #hasWritableDirectory()
     */
    boolean hasPasswordWritableDirectory();

    /**
     * Returns true if any of the directories have permission to update groups.
     * <p>
     * Note that this will not always return the same results as {@link #hasWritableDirectory()} because you can set "Read-Only with Local Groups" to LDAP directories.
     * These directories are generally read-only but you can create local gropus and assign users to them.
     *
     * @return true if any of the directories have permission to update groups, false if otherwise.
     *
     * @see #hasWritableDirectory()
     */
    boolean hasGroupWritableDirectory();

    /**
     * Checks if the given directory is able to update user passwords.
     *
     * @param directory the Directory
     * @return true if the directory can update user passwords, false if otherwise.
     */
    boolean canDirectoryUpdateUserPassword(@Nullable Directory directory);

    Directory getDirectory(Long directoryId);

    /**
     * Checks if given user is existing user
     * @param user possible existing user object - i.e. recieved from {@link #getUserByKeyEvenWhenUnknown(String)} or {@link #getUserByNameEvenWhenUnknown(String)}
     * @return <code>true</code> if given user is real user, <code>false</code> otherwise (also when given object is <code>null</code>)
     * @see #getUserByKeyEvenWhenUnknown(String)
     * @see #getUserByNameEvenWhenUnknown(String)
     */
    boolean isUserExisting(@Nullable ApplicationUser user);

    /**
     * Checks for the existence of this user across all directories to determine
     * whether or not the user exists in the specified directory and whether or not
     * it is shadowing or shadowed by a user with the same username in another
     * active user directory.
     *
     * @param username the username to check
     * @param directoryId the directory ID of the user directory that the user came from
     * @return the shadowing state of the specified user
     */
    @ExperimentalApi
    @Nonnull UserState getUserState(@Nonnull String username, long directoryId);

    /**
     * This convenience method is equivalent to
     * {@link #getUserState(String,long) getUserState(user.getName(), user.getDirectoryId())}
     * except that a {@code null} user is permitted and returns {@link UserState#INVALID_USER}.
     *
     * @param user the user to check
     * @return the shadowing state of the specified user
     */
    @ExperimentalApi
    @Nonnull UserState getUserState(@Nullable User user);

    /**
     * This convenience method is equivalent to
     * {@link #getUserState(String,long) getUserState(user.getUsername(), user.getDirectoryId())}
     * except that a {@code null} user is permitted and returns {@link UserState#INVALID_USER}.
     *
     * @param user the user to check
     * @return the shadowing state of the specified user
     */
    @ExperimentalApi
    @Nonnull UserState getUserState(@Nullable ApplicationUser user);

    /**
     * The current state of a user with regard to the same username existing in other
     * user directories.  Only the first occurrence of a username in list of user
     * directories is effective.  If the same username is found in other directories,
     * then those entries are said to be "shadowed".  This value represents the results
     * of searching for the username across all of the directories to determine whether
     * or not shadowing is relevant for the given user.
     * <p/>
     * Using the various test methods like {@link #isInMultipleDirectories()} and
     * {@link #isValid()} should be preferred over testing for exact matches.
     */
    @ExperimentalApi
    static enum UserState
    {
        /**
         * Indicates that the specified user was not valid, meaning that the directory ID
         * is invalid, that directory is disabled, or the directory does not have a user
         * with the specified username.
         */
        INVALID_USER,

        /**
         * Indicates that an active user with the same name was found in a directory that
         * currently takes priority over directory and username that were specified, meaning
         * that the specified user is shadowed by another.
         */
        SHADOW_USER,

        /**
         * Indicates that there is only a single active user with that username across
         * all active user directories and shadowing is not relevant for the user.
         */
        NORMAL_USER,

        /**
         * Indicates that the specified username was first found in the specified directory is active and that there is at least one user with the
         * same username defined in another directory that is ordered after it, so this
         * user has a shadow (it is the effective version of the user, and is hiding others).
         */
        NORMAL_USER_WITH_SHADOW;

        /**
         * Returns whether or not the user is the same user that would be returned by a
         * call to {@link #getUserByName(String)}.  This is, it was the first matching
         * user found.
         */
        public boolean isEffective()
        {
            return this == NORMAL_USER || this == NORMAL_USER_WITH_SHADOW;
        }

        /**
         * Convenience method that only returns {@code true} for all of the valid user types.
         * @return {@code true} if the specified username and directory ID was valid;
         *      {@code false} if the user does not actually exist
         */
        public boolean isValid()
        {
            return this != INVALID_USER;
        }

        /**
         * Returns {@code true} if the user is in multiple active user directories, regardless
         * of whether or not the specified directory ID was the active one.
         *
         * @return {@code true} if the user is in multiple active user directories; {@code false}
         *      if the user is only in one or does not exist at all
         */
        public boolean isInMultipleDirectories()
        {
            return this == SHADOW_USER || this == NORMAL_USER_WITH_SHADOW;
        }
    }
}
