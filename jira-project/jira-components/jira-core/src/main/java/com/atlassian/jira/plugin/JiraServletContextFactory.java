package com.atlassian.jira.plugin;

import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.plugin.servlet.ServletContextFactory;

import javax.servlet.ServletContext;

/**
 * @since v4.0
 */
public class JiraServletContextFactory implements ServletContextFactory
{
    public ServletContext getServletContext()
    {
        return ServletContextProvider.getServletContext();
    }
}
