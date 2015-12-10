package com.atlassian.jira.dashboard;

import com.atlassian.gadgets.opensocial.spi.Whitelist;
import com.atlassian.jira.bc.whitelist.WhitelistService;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.net.URI;

/**
 * A JIRA specific whitelist implementation which uses the whitelist configuration provided by the
 * whitelist service as well as localhost for gadget loopback requests (there shouldn't be any but just in case).
 *
 * @since v4.3
 */
public class JiraWhitelist implements Whitelist
{
    private final WhitelistService whitelistService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public JiraWhitelist(final WhitelistService whitelistService, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.whitelistService = whitelistService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    @Override
    public boolean allows(URI uri)
    {
        final String uriString = uri.normalize().toASCIIString().toLowerCase();
        final String canonicalBaseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
        return uriString.startsWith(canonicalBaseUrl) || whitelistService.isAllowed(uri);
    }

}
