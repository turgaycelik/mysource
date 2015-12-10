package com.atlassian.jira.event.bc.project.component;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.bc.project.component.ProjectComponent;

/**
 * Event indicating an project component has been deleted
 *
 * @since v5.1
 */
public class ProjectComponentDeletedEvent extends AbstractProjectComponentEvent
{
    @Internal
    public ProjectComponentDeletedEvent(ProjectComponent projectComponent)
    {
        super(projectComponent);
    }
}
