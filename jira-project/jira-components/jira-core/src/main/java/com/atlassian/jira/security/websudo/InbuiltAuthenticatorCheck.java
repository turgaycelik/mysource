package com.atlassian.jira.security.websudo;

import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.config.SecurityConfigFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO delete once CONFDEV-684 (CONF-20958) fixed properly
 *
 */
public class InbuiltAuthenticatorCheck
{
    private static final boolean customAuthenticator;
    static
    {
        Set<String> supportedAuthenticators = new HashSet<String>();
        supportedAuthenticators.add("com.atlassian.jira.security.login.JiraSeraphAuthenticator");
        supportedAuthenticators.add("com.atlassian.jira.security.login.SSOSeraphAuthenticator");
        Authenticator authenticator = SecurityConfigFactory.getInstance().getAuthenticator();
        customAuthenticator = !supportedAuthenticators.contains(authenticator.getClass().getName());


    }
    public static boolean hasCustomAuthenticator()
    {
        return customAuthenticator;
    }
}
