package com.atlassian.jira.plugin.servlet;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.servlet.ServletModuleManager;

public class ServletModuleContainerServlet extends com.atlassian.plugin.servlet.ServletModuleContainerServlet
{
    protected ServletModuleManager getServletModuleManager()
    {
        //JRA-16230: Do not cache the result of this lookup. Servlets should not hold onto JIRA components as they are
        //not reinitialised when JIRA's components are reinitialised after an import. This can leave the servlet using old
        //versions of a JIRA component.  In this case, it caused JIRA to contine using servlet plugins that should
        //have been destroyed after an import.
        return ComponentAccessor.getComponentOfType(ServletModuleManager.class);
    }
}
