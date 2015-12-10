package com.atlassian.jira.issue.fields.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.event.bc.project.component.AbstractProjectComponentEvent;

/**
 * Event to throw when a component is created inline
 */
@EventName ("administration.projects.components.component.created.inline")
public class ComponentCreatedInlineEvent extends AbstractProjectComponentEvent
{
    public ComponentCreatedInlineEvent(final ProjectComponent projectComponent)
    {
        super(projectComponent);
    }
}
