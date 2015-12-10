package com.atlassian.jira.auditing.handlers;

import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.ProjectCreatedEvent;
import com.atlassian.jira.event.ProjectDeletedEvent;
import com.atlassian.jira.event.ProjectUpdatedEvent;
import com.atlassian.jira.event.project.ProjectAvatarUpdateEvent;
import com.atlassian.jira.event.project.ProjectCategoryChangeEvent;
import com.atlassian.jira.event.role.ProjectRoleDeletedEvent;
import com.atlassian.jira.event.role.ProjectRoleUpdatedEvent;
import com.atlassian.jira.event.project.ProjectCategoryUpdateEvent;

/**
 * @since v6.2
 */
public interface ProjectEventHandler
{
    RecordRequest onProjectCreatedEvent(ProjectCreatedEvent event);

    Option<RecordRequest> onProjectUpdatedEvent(ProjectUpdatedEvent event);

    RecordRequest onProjectDeletedEvent(ProjectDeletedEvent event);

    RecordRequest onProjectCategoryChangeEvent(ProjectCategoryChangeEvent event);

    RecordRequest onProjectRoleUpdatedEvent(ProjectRoleUpdatedEvent event);

    RecordRequest onProjectAvatarUpdateEvent(ProjectAvatarUpdateEvent event);

    Option<RecordRequest> onProjectCategoryUpdateEvent(ProjectCategoryUpdateEvent event);

    RecordRequest onProjectRoleDeletedEvent(ProjectRoleDeletedEvent event);
}
