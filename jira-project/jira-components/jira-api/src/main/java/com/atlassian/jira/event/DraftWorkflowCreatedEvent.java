package com.atlassian.jira.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
@Analytics("administration.workflow.draftcreated")
public class DraftWorkflowCreatedEvent extends AbstractWorkflowEvent {

    public DraftWorkflowCreatedEvent(JiraWorkflow workflow) {
        super(workflow);
    }
}
