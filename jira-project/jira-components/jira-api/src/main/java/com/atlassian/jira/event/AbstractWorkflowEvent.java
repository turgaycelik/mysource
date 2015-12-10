package com.atlassian.jira.event;

import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
public abstract class AbstractWorkflowEvent {

    private final JiraWorkflow workflow;

    public AbstractWorkflowEvent(JiraWorkflow workflow) {
        this.workflow = workflow;
    }

    public JiraWorkflow getWorkflow() {
        return workflow;
    }
}
