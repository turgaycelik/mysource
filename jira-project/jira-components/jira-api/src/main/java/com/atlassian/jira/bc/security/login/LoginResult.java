package com.atlassian.jira.bc.security.login;

import com.atlassian.annotations.PublicApi;

import java.util.Set;

/**
 * A result object for login operations
 *
 * @since v4.0.1
 */
@PublicApi
public interface LoginResult
{
    /**
     * @return true if all is OK.  In this case {@link #getReason()} will equals {@link LoginReason#OK}
     */
    boolean isOK();

    /**
     * @return the name of the user that the login was performed for or null if its not known
     */
    String getUserName();

    /**
     * If the login fails then this enum describes why
     *
     * @return the {@link LoginReason}
     */
    LoginReason getReason();

    /**
     * Returns a Set of DenialReason objects, which can be used to determine the reason why a login request has been
     * denied. This method returns an empty set when <code>{@link #getReason()} != {@link
     * com.atlassian.jira.bc.security.login.LoginReason#AUTHENTICATION_DENIED}</code>.
     *
     * @return a Set of DenialReason
     */
    Set<DeniedReason> getDeniedReasons();

    /**
     * @return The {@link LoginInfo} associated with the user or null if the {@link #getUserName()} returns null.
     */
    LoginInfo getLoginInfo();
}
