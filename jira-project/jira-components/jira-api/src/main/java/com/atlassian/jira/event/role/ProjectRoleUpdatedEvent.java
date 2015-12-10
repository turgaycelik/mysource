package com.atlassian.jira.event.role;

import javax.annotation.Nonnull;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;

/**
 * @since v6.3
 */
public class ProjectRoleUpdatedEvent extends AbstractProjectRoleEvent
{
    private final Project project;
    private final ProjectRoleActors newRoleActors;
    private final ProjectRoleActors originalRoleActors;

    public ProjectRoleUpdatedEvent(@Nonnull final Project project, @Nonnull final ProjectRole projectRole,
                                   @Nonnull final ProjectRoleActors newRoleActors, @Nonnull final ProjectRoleActors originalRoleActors)
    {
        super(projectRole);
        this.project = project;
        this.newRoleActors = newRoleActors;
        this.originalRoleActors = originalRoleActors;
    }

    @Nonnull
    public Project getProject()
    {
        return project;
    }

    @Nonnull
    public ProjectRoleActors getRoleActors()
    {
        return newRoleActors;
    }

    @Nonnull
    public ProjectRoleActors getOriginalRoleActors()
    {
        return originalRoleActors;
    }
}
