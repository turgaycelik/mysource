package com.atlassian.jira.web.pagebuilder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for creating and initialising page builders.
 *
 * @since v6.1
 */
public interface PageBuilderServiceSpi
{
    /**
     * Creates a new page builder for the current request. If this has already been called for the current request,
     * this is a no-op.
     * @param request http request
     * @param response http response
     * @param decoratorListener decorator listener
     * @param servletContext servlet context
     */
    public void initForRequest(HttpServletRequest request, HttpServletResponse response,
            DecoratorListener decoratorListener, ServletContext servletContext);

    /**
     * Clears the page builder for the current request.
     */
    public void clearForRequest();

    /**
     * Returns the page builder spi for the current request
     * @throws IllegalStateException if no page builder spi has been set for the current request
     * @return request-local page builder spi
     */
    public PageBuilderSpi getSpi();
}
