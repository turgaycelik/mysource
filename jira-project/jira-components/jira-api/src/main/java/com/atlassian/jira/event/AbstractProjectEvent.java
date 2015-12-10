package com.atlassian.jira.event;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Basic representation of something that happens to a {@link Project}, such as a modification. Event listeners
 * register to receive these.
 *
 * @since v6.1
 */
@PublicApi
public abstract class AbstractProjectEvent extends AbstractEvent implements ProjectRelatedEvent
{
    private final ApplicationUser user;
    private final Project project;

    @Internal
    public AbstractProjectEvent(@Nullable final ApplicationUser user, @Nonnull final Project project)
    {
        Preconditions.checkNotNull(project, "project must not be null");
        this.user = user;
        this.project = project;
    }

    @Override
    @Nonnull
    public Project getProject()
    {
        return project;
    }

    /**
     * Returns the user who initiated this event.
     *
     * @return the user who initiated this event.
     */
    @Nullable
    public ApplicationUser getUser()
    {
        return user;
    }

    /**
     * Note: this will not compare the time stamps of two events - only everything else.
     */
    @SuppressWarnings ( { "RedundantIfStatement" })
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AbstractProjectEvent event = (AbstractProjectEvent) o;

        if (getParams() != null ? !getParams().equals(event.getParams()) : event.getParams() != null)
        {
            return false;
        }
        if (project != null ? !project.equals(event.project) : event.project != null)
        {
            return false;
        }
        if (user != null ? !user.equals(event.user) : event.user != null)
        {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 29 * result + (project != null ? project.hashCode() : 0);
        result = 29 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
