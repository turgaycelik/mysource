package com.atlassian.jira.auditing.handlers;

import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.bc.project.component.ProjectComponentCreatedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentDeletedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentMergedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentUpdatedEvent;

/**
 * @since v6.3
 */
public interface ProjectComponentEventHandler
{
    RecordRequest onProjectComponentCreatedEvent(ProjectComponentCreatedEvent event);

    Option<RecordRequest> onProjectComponentUpdatedEvent(ProjectComponentUpdatedEvent event);

    RecordRequest onProjectComponentMergedEvent(ProjectComponentMergedEvent event);

    RecordRequest onProjectComponentDeletedEvent(ProjectComponentDeletedEvent event);
}
