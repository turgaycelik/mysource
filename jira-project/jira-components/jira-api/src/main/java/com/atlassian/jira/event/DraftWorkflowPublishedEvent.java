package com.atlassian.jira.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.annotations.Internal;
import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
@Analytics("administration.workflow.draftpublished")
public class DraftWorkflowPublishedEvent extends AbstractWorkflowEvent {

    private final JiraWorkflow originalWorkflow;

    @Internal
    public DraftWorkflowPublishedEvent(JiraWorkflow workflow, JiraWorkflow originalWorkflow) {
        super(workflow);
        this.originalWorkflow = originalWorkflow;
    }

    public JiraWorkflow getOriginalWorkflow() {
        return originalWorkflow;
    }
}
