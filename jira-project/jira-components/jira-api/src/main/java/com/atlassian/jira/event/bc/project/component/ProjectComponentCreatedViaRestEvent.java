package com.atlassian.jira.event.bc.project.component;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.jira.bc.project.component.ProjectComponent;

/**
 * Event indicating if a project component is created via REST (which means project admin in most cases using the
 * RESTful table). This will be used to establish a baseline to compare against creating components inline.
 *
 * @since v6.3
 */
@EventName("administration.projects.components.component.created.rest")
public class ProjectComponentCreatedViaRestEvent extends AbstractProjectComponentEvent
{
    public ProjectComponentCreatedViaRestEvent(final ProjectComponent projectComponent)
    {
        super(projectComponent);
    }
}
