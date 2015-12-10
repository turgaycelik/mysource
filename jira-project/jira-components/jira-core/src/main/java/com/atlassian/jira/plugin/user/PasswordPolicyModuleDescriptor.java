package com.atlassian.jira.plugin.user;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * Descriptor definition for the {@code password-policy} plugin point.
 *
 * @since v6.1
 */
public class PasswordPolicyModuleDescriptor extends AbstractJiraModuleDescriptor<PasswordPolicy>
{
    public PasswordPolicyModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }
}
