package com.atlassian.jira.plugin.profile;

import com.atlassian.crowd.embedded.api.User;

/**
 * Renders html for a view profile panel.
 *
 * @since v3.12
 */
public class DefaultProfilePanel implements ViewProfilePanel
{

    private ViewProfilePanelModuleDescriptor moduleDescriptor;

    public void init(ViewProfilePanelModuleDescriptor moduleDescriptor)
    {
        this.moduleDescriptor = moduleDescriptor;
    }

    public String getHtml(User profileUser)
    {
        return moduleDescriptor.getHtml(VIEW_TEMPLATE);
    }

    public String getTabKey()
    {
        return moduleDescriptor.getTabKey();
    }

}
