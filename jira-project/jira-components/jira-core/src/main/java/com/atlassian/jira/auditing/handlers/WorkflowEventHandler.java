package com.atlassian.jira.auditing.handlers;

import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.DraftWorkflowPublishedEvent;
import com.atlassian.jira.event.WorkflowCopiedEvent;
import com.atlassian.jira.event.WorkflowCreatedEvent;
import com.atlassian.jira.event.WorkflowDeletedEvent;
import com.atlassian.jira.event.WorkflowRenamedEvent;
import com.atlassian.jira.event.WorkflowUpdatedEvent;

/**
 * @since v6.2
 */
public interface WorkflowEventHandler
{
    RecordRequest onWorkflowCreatedEvent(WorkflowCreatedEvent event);

    RecordRequest onWorkflowDeletedEvent(WorkflowDeletedEvent event);

    RecordRequest onWorkflowCopiedEvent(WorkflowCopiedEvent event);

    Option<RecordRequest> onWorkflowUpdatedEvent(WorkflowUpdatedEvent event);

    RecordRequest onDraftWorkflowPublishedEvent(DraftWorkflowPublishedEvent event);

    RecordRequest onWorkflowRenamedEvent(WorkflowRenamedEvent event);
}
