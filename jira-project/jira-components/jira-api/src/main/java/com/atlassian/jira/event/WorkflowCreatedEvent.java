package com.atlassian.jira.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Event indicating a workflow has been created.
 *
 * @since v5.2
 */
@Analytics("administration.workflow.created")
public class WorkflowCreatedEvent extends AbstractWorkflowEvent
{
    public WorkflowCreatedEvent(JiraWorkflow workflow)
    {
        super(workflow);
    }
}
