package com.atlassian.jira.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
@Analytics("administration.workflow.deleted")
public class WorkflowDeletedEvent extends AbstractWorkflowEvent {

    public WorkflowDeletedEvent(JiraWorkflow workflow) {
        super(workflow);
    }
}

