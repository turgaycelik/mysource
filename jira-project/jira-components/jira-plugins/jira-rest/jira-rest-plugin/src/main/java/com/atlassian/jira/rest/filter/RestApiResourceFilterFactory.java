package com.atlassian.jira.rest.filter;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import java.util.List;

/**
 * Factory for REST API filters.
 *
 * @since v5.2
 */
public class RestApiResourceFilterFactory implements ResourceFilterFactory
{
    @Override
    public List<ResourceFilter> create(AbstractMethod am)
    {
        return ImmutableList.<ResourceFilter>of(new ResourceFilter()
        {
            @Override
            public ContainerRequestFilter getRequestFilter()
            {
                return null;
            }

            @Override
            public ContainerResponseFilter getResponseFilter()
            {
                return new CacheControlResponseFilter();
            }
        });
    }
}
