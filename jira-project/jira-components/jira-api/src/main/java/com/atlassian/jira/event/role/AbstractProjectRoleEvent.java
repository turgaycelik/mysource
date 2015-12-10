package com.atlassian.jira.event.role;

import javax.annotation.Nonnull;

import com.atlassian.jira.security.roles.ProjectRole;

/**
 * @since v6.3
 */
public class AbstractProjectRoleEvent
{
    protected final ProjectRole projectRole;

    public AbstractProjectRoleEvent(@Nonnull final ProjectRole projectRole) {this.projectRole = projectRole;}

    @Nonnull
    public ProjectRole getProjectRole()
    {
        return projectRole;
    }
}
