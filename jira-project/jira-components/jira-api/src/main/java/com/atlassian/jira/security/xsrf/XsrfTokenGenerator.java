package com.atlassian.jira.security.xsrf;

import com.atlassian.jira.util.velocity.VelocityRequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for generating anti-XSRF tokens for web forms.
 * <p/>
 * The default implementation (available viw dependency injection) should be good enough for anyone, but this interface is
 * provided just in case anyone wants to implement their own token generation strategy.
 *
 * @since v4.1
 */
public interface XsrfTokenGenerator
{
    /**
     * The name of the XSRF token put ino the HTTP session
     */
    public static final String TOKEN_HTTP_SESSION_KEY = "atlassian.xsrf.token";

    /**
     * The name of the XSRF token parameter sent in on a web request
     */
    public static final String TOKEN_WEB_PARAMETER_KEY = "atl_token";

    /**
     * Gets the token from the current request, generating a new one if none is found
     *
     * @return a valid XSRF form token
     */
    String generateToken();

    /**
     * Gets the token from the current request, optionally generating a new one if none is found
     *
     * @param create true to create token if none is found
     *
     * @return a valid XSRF form token
     */
    String generateToken(boolean create);

    /**
     * Gets the token from the current request, generating a new one if none is found
     *
     * @param request the request the token is being generated for
     *
     * @return a valid XSRF form token
     */
    String generateToken(HttpServletRequest request);

    /**
     * Gets the token from the current request, optionally generating a new one if none is found
     *
     * @param request the request the token is being generated for
     * @param create true to create token if none is found
     *
     * @return a valid XSRF form token
     */
        String generateToken(HttpServletRequest request, boolean create);

    /**
     * Gets the token from the current request, generating a new one if none is found
     *
     * @param request request that contains the form token.
     *
     * @return the token stored in the cookie of this request.
     */
    String getToken(HttpServletRequest request);

    /**
     * Gets the token from the current request, generating a new one if none is found
     *
     * @param request the request the token is being generated for
     *
     * @return a valid XSRF form token
     * @deprecated since 4.3 - use the other two forms of generateToken()
     */
    @Deprecated
    String generateToken(VelocityRequestContext request);

    /**
     * Convenience method which will return the name to be used for a supplied XsrfToken in a request.
     *
     * @return the name in the request for the Xsrf token.
     */
    String getXsrfTokenName();

    /**
     * Validate a form token received as part of a web request
     *
     * @param request the request the token was received in
     * @param token   the token
     *
     * @return true iff the token is valid
     */
    boolean validateToken(HttpServletRequest request, String token);

    /**
     * This returns true of the token was generated by an authenticated user
     * 
     * @param token the XSRF token in question
     * @return true if the token was generated by an authenticated user.
     */
    boolean generatedByAuthenticatedUser(String token);
}