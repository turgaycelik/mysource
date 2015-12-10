package com.atlassian.jira.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.annotations.Internal;
import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Event indicating a workflow has been updated.
 *
 * @since v5.0
 */
@Analytics("administration.workflow.updated")
public class WorkflowUpdatedEvent extends AbstractWorkflowEvent
{
    private final JiraWorkflow originalWorkflow;

    @Internal
    public WorkflowUpdatedEvent(JiraWorkflow workflow, JiraWorkflow originalWorkflow)
    {
        super(workflow);
        this.originalWorkflow = originalWorkflow;
    }

    public JiraWorkflow getOriginalWorkflow() {
        return originalWorkflow;
    }
}
