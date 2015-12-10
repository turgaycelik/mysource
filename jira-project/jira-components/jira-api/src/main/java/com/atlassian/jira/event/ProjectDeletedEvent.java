package com.atlassian.jira.event;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Event that is triggered when a project is deleted.
 *
 * @since v5.1
 */
@PublicApi
public final class ProjectDeletedEvent extends AbstractProjectEvent
{
    @Internal
    public ProjectDeletedEvent(@Nullable final ApplicationUser user, @Nonnull final Project oldProject)
    {
        super(user, oldProject);
    }

    /**
     * @return the project ID
     */
    @Nonnull
    public Long getId()
    {
        return getProject().getId();
    }

    /**
     * @return the project key
     */
    @Nonnull
    public String getKey()
    {
        return getProject().getKey();
    }
}
