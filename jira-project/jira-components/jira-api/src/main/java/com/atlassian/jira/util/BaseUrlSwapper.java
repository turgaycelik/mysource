package com.atlassian.jira.util;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * Utility class for swapping the base of a URL.
 *
 * @since v5.0
 */
public class BaseUrlSwapper
{
    /**
     * Swap the base of the given url from the oldBaseUrl to the newBaseUrl. If the url does not start with the
     * oldBaseUrl, the original url is returned. If the url is null, null is returned.
     *
     * @param url the url to swap the base of
     * @param oldBaseUrl the base url of the given url
     * @param newBaseUrl the new base url to set on the given url
     * @return the url with the newBaseUrl as a base
     */
    public static String swapBaseUrl(final String url, final String oldBaseUrl, String newBaseUrl)
    {
        if (url == null)
        {
            return null;
        }

        newBaseUrl = fixMismatchedTrailingSlash(oldBaseUrl, newBaseUrl);

        if (!newBaseUrl.equals(oldBaseUrl) && url.startsWith(oldBaseUrl))
        {
            return url.replace(oldBaseUrl, newBaseUrl);
        }

        return url;
    }

    private static String fixMismatchedTrailingSlash(final String oldBaseUrl, final String newBaseUrl)
    {
        final boolean oldBaseUrlHasTrailingSlash = oldBaseUrl.endsWith("/");
        final boolean newBaseUrlHasTrailingSlash = newBaseUrl.endsWith("/");

        if (oldBaseUrlHasTrailingSlash && !newBaseUrlHasTrailingSlash)
        {
            return newBaseUrl + "/";
        }
        else if (!oldBaseUrlHasTrailingSlash && newBaseUrlHasTrailingSlash)
        {
            return newBaseUrl.substring(0, newBaseUrl.length() - 1);
        }

        return newBaseUrl;
    }

    /**
     * Swap the base of the given url to be the display url of the given application link. The swap is made only when
     * the base of the given url is the RPC url of the given application link. If no swap is made, the original url is
     * returned. If the url is null, null is returned.
     *
     * @param url the url to swap the base of
     * @param appLink the application link
     * @return the url with the display url as a base
     */
    public static String swapRpcUrlToDisplayUrl(final String url, final ApplicationLink appLink)
    {
        final String rpcUrl = appLink.getRpcUrl().toASCIIString();
        final String displayUrl = appLink.getDisplayUrl().toASCIIString();

        return swapBaseUrl(url, rpcUrl, displayUrl);
    }
}
