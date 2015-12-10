package com.atlassian.jira.web.action;

import javax.annotation.Nullable;

/**
 * Provides a way for clients to sanitise redirect URLs before issuing the redirect.
 *
 * @since 5.1.5
 */
public interface RedirectSanitiser
{
    /**
     * Constructs a <b>safe</b> redirect URL out of user-provided input. This means checking that the URL has an HTTP or
     * HTTPS scheme, and that it does not redirect to a different domain (i.e. not JIRA). If the {@code redirectUrl}
     * does not meet these conditions, this method returns null.
     * <p/>
     * This is used to prevent <a href="https://www.owasp.org/index.php/Open_redirect">Open redirect</a> attacks, which
     * facilitate phishing attacks against JIRA users.
     *
     * @param redirectUrl    a String containing the redirect URL
     * @return a <b>safe</b> redirect URL, or null
     *
     * @since 5.1.5
     */
    @Nullable
    String makeSafeRedirectUrl(@Nullable String redirectUrl);

    /**
     * Returns a boolean indicating whether redirecting to the given URI is allowed or not.
     * <p/>
     * This method returns false if the <code>redirectUri</code> is an absolute URI and it points to a domain that is not
     * this JIRA instance's domain, and true otherwise.
     * If the uri is in the form //xxx then it is not allowed as per JRA-27405.
     *
     * @param redirectUri a String containing a URI
     * @return a boolean indicating whether redirecting to the given URI should be allowed or not
     * @since v6.2
     */
    boolean canRedirectTo(@Nullable String redirectUri);
}
