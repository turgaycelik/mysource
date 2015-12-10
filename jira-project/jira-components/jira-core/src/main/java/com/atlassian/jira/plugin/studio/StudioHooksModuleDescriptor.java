package com.atlassian.jira.plugin.studio;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * Module descriptor that creates {@link StudioHooks}.
 *
 * @since v4.4.2
 */
public class StudioHooksModuleDescriptor extends AbstractJiraModuleDescriptor<StudioHooks>
{
    public StudioHooksModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }
}
