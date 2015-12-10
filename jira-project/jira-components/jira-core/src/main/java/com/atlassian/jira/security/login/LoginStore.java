package com.atlassian.jira.security.login;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.security.login.LoginInfo;

/**
 * The store for saving user login information
 *
 * @since v4.0.1
 */
public interface LoginStore
{
    /**
     * Called to get the login information about a user
     *
     * @param user the user in play. This MUST not be null.
     *
     * @return a non null {@link LoginInfo} object
     */
    LoginInfo getLoginInfo(User user);

    /**
     * This will record that fact the user authenticated or not
     *
     * @param user          the user in play. This MUST not be null.
     * @param authenticated set to true if they authenticated ok or false otherwise
     *
     * @return a non null {@link com.atlassian.jira.bc.security.login.LoginInfo} object
     */
    LoginInfo recordLoginAttempt(User user, final boolean authenticated);

    /**
     * This returns the maximum failed authentications attempts that the user can perform
     * after which they will be asked for extra elevated security information
     *
     * @return the maximum authentication attempts allowed.
     */
    long getMaxAuthenticationAttemptsAllowed();

    /**
     * This can be called to reset the failed login count of a user
     *
     * @param user               the user to authorise.  This MUST not be null.
     */
    void resetFailedLoginCount(User user);
}
