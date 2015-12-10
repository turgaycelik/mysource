package com.atlassian.jira.plugin.projectpanel;

import com.atlassian.jira.plugin.AbstractTabPanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * A project tab panel plugin adds extra panel tabs to JIRA's Browse Project page.
 */
public class ProjectTabPanelModuleDescriptorImpl extends AbstractTabPanelModuleDescriptor<ProjectTabPanel> implements ProjectTabPanelModuleDescriptor
{
    public ProjectTabPanelModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    /**
     * Asserts that module class implements {@link com.atlassian.jira.plugin.projectpanel.ProjectTabPanel}
     *
     * @throws com.atlassian.plugin.PluginParseException if {@link com.atlassian.jira.plugin.projectpanel.ProjectTabPanel} class is not assignable from module class
     */
    protected void assertModuleClass() throws PluginParseException
    {
        assertModuleClassImplements(ProjectTabPanel.class);
    }

    public int compareTo(Object o)
    {
        if (o instanceof ProjectTabPanelModuleDescriptorImpl)
        {
            ProjectTabPanelModuleDescriptorImpl descriptor = (ProjectTabPanelModuleDescriptorImpl) o;
            if (order == descriptor.order)
            {
                return 0;
            }
            else if (order > 0 && order < descriptor.order)
            {
                return -1;
            }
            return 1;
        }
        return -1;
    }

}
