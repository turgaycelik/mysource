package com.atlassian.jira.util;

import java.net.URI;

/**
 * Utility class for matching URIs.
 *
 * @since v5.0
 */
public class UriMatcher
{
    /**
     * Checks if the testUri belongs to the baseUri. For example, www.foo.com/bar belongs to www.foo.com.
     *
     * @param baseUri the base URI
     * @param testUri the URI to match against the base URI
     * @return true if the testUri belongs to the baseUri, false if otherwise
     */
    public static boolean isBaseEqual(URI baseUri, URI testUri)
    {
        baseUri = baseUri.normalize();
        testUri = testUri.normalize();

        final boolean schemeEqual = baseUri.getScheme().equalsIgnoreCase(testUri.getScheme());
        final boolean hostEqual = baseUri.getHost().equalsIgnoreCase(testUri.getHost());
        final boolean portEqual = normalizedPort(baseUri) == normalizedPort(testUri);
        final boolean pathEqual = testUri.getPath().startsWith(baseUri.getPath());

        return schemeEqual && hostEqual && portEqual && pathEqual;
    }

    private static int normalizedPort(final URI uri)
    {
        if (uri.getPort() == -1)
        {
            if ("http".equalsIgnoreCase(uri.getScheme()))
            {
                return 80;
            }
            if ("https".equalsIgnoreCase(uri.getScheme()))
            {
                return 443;
            }
        }
        return uri.getPort();
    }
}
