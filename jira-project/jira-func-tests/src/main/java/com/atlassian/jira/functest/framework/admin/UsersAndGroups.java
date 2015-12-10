package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.admin.user.EditUserPage;

/**
 * Admin operations for users and groups.
 *
 * @since v3.13
 */
public interface UsersAndGroups
{
    /**
     * Adds a user with all fields set to username and email address as 'username'@example.com.
     * Brake the test if user exists.
     *
     * @param username The user to add.
     */
    void addUser(String username);

    /**
     * Check if user with given <tt>username</tt> exists.
     *
     * @param username username to check.
     * @return true if the user exists; otherwise false.
     */
    boolean userExists(String username);

    /**
     * Adds a user with specified values.
     *
     * @param username     username
     * @param password     user password
     * @param fullname     user's fullname
     * @param emailAddress email address of user
     */
    void addUser(String username, String password, String fullname, String emailAddress);

    /**
     * Adds a user with specified values.
     *
     * @param username     username
     * @param password     user password
     * @param fullname     user's fullname
     * @param emailAddress email address of user
     * @param sendEmail    flag to send a notification email to the new user
     */
    void addUser(String username, String password, String fullname, String emailAddress, boolean sendEmail);

    /**
     * Attempts to add a user with specified values, but does not verify that the add succeeded.  This is
     * intended for use in tests that are specifically checking for failing conditions.
     *
     * @param username     username
     * @param password     user password
     * @param fullname     user's fullname
     * @param emailAddress email address of user
     */
    void addUserWithoutVerifyingResult(String username, String password, String fullname, String emailAddress);

    /**
     * Attempts to add a user with specified values, but does not verify that the add succeeded.  This is
     * intended for use in tests that are specifically checking for failing conditions.
     *
     * @param username     username
     * @param password     user password
     * @param fullname     user's fullname
     * @param emailAddress email address of user
     * @param sendEmail    flag to send a notification email to the new user
     */
    void addUserWithoutVerifyingResult(String username, String password, String fullname, String emailAddress, boolean sendEmail);

    /**
     * Removes a user given the username.  This will throw an error if no user with the username exists.
     *
     * @param username The username of the user to delete.
     */
    void deleteUser(String username);

    /**
     * Removes a group given the groupname.  This will throw an error if no group with that groupname exists.
     *
     * @param groupname The name of the group to delete.
     */
    void deleteGroup(String groupname);

    /**
     * Navigate to the view user page in the admin section for the given user.
     *
     * @param username The username of the user to view.
     */
    void gotoViewUser(String username);

    /**
     * Navigate to the user browser page.
     */
    void gotoUserBrowser();

    /**
     * Navigate to the edit user page in the admin section for the given user.
     *
     * @param username The username of the user to edit.
     */
    EditUserPage gotoEditUser(String username);

    /**
     * Adds the specified user to the specified group
     *
     * @param userName  the user to add.
     * @param groupName the group to join.
     */
    void addUserToGroup(String userName, String groupName);

    /**
     * Adds the specified group.
     *
     * @param groupName the name of the group to create.
     */
    void addGroup(String groupName);

    /**
     * Removes an user from a specified group.
     * @param userName The name of the user to remove from the group.
     * @param groupName The name of the group to remove the user from.
     */
    void removeUserFromGroup(String userName, String groupName);
}
