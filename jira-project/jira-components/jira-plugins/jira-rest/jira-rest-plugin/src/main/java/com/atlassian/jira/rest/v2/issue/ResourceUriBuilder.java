package com.atlassian.jira.rest.v2.issue;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * This class is used for building URI's for JIRA's REST resources.
 *
 * @since v4.2
 */
public class ResourceUriBuilder
{
    public ResourceUriBuilder()
    {
        // empty
    }

    /**
     * Creates a new URI for the given resource class and keys, using the provided UriInfo.
     *
     * @param context a UriInfo
     * @param resourceClass the resource class
     * @param resourceKey the resource key
     * @return a URI
     */
    public URI build(UriInfo context, Class<?> resourceClass, String resourceKey)
    {
        return context.getBaseUriBuilder().path(resourceClass).path(resourceKey).build();
    }

    public URI build(final UriBuilder builder, Class<?> resourceClass, String resourceKey)
    {
        return builder.path(resourceClass).path(resourceKey).build();
    }
}
