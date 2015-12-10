package com.atlassian.jira.notification;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.auth.Authorisation;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * Allows a plugin to filter notification recipients
 *
 * @since v6.0
 */
public class NotificationFilterModuleDescriptor extends AbstractJiraModuleDescriptor<NotificationFilter>
{
    public NotificationFilterModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }
}
