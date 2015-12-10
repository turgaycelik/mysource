package com.atlassian.jira.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * The listener implementation to acquire and hold the ServletContext.
 *
 * @since v3.13
 */
public class ServletContextProviderListener implements ServletContextListener
{
    private static volatile ServletContext context;

    /**
     * Will return null only if we are not running in a ServletContext or if the web app is not properly configured.
     *
     * @return the ServletContext
     */
    static ServletContext getServletContext()
    {
        return context;
    }

    public void contextInitialized(final ServletContextEvent event)
    {
        context = event.getServletContext();
    }

    public void contextDestroyed(final ServletContextEvent event)
    {
        context = null;
    }
}
