package com.atlassian.jira.event;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Event that is triggered when a project is created.
 */
@PublicApi
public final class ProjectCreatedEvent extends AbstractProjectEvent
{
    @Internal
    public ProjectCreatedEvent(@Nullable final ApplicationUser user, @Nonnull final Project newProject)
    {
        super(user, newProject);
    }

    /**
     * @return the project ID
     */
    @Nonnull
    public Long getId()
    {
        return getProject().getId();
    }
}
