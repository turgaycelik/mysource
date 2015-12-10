package com.atlassian.jira.security.auth;

import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * Provides Authorisation of the user to a request.  Called as part of the Seraph waltz!
 */
public interface AuthorisationManager
{
    /**
     * Called to ask whether a user (non null always) is authorised to perform the given request as a login event
     *
     * @param user a non null user
     * @param httpServletRequest the request in play
     * @return true if they are authorised to perform the request
     */

    boolean authoriseForLogin(@Nonnull final ApplicationUser user, final HttpServletRequest httpServletRequest);

    /**
     * Gets the set of role strings that are examined by Seraph to decide if a user is authorised to execute a request.
     *
     * @param httpServletRequest the request in play
     * @return a set of roles
     */
    Set<String> getRequiredRoles(final HttpServletRequest httpServletRequest);

    /**
     * Called to ask whether a user (non null always) is authorised to perform the given request as a login event
     *
     * @param user a possibly null user
     * @param httpServletRequest the request in play
     * @param role one or more of the roles that was given out during {@link #getRequiredRoles(javax.servlet.http.HttpServletRequest)}
     * @return true if they are authorised to perform the request
     */
    boolean authoriseForRole(@Nullable final ApplicationUser user, final HttpServletRequest httpServletRequest, final String role);

}
