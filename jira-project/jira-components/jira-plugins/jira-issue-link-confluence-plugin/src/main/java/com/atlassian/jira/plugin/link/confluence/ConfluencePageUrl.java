package com.atlassian.jira.plugin.link.confluence;

import com.atlassian.applinks.api.ApplicationLink;

import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.jira.util.UriMatcher.isBaseEqual;

/**
 * Represents an URL to a Confluence page.
 */
public class ConfluencePageUrl
{
    private final URI pageUri;
    private final ApplicationLink appLink;

    private ConfluencePageUrl(final URI pageUri, final ApplicationLink appLink)
    {
        this.pageUri = pageUri;
        this.appLink = appLink;
    }

    public static ConfluencePageUrl build(final String pageUriAsString, final ApplicationLink appLink)
    {
        URI pageUri;
        try
        {
            pageUri = new URI(pageUriAsString);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }

        if (!isUriValidForAppLink(pageUri, appLink))
        {
            throw new IllegalArgumentException("The page uri must be based on either the application link's display uri or the application link's rpc uri");
        }

        return new ConfluencePageUrl(pageUri, appLink);

    }

    private static boolean isUriValidForAppLink(final URI pageUri, final ApplicationLink appLink)
    {
        return isBaseEqual(appLink.getDisplayUrl(), pageUri) || isBaseEqual(appLink.getRpcUrl(), pageUri);
    }

    /**
     * Returns the url for the Confluence page, rebased so it starts with the rpc url of the given application link.
     * @param appLink
     * @return
     */
    public String getUrlRebasedToRpcUrl()
    {
        String urlAsString = pageUri.toASCIIString();

        if (isBaseEqual(appLink.getDisplayUrl(), pageUri))
        {
            return urlAsString.replace(appLink.getDisplayUrl().toASCIIString(), appLink.getRpcUrl().toASCIIString());
        }

        return urlAsString;
    }
}
