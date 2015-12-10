package com.atlassian.jira.plugin.webfragment.descriptors;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebSectionModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;

public class JiraWebSectionModuleDescriptor extends JiraAbstractWebFragmentModuleDescriptor implements WebSectionModuleDescriptor
{
    public JiraWebSectionModuleDescriptor(final JiraAuthenticationContext authenticationContext, final WebInterfaceManager webInterfaceManager)
    {
        super(authenticationContext, new DefaultWebSectionModuleDescriptor(webInterfaceManager));
    }

    public String getLocation()
    {
        return getSectionDescriptor().getLocation();
    }

    private WebSectionModuleDescriptor getSectionDescriptor()
    {
        if (getDecoratedDescriptor() instanceof DefaultWebSectionModuleDescriptor)
        {
            return (WebSectionModuleDescriptor) getDecoratedDescriptor();
        }

        return null; // bad :)
    }
}
