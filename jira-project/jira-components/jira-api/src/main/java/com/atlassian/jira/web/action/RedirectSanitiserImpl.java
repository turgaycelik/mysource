package com.atlassian.jira.web.action;

import javax.annotation.Nullable;

import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import org.apache.log4j.Logger;

public class RedirectSanitiserImpl implements RedirectSanitiser
{
    // Logs to atlassian-jira-security.log - inherited from SafeRedirectChecker
    private static final Logger securityLog = Logger.getLogger("com.atlassian.jira.login.security");

    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public RedirectSanitiserImpl(VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    @Override
    public boolean canRedirectTo(@Nullable String redirectUri)
    {
        if (redirectUri == null)
        {
            return true;
        }

        // JRA-27405 scheme relative is a no-no
        if (redirectUri.startsWith("//"))
        {
            return false;
        }

        // redirecting to another page in this JIRA instance is OK
        if (redirectUri.startsWith(getCanonicalBaseURL()))
        {
            return true;
        }

        // true if the URL has a scheme
        boolean hasScheme = redirectUri.contains(":");

        // we already know that this URI does not point back to JIRA, because we have checked it against the canonical
        // base URL above. so if it has a scheme then the redirect is denied (could be javascript:, or anything else
        // that we don't like)
        return !hasScheme;
    }

    @Nullable
    @Override
    public String makeSafeRedirectUrl(@Nullable String redirectUrl)
    {
        if (redirectUrl == null)
        {
            // NULLs are safe...
            return null;
        }

        if (!canRedirectTo(redirectUrl))
        {
            securityLog.warn("Potential malicious redirect detected: " + redirectUrl);
            return null;
        }

        return redirectUrl;
    }

    /**
     * Returns the canonical base URL for JIRA.
     *
     * @return a String containing the canonical base URL
     */
    protected String getCanonicalBaseURL()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
    }
}
