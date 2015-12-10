package com.atlassian.jira.user.util;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import javax.annotation.Nonnull;

/**
 * This is a back end service level interface that defines an API for user level operations.
 */
public interface UserUtil
{
    public static final String META_PROPERTY_PREFIX = "jira.meta.";

    /**
     * Returns the total number of users defined in JIRA, regardless of whether they are active or not.
     *
     * @return the total number of users defined in JIRA
     *
     * @since v4.0
     */
    int getTotalUserCount();

    /**
     * Returns the all users defined in JIRA, regardless of whether they are active or not.
     *
     * @return the set of all users defined in JIRA
     *
     * @since v4.0
     * @deprecated Since v4.3.  Use {@link #getUsers()}.
     */
    @Nonnull
    Set<User> getAllUsers();

    /**
     * Returns the all users defined in JIRA, regardless of whether they are active or not.
     *
     * @return the set of all users defined in JIRA
     *
     * @since v4.3
     */
    @Nonnull
    Collection<User> getUsers();

    /**
     * Returns the all users defined in JIRA, regardless of whether they are active or not.
     *
     * @return the set of all users defined in JIRA
     *
     * @since v6.0
     */
    @Nonnull
    Collection<ApplicationUser> getAllApplicationUsers();

    /**
     * Returns a {@link Group} based on user name.
     *
     * <p>
     * <strong>WARNING:</strong> This method will be changed in the future to return a Crowd {@link Group} object. Since v4.3.
     *
     * @param groupName the user name of the group
     *
     * @return the Group object, or null if the user cannot be found including null groupName.
     *
     * @since v4.0
     * @deprecated Since v4.3.  Use {@link #getGroupObject(String)}.
     */
    Group getGroup(@Nullable String groupName);

    /**
     * Returns a {@link Group} based on user name.
     *
     * <p>
     * <strong>WARNING:</strong> This method will be changed in the future to return a Crowd {@link Group} object. Since v4.3.
     *
     * @param groupName the user name of the group
     *
     * @return the Group object, or null if the user cannot be found including null groupName.
     *
     * @since v4.3
     */
    Group getGroupObject(@Nullable String groupName);

    /**
     * Creates a User from supplied details.
     * <p>
     * Email notification will be send to created user.
     *
     * @param username      The username of the new user. Needs to be lowercase and unique.
     * @param password      The password for the new user.
     * @param email         The email for the new user.  Needs to be a valid email address.
     * @param fullname      The full name for the new user
     * @param userEventType The event type dispatched on user creation.
     *   Either {@link com.atlassian.jira.event.user.UserEventType#USER_CREATED} or {@link com.atlassian.jira.event.user.UserEventType#USER_SIGNUP}
     *
     * @return The new user object that was created
     *
     * @throws PermissionException If the operation was not permitted.
     *
     * @since 4.3
     */
    User createUserWithNotification(String username, String password, String email, String fullname, int userEventType)
            throws PermissionException, CreateException;

    /**
     * Creates a User from supplied details.
     * <p>
     * Email notification will be send to created user.
     *
     * @param username      The username of the new user. Needs to be lowercase and unique.
     * @param password      The password for the new user.
     * @param email         The email for the new user.  Needs to be a valid email address.
     * @param fullname      The full name for the new user
     * @param directoryId   The directory to create the user in. Null means "first writable directory".
     * @param userEventType The event type dispatched on user creation.
     *   Either {@link com.atlassian.jira.event.user.UserEventType#USER_CREATED} or {@link com.atlassian.jira.event.user.UserEventType#USER_SIGNUP}
     *
     * @return The new user object that was created
     *
     * @throws PermissionException If the operation was not permitted.
     *
     * @since 4.3.2
     */
    User createUserWithNotification(String username, String password, String email, String fullname, Long directoryId, int userEventType)
            throws PermissionException, CreateException;

    /**
     * Creates a User from supplied details.
     * <p>
     * No email notification will be send to created user.
     *
     * @param username The username of the new user. Needs to be lowercase and unique.
     * @param password The password for the new user.
     * @param emailAddress The email for the new user.  Needs to be a valid email address.
     * @param displayName The display name for the new user
     *
     * @return The new user object that was created
     *
     * @throws PermissionException If the operation was not permitted.
     *
     * @since 4.3
     */
    User createUserNoNotification(String username, String password, String emailAddress, String displayName)
            throws PermissionException, CreateException;

    /**
     * Creates a User from supplied details.
     * <p>
     * No email notification will be send to created user.
     *
     * @param username The username of the new user. Needs to be lowercase and unique.
     * @param password The password for the new user.
     * @param emailAddress The email for the new user.  Needs to be a valid email address.
     * @param displayName The display name for the new user
     * @param directoryId   The directory to create the user in. Null means "first writable directory".
     *
     * @return The new user object that was created
     *
     * @throws PermissionException If the operation was not permitted.
     *
     * @since 4.3.2
     */
    User createUserNoNotification(String username, String password, String emailAddress, String displayName, Long directoryId)
            throws PermissionException, CreateException;

    /**
     * This will remove the user and removes the user from all the groups.
     * All components lead by user will have lead cleared.
     *
     * @param loggedInUser the user performing operation
     * @param user         the user to delete
     * @deprecated Use {@link #removeUser(ApplicationUser, ApplicationUser)} instead. Since v6.0.
     */
    void removeUser(User loggedInUser, User user);

    /**
     * This will remove the user and removes the user from all the groups.
     * All components lead by user will have lead cleared.
     *
     * @param loggedInUser the user performing operation
     * @param user         the user to delete
     */
    void removeUser(ApplicationUser loggedInUser, ApplicationUser user);

    /**
     * This is used to add a specified user to a specified group. The user will be added to the group if the user is not
     * already a member of the group.
     *
     * @param group     the group to add the user to.
     * @param userToAdd the user to add to the group.
     */
    public void addUserToGroup(Group group, User userToAdd) throws PermissionException, AddException;

    /**
     * This is used to add a user to many groups at once.
     *
     * @param groups    a list containing the groups to add the user to.
     * @param userToAdd the user to add to the group.
     */
    void addUserToGroups(Collection<Group> groups, User userToAdd) throws PermissionException, AddException;

    /**
     * This is used to remove a specified user from a specified group. The user will be removed from the group only if
     * the user is already a member of the group.
     *
     * @param group        the group to add the user to.
     * @param userToRemove the user to add to the group.
     */
    void removeUserFromGroup(Group group, User userToRemove) throws PermissionException, RemoveException;

    /**
     * This is used to remove a user from many groups at once.
     *
     * @param groups       a list containing the groups to add the user to.
     * @param userToRemove the user to add to the group.
     */
    void removeUserFromGroups(Collection<Group> groups, User userToRemove) throws PermissionException, RemoveException;

    /**
     * This is used to generate a reset password token that last a certain time and allows a person to access a page
     * anonymously so they can reset their password.
     * <p/>
     * The generated token will be associated with the named user so that that this information can be verified at a
     * later time.
     *
     * @param user the user in question.  This MUST not be null
     *
     * @return a newly generated token that will live for a certain time
     */
    PasswordResetToken generatePasswordResetToken(User user);

    interface PasswordResetToken
    {

        /**
         * @return the user that the password reset token is associated with
         */
        User getUser();

        /**
         * @return the unique token that will be associated with a user
         */
        String getToken();

        /**
         * @return how long before the token expires, in hours
         */
        int getExpiryHours();

        /**
         * @return the time in UTC milliseconds at which the token will expire
         */
        long getExpiryTime();
    }

    /**
     * This can be called to validate a token against the user.
     *
     * @param user the user in play
     * @param token the token they provided
     *
     * @return a Validation object that describes how the option went
     */
    PasswordResetTokenValidation validatePasswordResetToken(User user, String token);


    interface PasswordResetTokenValidation
    {

        enum Status
        {
            EXPIRED, UNEQUAL, OK
        }

        Status getStatus();
    }

    /**
     * Can be called to set the password for a user.  This will delete any password request tokens associated with that user
     * @param user the user in play
     * @param newPassword their new password
     *
     * @throws UserNotFoundException if the user does not exist
     * @throws InvalidCredentialException if the password is invalid
     * @throws OperationNotPermittedException if the underlying User Directory is read-only
     */
    public void changePassword(User user, String newPassword)
            throws UserNotFoundException, InvalidCredentialException, OperationNotPermittedException, PermissionException;

    /**
     * Returns the number of users that are currently 'active'.  For a user to be active, means that it must belong to a
     * group that has either the JIRA-users, JIRA-administrators or JIRA-Systemadministartors global permission.
     * Implementations of this method should take performance into consideration, and ensure that the value is cached.
     * Use {@link #clearActiveUserCount()} to clear the cache.
     * <p/>
     * Please note that the calculation will be run if the license does not specify a user limit.
     *
     * @return the active user count
     *
     * @see com.atlassian.jira.security.Permissions
     * @since v3.13
     */
    int getActiveUserCount();

    /**
     * Clears the cache of the active user count so that it can be recalculated. This method should be used when
     * performing operations that will modify the number of active users in the system.
     *
     * @since v3.13
     */
    void clearActiveUserCount();

    /**
     * Returns true if this JIRA instance has more active users, than allowed by the license.  If the license does not
     * require a user limit, this method will return false immediately.
     *
     * @return True if the number of active users is greater than the limit enforced by the license. False otherwise.
     *
     * @since v3.13
     */
    boolean hasExceededUserLimit();

    /**
     * Returns true if, after adding the specified number of users, the number of active users in JIRA does not exceed
     * the user limit allowed by the license. If the license does not require a user limit, this method will return true
     * immediately.
     *
     * @param numUsers the number of users to add to the JIRA instance. If 0, all things being equal, this method will
     *                 return true. Must not be negative!
     *
     * @return False if the number of active users + the number of users to add is greater than the limit enforced by
     *         the license. True otherwise.
     *
     * @since v3.13
     */
    boolean canActivateNumberOfUsers(int numUsers);

    /**
     * Returns true if, after adding the specified users, the number of active users in JIRA does not exceed the user
     * limit allowed by the license. If a user specified is already active, or cannot be found, they are not counted
     * towards the user limit. If the limit has already been exceeded, but none of the users specified are inactive, the
     * result will still be true, as these users are already active and thus nothing changes. If the license does not
     * require a user limit, this method will return true immediately.
     *
     * @param userNames the names of the users to add to the JIRA instance. Must not be null!
     *
     * @return False if the number of active users + the number of users to add is greater than the limit enforced by
     *         the license. True otherwise.
     *
     * @since v3.13
     */
    boolean canActivateUsers(Collection<String> userNames);

    /**
     * Returns a user based on user name.
     *
     * @param userName the user name of the user
     *
     * @return the User object, or null if the user cannot be found including null userName.
     *
     * @since v3.13
     * @deprecated Use {@link #getUserByKey(String)} or {@link #getUserByName(String)} instead. Since v6.0.
     */
    User getUser(String userName);

    /**
     * Returns a user based on key.
     *
     * @param userkey the key of the user
     *
     * @return the User object, or null if the user cannot be found including null userkey.
     *
     * @since v6.0
     */
    ApplicationUser getUserByKey(String userkey);

    /**
     * Returns a user based on user name.
     *
     * @param username the user name of the user
     *
     * @return the User object, or null if the user cannot be found including null userName.
     *
     * @since v6.0
     */
    ApplicationUser getUserByName(String username);

    /**
     * Returns a user based on user name.
     *
     * @param userName the user name of the user
     *
     * @return the User object, or null if the user cannot be found including null userName.
     *
     * @since v4.3
     *
     * @deprecated Use {@link #getUserByKey(String)} or {@link #getUserByName(String)} instead. Since v6.0.
     */
    User getUserObject(String userName);

    /**
     * Returns true if the a user exists with the specified userName
     *
     * @param userName the name of the user
     *
     * @return true if t a user exists with the specified name or false if not
     */
    boolean userExists(String userName);

    /**
     * Returns a list of JIRA admin {@link User}s.
     *
     * <p>
     * <strong>WARNING:</strong> This method will be changed in the future to return a Collection of Crowd {@link User} objects. Since v4.3.
     *
     * @return a list of JIRA admin {@link User}s.
     *
     * @since v3.13
     * @deprecated Since v4.3.  Use {@link #getJiraAdministrators()}.
     */
    Collection<User> getAdministrators();

    /**
     * Returns a list of JIRA admin {@link User}s.
     *
     * <p>
     * <strong>WARNING:</strong> This method will be changed in the future to return a Collection of Crowd {@link User} objects. Since v4.3.
     *
     * @return a list of JIRA admin {@link User}s.
     *
     * @since v4.3
     */
    Collection<User> getJiraAdministrators();

    /**
     * Returns a list of JIRA system admin {@link User}s.
     *
     * <p>
     * <strong>WARNING:</strong> This method will be changed in the future to return a Collection of Crowd {@link User} objects. Since v4.3.
     *
     * @return a collection of {@link User}'s that are associated with the {@link
     *         com.atlassian.jira.security.Permissions#SYSTEM_ADMIN} permission.
     *
     * @since v3.13
     * @deprecated Since v4.3.  Use {@link #getJiraSystemAdministrators()} .
     */
    Collection<User> getSystemAdministrators();

    /**
     * Returns a list of JIRA system admin {@link User}s.
     *
     * <p>
     * <strong>WARNING:</strong> This method will be changed in the future to return a Collection of Crowd {@link User} objects. Since v4.3.
     *
     * @return a collection of {@link User}'s that are associated with the {@link
     *         com.atlassian.jira.security.Permissions#SYSTEM_ADMIN} permission.
     *
     * @since v4.3
     */
    Collection<User> getJiraSystemAdministrators();

    /**
     * Takes the given user and adds him/her to all the groups that grant a user the global JIRA use permission. (see
     * {@link com.atlassian.jira.security.Permissions#USE}) Note: operation is only performed if by doing so we will not
     * exceed the user limit (if the current license happens to specify a limit)
     *
     * @param user The user to be added to the USE permission
     *
     * @since v3.13
     */
    void addToJiraUsePermission(User user) throws PermissionException;

    /**
     * Retrieve a collection of ProjectComponents - where the lead of each component is the specified user.
     *
     * @param user User leading components
     *
     * @return Collection of project components
     */
    Collection<ProjectComponent> getComponentsUserLeads(User user);

    /**
     * Retrieve a collection of ProjectComponents - where the lead of each component is the specified user.
     *
     * @param user User leading components
     *
     * @return Collection of project components
     */
    Collection<ProjectComponent> getComponentsUserLeads(ApplicationUser user);

    /**
     * Returns all the projects that leadUser is the project lead for.
     *
     * @param user the user in play
     *
     * @return A collection of project {@link org.ofbiz.core.entity.GenericValue}s
     */
    Collection<Project> getProjectsLeadBy(User user);

    /**
     * Returns all the projects that leadUser is the project lead for.
     *
     * @param user the user in play
     *
     * @return A collection of project {@link org.ofbiz.core.entity.GenericValue}s
     */
    Collection<Project> getProjectsLeadBy(ApplicationUser user);

    /**
     * Checking if user without SYSTEM_ADMIN rights tries to remove user with SYSTEM_ADMIN rights.
     *
     * @param loggedInUser User performing operation
     * @param user         User for remove
     *
     * @return true if user without SYSTEM_ADMIN rights tries to remove user with SYSTEM_ADMIN rights
     */
    boolean isNonSysAdminAttemptingToDeleteSysAdmin(User loggedInUser, User user);

    /**
     * Checking if user without SYSTEM_ADMIN rights tries to remove user with SYSTEM_ADMIN rights.
     *
     * @param loggedInUser User performing operation
     * @param user         User for remove
     *
     * @return true if user without SYSTEM_ADMIN rights tries to remove user with SYSTEM_ADMIN rights
     */
    boolean isNonSysAdminAttemptingToDeleteSysAdmin(ApplicationUser loggedInUser, ApplicationUser user);

    /**
     * Returns number of issues reported by user
     *
     * @param loggedInUser the logged in user
     * @param user the user to find the issue count for
     *
     * @return number of issues reported by user
     *
     * @throws SearchException if something goes wrong
     */
    long getNumberOfReportedIssuesIgnoreSecurity(User loggedInUser, User user) throws SearchException;

    /**
     * Returns number of issues reported by user
     *
     * @param loggedInUser the logged in user
     * @param user the user to find the issue count for
     *
     * @return number of issues reported by user
     *
     * @throws SearchException if something goes wrong
     */
    long getNumberOfReportedIssuesIgnoreSecurity(ApplicationUser loggedInUser, ApplicationUser user) throws SearchException;

    /**
     * Returns number of issues assigned to user
     *
     * @param loggedInUser the logged in user
     * @param user the user to find the issue count for
     *
     * @return number of issues assigned to user
     *
     * @throws SearchException if something goes wrong
     */
    long getNumberOfAssignedIssuesIgnoreSecurity(User loggedInUser, User user) throws SearchException;

    /**
     * Returns number of issues assigned to user
     *
     * @param loggedInUser the logged in user
     * @param user the user to find the issue count for
     *
     * @return number of issues assigned to user
     *
     * @throws SearchException if something goes wrong
     */
    long getNumberOfAssignedIssuesIgnoreSecurity(ApplicationUser loggedInUser, ApplicationUser user) throws SearchException;

    /**
     * Takes the given user and returns a "displayable name" by cautiously checking the different edge cases for users.
     *
     * @param user the user. May be null.
     *
     * @return The user's full name if present and not blank, the username if present, or null otherwise.
     */
    String getDisplayableNameSafely(User user);

    /**
     * Takes the given user and returns a "displayable name" by cautiously checking the different edge cases for users.
     *
     * @param user the user. May be null.
     *
     * @return The user's full name if present and not blank, the username if present, or null otherwise.
     */
    String getDisplayableNameSafely(ApplicationUser user);

    /**
     * Returns a collection of {@link Group} objects that the user belongs to.
     *
     * @param userName A User name
     *
     * @return the set of groups that the user belongs to
     * @since v4.3
     */
    SortedSet<Group> getGroupsForUser(String userName);

    /**
     * Returns a collection of the names of the groups that the user belongs to.
     *
     * @param userName A User name
     *
     * @return the set of groups that the user belongs to
     * @since v4.3
     */
    SortedSet<String> getGroupNamesForUser(String userName);

    /**
     * Returns a collection of {@link User} objects that belong to any of the passed in collection of group names.
     * Prefer using {@link #getAllUsersInGroupNamesUnsorted(java.util.Collection)} and sorting the list of users only
     * if absolutely necessary rather than relying on this method to perform the sort.
     *
     * @param groupNames a collection of group name strings
     *
     * @return the set of users that are in the named groups, sorted in {@link com.atlassian.jira.issue.comparator.UserCachingComparator}
     *         order
     */
    SortedSet<User> getAllUsersInGroupNames(Collection<String> groupNames);

    /**
     * Returns a collection of {@link User} objects that belong to any of the passed in collection of group names.
     *
     * @param groupNames a collection of group name strings
     *
     * @return the set of users that are in the named groups
     * @since 5.1
     */
    Set<User> getAllUsersInGroupNamesUnsorted(Collection<String> groupNames);

    /**
     * Returns a collection of {@link User} objects that are found within the passed in collection
     * of group names. Null users are excluded even if they exist in the underlying data.
     *
     * @param groupNames a collection of group name strings
     *
     * @return the set of users that are in the named groups.
     *
     * @deprecated Use {@link #getAllUsersInGroupNames(java.util.Collection)} instead. Since v4.3
     */
    SortedSet<User> getUsersInGroupNames(Collection<String> groupNames);

    /**
     * Returns a collection of {@link User} objects that are found within the passed in collection of {@link Group} objects.
     *
     * @param groups a collection of {@link Group} objects
     *
     * @return the set of users that are in the groups, sorted in {@link com.atlassian.jira.issue.comparator.UserCachingComparator}
     *         order
     */
    SortedSet<User> getAllUsersInGroups(Collection<Group> groups);

    /**
     * Returns a collection of {@link User} objects that are found within the passed in collection
     * of {@link Group} objects. Null users are excluded even if they exist in the underlying data.
     *
     * @param groups a collection of {@link Group} objects
     *
     * @return the set of users that are in the groups, sorted in {@link com.atlassian.jira.issue.comparator.UserCachingComparator}
     *         order
     *
     * @deprecated Use {@link #getAllUsersInGroups(java.util.Collection)} instead. Since v4.3
     */
    SortedSet<User> getUsersInGroups(Collection<Group> groups);
}
