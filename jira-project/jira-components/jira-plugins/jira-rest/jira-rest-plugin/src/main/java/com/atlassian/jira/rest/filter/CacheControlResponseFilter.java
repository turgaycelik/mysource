package com.atlassian.jira.rest.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Sets caching headers if there aren't any.
 */
class CacheControlResponseFilter implements ContainerResponseFilter
{
    private static final Logger log = LoggerFactory.getLogger(CacheControlResponseFilter.class);

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
    {
        MultivaluedMap<String, Object> headers = response.getHttpHeaders();
        if (!headers.containsKey("Cache-Control") && !headers.containsKey("Expires"))
        {
            log.trace("Response does not have caching headers, adding 'Cache-Control: no-cache, no-store'");
            headers.putSingle("Cache-Control", "no-cache, no-store, no-transform");
        }

        return response;
    }
}
