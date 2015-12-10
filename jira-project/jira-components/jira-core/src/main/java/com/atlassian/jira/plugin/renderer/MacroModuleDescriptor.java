package com.atlassian.jira.plugin.renderer;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.module.ModuleFactory;

public class MacroModuleDescriptor extends AbstractJiraModuleDescriptor<Object>
{
    public MacroModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public Object createModule()
    {
        // First look for the macro in the parent container, this needed for the code macro which has
        // a list which initializes it.
        Object module = ComponentAccessor.getComponentOfType(getModuleClass());

        if (module == null)
        {
            module = super.createModule();
        }
        return module;
    }

    public boolean hasHelp()
    {
        return getResourceDescriptor("velocity", "help") != null;
    }

    public String getHelpSection()
    {
        return getResourceDescriptor("velocity", "help").getParameter("help-section");
    }

    public String getHelp()
    {
        return getHtml("help");
    }
}
