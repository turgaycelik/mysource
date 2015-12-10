package com.atlassian.jira.plugin.webfragment.descriptors;

import com.atlassian.jira.plugin.webfragment.model.JiraWebIcon;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebIcon;
import com.atlassian.plugin.web.model.WebLink;

public class JiraWebItemModuleDescriptor extends JiraAbstractWebFragmentModuleDescriptor implements WebItemModuleDescriptor
{
    public JiraWebItemModuleDescriptor(final JiraAuthenticationContext authenticationContext, final WebInterfaceManager webInterfaceManager)
    {
        super(authenticationContext, new DefaultWebItemModuleDescriptor(webInterfaceManager));
    }

    public String getSection()
    {
        return getItemDescriptor().getSection();
    }

    public WebLink getLink()
    {
        final WebLink link = getItemDescriptor().getLink();
        if (link != null)
        {
            return new JiraWebLink(link, authenticationContext);
        }

        return null;
    }

    public WebIcon getIcon()
    {
        final WebIcon icon = getItemDescriptor().getIcon();
        if (icon != null)
        {
            return new JiraWebIcon(icon, authenticationContext);
        }

        return null;
    }

    public String getStyleClass()
    {
        return getItemDescriptor().getStyleClass();
    }

    private WebItemModuleDescriptor getItemDescriptor()
    {
        if (getDecoratedDescriptor() instanceof DefaultWebItemModuleDescriptor)
        {
            return (WebItemModuleDescriptor) getDecoratedDescriptor();
        }

        return null; // bad :)
    }
}
