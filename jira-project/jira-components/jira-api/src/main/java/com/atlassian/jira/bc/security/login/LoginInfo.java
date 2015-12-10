package com.atlassian.jira.bc.security.login;

import com.atlassian.annotations.PublicApi;

/**
 * Information about a user's login history
 *
 * @since v4.0.1
 */
@PublicApi
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
     * @return the date of the last failed login or null if its not known
     */
    Long getLastFailedLoginTime();

    /**
     * @return the number of times a user has logged in or null if its not recorded
     */
    Long getLoginCount();

    /**
     * @return the number of times the user has failed to login since this value was reset on a sucessful login or null
     *         if its not recorded
     */
    Long getCurrentFailedLoginCount();

    /**
     * @return the number of total number of times the user has failed to login ever or null if its not recorded
     */
    Long getTotalFailedLoginCount();

    /**
     * @return the maximum number of failed authentication attempts that are allowed for this user or null if its not applicable.
     */
    Long getMaxAuthenticationAttemptsAllowed();

    /**
     * @return true if the user has failed to authenticated more than a certain number of times or the configuration demands that it be done every time.  The user will now be required
     * to pass an elevated security check along with user name and password.
     */
    boolean isElevatedSecurityCheckRequired();

}
