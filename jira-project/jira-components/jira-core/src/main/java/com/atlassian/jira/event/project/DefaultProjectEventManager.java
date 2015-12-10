package com.atlassian.jira.event.project;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ProjectCreatedEvent;
import com.atlassian.jira.event.ProjectDeletedEvent;
import com.atlassian.jira.event.ProjectUpdatedEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

public class DefaultProjectEventManager implements ProjectEventManager
{
    private final EventPublisher eventPublisher;

    public DefaultProjectEventManager(final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void dispatchProjectUpdated(final ApplicationUser user, final Project newProject, final Project oldProject)
    {
        eventPublisher.publish(new ProjectUpdatedEvent(user, newProject, oldProject));
        eventPublisher.publish(new ProjectUpdatedCheckProjectKeyAnalyticsEvent(oldProject.getKey(), newProject.getKey()));
    }

    @Override
    public void dispatchProjectCreated(final ApplicationUser user, final Project newProject)
    {
        eventPublisher.publish(new ProjectCreatedEvent(user, newProject));
    }

    @Override
    public void dispatchProjectDeleted(final ApplicationUser user, final Project oldProject)
    {
        eventPublisher.publish(new ProjectDeletedEvent(user, oldProject));
    }
}
