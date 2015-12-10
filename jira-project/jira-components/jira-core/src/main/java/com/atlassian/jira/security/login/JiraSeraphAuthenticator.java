package com.atlassian.jira.security.login;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.exception.AccountNotFoundException;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.exception.runtime.CommunicationException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.seraph.auth.AuthenticationContextAwareAuthenticator;
import com.atlassian.seraph.auth.AuthenticationErrorType;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.auth.LoginReason;
import com.atlassian.seraph.elevatedsecurity.ElevatedSecurityGuard;
import com.atlassian.seraph.util.SecurityUtils;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Principal;

import static com.atlassian.seraph.auth.LoginReason.AUTHENTICATED_FAILED;
import static com.atlassian.seraph.auth.LoginReason.AUTHENTICATION_DENIED;
import static com.atlassian.seraph.auth.LoginReason.OK;

/**
 * JIRA's standard implementation of Seraph's Authenticator interface.
 * <p/>
 * It uses Crowd Embedded to implement the abstract methods of Seraph's default base implementation.
 *
 * @since v4.3
 */
@AuthenticationContextAwareAuthenticator
public class JiraSeraphAuthenticator extends DefaultAuthenticator
{
    private static final Logger log = Logger.getLogger(JiraSeraphAuthenticator.class);

    @Override
    protected Principal getUser(final String username)
    {
        return getUserManager().getUserByName(username);
    }

    @Override
    protected boolean authenticate(final Principal user, final String password) throws AuthenticatorException
    {
        try
        {
            crowdServiceAuthenticate(user, password);
            return true;
        }
        catch (AccountNotFoundException e)
        {
            log.debug("authenticate : '" + user.getName() + "' does not exist and cannot be authenticated.");
            return false;
        }
        catch (FailedAuthenticationException e)
        {
            return false;
        }
        catch (CommunicationException ex)
        {
            throw new AuthenticatorException(AuthenticationErrorType.CommunicationError);
        }
        catch (OperationFailedException ex)
        {
            // Unexpected error - log the stacktrace.
            log.error("Error occurred while trying to authenticate user '" + user.getName() + "'.", ex);
            throw new AuthenticatorException(AuthenticationErrorType.UnknownError);
        }
    }

    private void crowdServiceAuthenticate(Principal user, String password) throws FailedAuthenticationException
    {
        // set the context class loader to this one, so that sun LDAP classes use the right classloader
        // (the same classloader as Crowd embedded itself).
        // Fixes JRADEV-6087/JRA-23998.
        // A better fix would be within crowd embedded itself, CWD-2414/CWD-2244
        final Thread currentThread = Thread.currentThread();
        final ClassLoader origCCL = currentThread.getContextClassLoader();
        try
        {
            currentThread.setContextClassLoader(this.getClass().getClassLoader());
            getCrowdService().authenticate(user.getName(), password);
        }
        finally
        {
            currentThread.setContextClassLoader(origCCL);
        }
    }

    /**
     * This is called to refresh the Principal object that has been retreived from the HTTP session.
     * <p/>
     * By default this will called {@link #getUser(String)} again to get a fresh user.
     *
     * @param httpServletRequest the HTTP request in play
     * @param principal          the Principal in play
     * @return a fresh up to date principal
     */
    @Override
    protected Principal refreshPrincipalObtainedFromSession(HttpServletRequest httpServletRequest, Principal principal)
    {
        Principal freshPrincipal = principal;
        if (principal != null && principal.getName() != null)
        {
            if (principal instanceof ApplicationUser)
            {
                freshPrincipal = getUserManager().getUserByKey(((ApplicationUser) principal).getKey());
            }
            else
            {
                freshPrincipal = getUser(principal.getName());
            }
            putPrincipalInSessionContext(httpServletRequest, freshPrincipal);
        }
        return freshPrincipal;
    }

    @Override
    protected Principal getUserFromBasicAuthentication(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
    {
        final String METHOD = "getUserFromSession : ";
        final boolean dbg = log.isDebugEnabled();

        final String header = httpServletRequest.getHeader("Authorization");
        LoginReason reason = OK;

        if (SecurityUtils.isBasicAuthorizationHeader(header))
        {
            if (dbg)
            {
                log.debug(METHOD + "Looking in Basic Auth headers");
            }

            final SecurityUtils.UserPassCredentials creds = SecurityUtils.decodeBasicAuthorizationCredentials(header);
            final ElevatedSecurityGuard securityGuard = getElevatedSecurityGuard();
            if (!securityGuard.performElevatedSecurityCheck(httpServletRequest, creds.getUsername()))
            {
                if (dbg)
                {
                    log.debug(METHOD + "'" + creds.getUsername() + "' failed elevated security check");
                }
                reason = AUTHENTICATION_DENIED.stampRequestResponse(httpServletRequest, httpServletResponse);
                securityGuard.onFailedLoginAttempt(httpServletRequest, creds.getUsername());
            }
            else
            {
                if (dbg)
                {
                    log.debug(METHOD + "'" + creds.getUsername() + "' does not require elevated security check.  Attempting authentication...");
                }

                try
                {
                    final boolean loggedin = login(httpServletRequest, httpServletResponse, creds.getUsername(),
                            creds.getPassword(), false);
                    if (loggedin)
                    {
                        reason = OK.stampRequestResponse(httpServletRequest, httpServletResponse);
                        securityGuard.onSuccessfulLoginAttempt(httpServletRequest, creds.getUsername());
                        if (dbg)
                        {
                            log.debug(METHOD + "Authenticated '" + creds.getUsername() + "' via Basic Auth");
                        }
                        return getUser(creds.getUsername());
                    }
                    else
                    {
                        reason = AUTHENTICATED_FAILED.stampRequestResponse(httpServletRequest, httpServletResponse);
                        securityGuard.onFailedLoginAttempt(httpServletRequest, creds.getUsername());
                    }
                }
                catch (final AuthenticatorException e)
                {
                    log.warn(METHOD + "Exception trying to login '" + creds.getUsername() + "' via Basic Auth:" + e, e);
                }
            }
            try
            {
                httpServletResponse.sendError(401, "Basic Authentication Failure - Reason : " + reason.toString());
            }
            catch (final IOException e)
            {
                log.warn(METHOD + "Exception trying to send Basic Auth failed error: " + e, e);
            }
            return null;
        }

        try
        {
            httpServletResponse.setHeader("WWW-Authenticate", "Basic realm=\"protected-area\"");
            httpServletResponse.sendError(401);
        }
        catch (final IOException e)
        {
            log.warn(METHOD + "Exception trying to send Basic Auth failed error: " + e, e);
        }
        return null;
    }

    /**
     * Get a fresh version of the Crowd Read Write service from Pico Container.
     *
     * @return fresh version of the Crowd Read Write service from Pico Container.
     */
    private CrowdService getCrowdService()
    {
        return ComponentAccessor.getComponent(CrowdService.class);
    }

    private UserManager getUserManager() {
        return ComponentAccessor.getUserManager();
    }
}

