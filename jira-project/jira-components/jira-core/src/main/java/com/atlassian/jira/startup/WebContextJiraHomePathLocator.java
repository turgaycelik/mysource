package com.atlassian.jira.startup;

import com.atlassian.jira.web.ServletContextProvider;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;

/**
 * Attempts to find a jira-home configured within our web context.
 *
 * @since v4.0
 */
public class WebContextJiraHomePathLocator implements JiraHomePathLocator
{
    private static final Logger log = Logger.getLogger(WebContextJiraHomePathLocator.class);

    public String getJiraHome()
    {
        // Get the Servlet context
        ServletContext servletContext = ServletContextProvider.getServletContext();
        if (servletContext == null)
        {
            // This should never happen in production, but can happen with naughty unit tests.
            log.error("No ServletContext exists - cannot check for jira.home.");
            return null;
        }
        return servletContext.getInitParameter(Property.JIRA_HOME);
    }

    public String getDisplayName()
    {
        return "Web Context";
    }
}
