package com.atlassian.jira.web.pagebuilder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for a decorator that runs over JSPs
 *
 * @since v6.1
 */
public interface JspDecorator
{
    /**
     * Sets this decorator's context
     * @param servletContext servlet context
     * @param request request
     * @param response response
     */
    void setContext(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response);
}
