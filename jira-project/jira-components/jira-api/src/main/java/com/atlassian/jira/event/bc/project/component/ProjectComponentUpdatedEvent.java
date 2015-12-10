package com.atlassian.jira.event.bc.project.component;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.bc.project.component.ProjectComponent;

import com.google.common.base.Preconditions;

/**
 * Event indicating an project component has been updated
 *
 * @since v5.1
 */
public class ProjectComponentUpdatedEvent extends AbstractProjectComponentEvent
{
    private final ProjectComponent oldProjectComponent;

    @Internal
    public ProjectComponentUpdatedEvent(@Nonnull ProjectComponent projectComponent, @Nonnull ProjectComponent oldProjectComponent)
    {
        super(projectComponent);

        if (oldProjectComponent == null)
        {
            throw new IllegalArgumentException("oldProjectComponent must not be null");
        }

        this.oldProjectComponent = oldProjectComponent;
    }

    public ProjectComponent getOldProjectComponent()
    {
        return oldProjectComponent;
    }
}
