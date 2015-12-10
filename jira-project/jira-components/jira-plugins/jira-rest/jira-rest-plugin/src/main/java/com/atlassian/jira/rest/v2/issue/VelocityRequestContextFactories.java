package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Helper methods for working with {@link com.atlassian.jira.util.velocity.VelocityRequestContextFactory} instances.
 *
 * @since v4.2
 */
public class VelocityRequestContextFactories
{
    /**
     * Prevents instantiation.
     */
    private VelocityRequestContextFactories()
    {
        // empty
    }

    /**
     * Returns the request's canonical base URL as a URI instance. This corresponds to JIRA's base URI.
     *
     * @param velocityRequestContextFactory the current VelocityRequestContextFactory
     * @return a URI
     */
    public static URI getBaseURI(VelocityRequestContextFactory velocityRequestContextFactory)
    {
        String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
        try
        {
            return new URI(baseUrl);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Error in canonicalBaseUrl: " + baseUrl, e);
        }
    }
}
