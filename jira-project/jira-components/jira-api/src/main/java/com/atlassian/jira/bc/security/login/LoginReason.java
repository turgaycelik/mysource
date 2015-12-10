package com.atlassian.jira.bc.security.login;

/**
 * An enum of reasons as to how a login attempt went
 *
 * @since v4.0.1
 */
public enum LoginReason
{
    /**
     * The user is not allowed to even attempt a login
     */
    AUTHENTICATION_DENIED,
    /**
     * The user could not be authenticated.
     */
    AUTHENTICATED_FAILED,
    /**
     * The user could not be authorised.
     */
    AUTHORISATION_FAILED,
    /**
     * The login was OK
     */
    OK;
}
