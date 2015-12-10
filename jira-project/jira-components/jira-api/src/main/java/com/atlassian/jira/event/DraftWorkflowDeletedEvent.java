package com.atlassian.jira.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
@Analytics("administration.workflow.draftdeleted")
public class DraftWorkflowDeletedEvent extends AbstractWorkflowEvent{

    public DraftWorkflowDeletedEvent(JiraWorkflow workflow) {
        super(workflow);
    }
}
