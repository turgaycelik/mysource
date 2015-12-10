package com.atlassian.jira.dev.reference.plugin.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ReferenceServletContextListener implements ServletContextListener
{

    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        servletContextEvent.getServletContext().setAttribute("reference", "upgraded");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent)
    {
        servletContextEvent.getServletContext().setAttribute("reference", "destroyed");
    }
}
