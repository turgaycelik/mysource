package com.atlassian.jira.plugin.user;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * Descriptor definition for the pre-delete-user-errors plugin point
 * @since v6.0
 */
public class PreDeleteUserErrorsModuleDescriptor extends AbstractJiraModuleDescriptor<PreDeleteUserErrors>
{
    public PreDeleteUserErrorsModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }
}