package com.atlassian.jira.security.auth.trustedapps;

/**
 * The basic SimpleTrustedApplication information. For more useful SimpleTrustedApplication data see {@link TrustedApplicationInfo}
 *
 * @since v3.12
 */
public interface SimpleTrustedApplication
{
    long getId();

    String getApplicationId();

    long getTimeout();

    String getPublicKey();

    String getName();

    String getIpMatch();

    String getUrlMatch();
}
