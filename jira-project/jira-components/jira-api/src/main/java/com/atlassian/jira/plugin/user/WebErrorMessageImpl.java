package com.atlassian.jira.plugin.user;

import com.atlassian.annotations.PublicApi;

import java.net.URI;

/**
 * Basic WebErrorMessage Implementation.
 * @since 6.0
 */
@PublicApi
public class WebErrorMessageImpl implements WebErrorMessage
{

    final private String description;
    final private String snippet;
    final private URI furtherInformationURI;

    public WebErrorMessageImpl(final String description, final String snippet, final URI furtherInformationURI)
    {
        this.description = description;
        this.snippet = snippet;
        this.furtherInformationURI = furtherInformationURI;
    }

    public String getDescription()
    {
        return description;
    }

    public String getSnippet()
    {
        return snippet;
    }

    public URI getURI()
    {
        return furtherInformationURI;
    }
}
