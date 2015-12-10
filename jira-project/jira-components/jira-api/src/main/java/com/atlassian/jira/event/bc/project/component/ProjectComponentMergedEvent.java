package com.atlassian.jira.event.bc.project.component;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.bc.project.component.ProjectComponent;

/**
 * Event indicating that issues of deleted project component were assigned to another component
 *
 * @since v6.3
 */
public class ProjectComponentMergedEvent extends AbstractProjectComponentEvent
{
    private final ProjectComponent mergedProjectComponent;

    @Internal
    public ProjectComponentMergedEvent(@Nonnull final ProjectComponent projectComponent, @Nonnull final ProjectComponent mergedProjectComponent)
    {
        super(projectComponent);
        this.mergedProjectComponent = mergedProjectComponent;
    }

    @Nonnull
    public ProjectComponent getMergedProjectComponent()
    {
        return mergedProjectComponent;
    }
}
