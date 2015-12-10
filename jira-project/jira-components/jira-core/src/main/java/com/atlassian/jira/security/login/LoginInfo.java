package com.atlassian.jira.security.login;

/**
 * Information about a user's login history
 *
 * @since v4.0
 */
public interface LoginInfo
{
    /**
     * @return a miliseconds UTC time of the last successful login or null if its not been recorded
     */
    Long getLastLoginTime();

    /**
     * @return a miliseconds UTC time of the previously successful login or null if its not been recorded
     */
    Long getPreviousLoginTime();

    /**
     * @return the number of times a user has logged in or null if its not recorded
     */
    Long getLoginCount();
}
