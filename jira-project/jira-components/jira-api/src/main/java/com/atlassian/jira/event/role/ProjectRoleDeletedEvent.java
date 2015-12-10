package com.atlassian.jira.event.role;

import com.atlassian.jira.security.roles.ProjectRole;

/**
 * @since v6.3
 */
public class ProjectRoleDeletedEvent extends AbstractProjectRoleEvent
{
    public ProjectRoleDeletedEvent(ProjectRole projectRole)
    {
        super(projectRole);
    }
}
