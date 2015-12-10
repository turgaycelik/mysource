package com.atlassian.jira.event.project;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Central dispatcher for project events
 *
 * @since v6.1
 */
public interface ProjectEventManager
{
    /**
     * Dispatches {@link com.atlassian.jira.event.ProjectUpdatedEvent}.
     */
    void dispatchProjectUpdated(ApplicationUser user, Project newProject, Project oldProject);

    /**
     * Dispatches {@link com.atlassian.jira.event.ProjectCreatedEvent}.
     */
    void dispatchProjectCreated(ApplicationUser user, Project newProject);

    /**
     * Dispatches {@link com.atlassian.jira.event.ProjectDeletedEvent}.
     */
    void dispatchProjectDeleted(ApplicationUser user, Project oldProject);

}
