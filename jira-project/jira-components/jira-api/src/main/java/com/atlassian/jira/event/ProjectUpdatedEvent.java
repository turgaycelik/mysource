package com.atlassian.jira.event;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Event indicating a project has been updated
 *
 * @since v6.0
 */
@PublicApi
public class ProjectUpdatedEvent extends AbstractProjectEvent
{
    private final Project oldProject;

    @Internal
    public ProjectUpdatedEvent(@Nullable final ApplicationUser user, @Nonnull final Project project, @Nonnull final Project oldProject)
    {
        super(user, project);
        Preconditions.checkNotNull(oldProject, "oldProject must not be null");
        this.oldProject = oldProject;
    }

    @Nonnull
    public Project getOldProject()
    {
        return oldProject;
    }
}
