package com.atlassian.jira.security.auth;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.module.ModuleFactory;

public class AuthorisationModuleDescriptor extends AbstractJiraModuleDescriptor<Authorisation>
{
    public AuthorisationModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }
}
