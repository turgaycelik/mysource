package com.atlassian.jira.security.plugin;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.module.ModuleFactory;

public class ProjectPermissionOverrideModuleDescriptorImpl extends AbstractJiraModuleDescriptor<ProjectPermissionOverride> implements ProjectPermissionOverrideModuleDescriptor
{
    public ProjectPermissionOverrideModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }
}
