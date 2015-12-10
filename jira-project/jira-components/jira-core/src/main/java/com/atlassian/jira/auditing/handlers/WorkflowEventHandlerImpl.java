package com.atlassian.jira.auditing.handlers;

import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.DraftWorkflowPublishedEvent;
import com.atlassian.jira.event.WorkflowCopiedEvent;
import com.atlassian.jira.event.WorkflowCreatedEvent;
import com.atlassian.jira.event.WorkflowDeletedEvent;
import com.atlassian.jira.event.WorkflowRenamedEvent;
import com.atlassian.jira.event.WorkflowUpdatedEvent;
import com.atlassian.jira.workflow.JiraWorkflow;

import com.google.common.collect.ImmutableList;

/**
 * @since v6.2
 */
public class WorkflowEventHandlerImpl implements WorkflowEventHandler
{
    @Override
    public RecordRequest onWorkflowCreatedEvent(final WorkflowCreatedEvent event)
    {
        return workflowCreated(event.getWorkflow());
    }

    @Override
    public RecordRequest onWorkflowCopiedEvent(final WorkflowCopiedEvent event)
    {
        return workflowCreated(event.getNewWorkflow());
    }

    @Override
    public Option<RecordRequest> onWorkflowUpdatedEvent(WorkflowUpdatedEvent event)
    {
        if (event.getWorkflow().isDraftWorkflow())
        {
            return Option.none(); //for now we ignore any changes to draft workflows
        }
        else
        {
            return Option.some(workflowUpdated(event.getOriginalWorkflow(), event.getWorkflow()));
        }
    }

    @Override
    public RecordRequest onDraftWorkflowPublishedEvent(DraftWorkflowPublishedEvent event)
    {
        return workflowUpdated(event.getOriginalWorkflow(), event.getWorkflow());
    }

    @Override
    public RecordRequest onWorkflowRenamedEvent(WorkflowRenamedEvent event)
    {
        return new RecordRequest(AuditingCategory.WORKFLOWS, "jira.auditing.workflow.renamed")
                .forObject(AssociatedItem.Type.WORKFLOW, event.getNewWorkflowName(), event.getNewWorkflowName())
                .withChangedValues(new ChangedValuesBuilder().addIfDifferent("common.words.name", event.getOldWorkflowName(), event.getNewWorkflowName()).build());
    }

    @Override
    public RecordRequest onWorkflowDeletedEvent(final WorkflowDeletedEvent event)
    {
        return new RecordRequest(AuditingCategory.WORKFLOWS, "jira.auditing.workflow.deleted")
                .forObject(AssociatedItem.Type.WORKFLOW, event.getWorkflow().getDisplayName(), event.getWorkflow().getName());
    }

    private RecordRequest workflowCreated(final JiraWorkflow workflow)
    {
        return new RecordRequest(AuditingCategory.WORKFLOWS, "jira.auditing.workflow.created")
                .forObject(AssociatedItem.Type.WORKFLOW, workflow.getDisplayName(), workflow.getName())
                .withChangedValues(computeChangedValues(workflow));
    }

    private RecordRequest workflowUpdated(JiraWorkflow fromWorkflow, final JiraWorkflow toWorkflow)
    {
        return new RecordRequest(AuditingCategory.WORKFLOWS, "jira.auditing.workflow.updated")
                .forObject(AssociatedItem.Type.WORKFLOW, toWorkflow.getDisplayName(), toWorkflow.getName())
                .withChangedValues(computeChangedValues(fromWorkflow, toWorkflow));
    }

    private ImmutableList<ChangedValue> computeChangedValues(final JiraWorkflow currentWorkflow)
    {
        return computeChangedValues(null, currentWorkflow);
    }

    private ImmutableList<ChangedValue> computeChangedValues(final JiraWorkflow originalWorkflow, final JiraWorkflow currentWorkflow)
    {
        final ChangedValuesBuilder changedValues = new ChangedValuesBuilder();

        changedValues.addIfDifferent("common.words.name", originalWorkflow == null ? null : originalWorkflow.getDisplayName(), currentWorkflow.getDisplayName())
                .addIfDifferent("common.words.description", originalWorkflow == null ? null : originalWorkflow.getDescription(), currentWorkflow.getDescription());

        return changedValues.build();
    }
}
