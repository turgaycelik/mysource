package com.atlassian.jira.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Event indicating a workflow has been imported via an XML file.
 *
 * @since v5.0
 */
@Analytics("administration.workflow.importedfromxml")
public class WorkflowImportedFromXmlEvent extends AbstractWorkflowEvent
{
    public WorkflowImportedFromXmlEvent(JiraWorkflow workflow)
    {
        super(workflow);
    }
}
