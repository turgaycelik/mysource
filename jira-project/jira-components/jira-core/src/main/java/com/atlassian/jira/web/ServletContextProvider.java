package com.atlassian.jira.web;

import javax.servlet.ServletContext;

/**
 * A means to acquire the ServletContext.
 *
 * @since v3.13
 */
public class ServletContextProvider
{
    public static ServletContext getServletContext()
    {
        return ServletContextProviderListener.getServletContext();
    }
}
