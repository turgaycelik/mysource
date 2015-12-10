package com.atlassian.jira.event;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.project.Project;

/**
 * Common interface which should be implemented for all project related events.
 *
 * @since v6.1
 */
public interface ProjectRelatedEvent extends JiraEvent
{

    Project getProject();

}
