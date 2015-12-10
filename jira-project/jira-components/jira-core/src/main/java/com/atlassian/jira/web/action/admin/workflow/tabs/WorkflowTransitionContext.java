package com.atlassian.jira.web.action.admin.workflow.tabs;

import java.util.Map;

import com.atlassian.fugue.Option;
import com.atlassian.jira.workflow.JiraWorkflow;

import com.opensymphony.workflow.loader.ActionDescriptor;

public abstract class WorkflowTransitionContext
{
    public final static String TRANSITION_KEY = "workflow_transition";
    public final static String WORKFLOW_KEY = "workflow";
    public final static String COUNT_KEY = "count";

    public static Option<ActionDescriptor> getTransition(Map<String, Object> context) {
        final Object maybeTransition = context.get(TRANSITION_KEY);
        return maybeTransition instanceof ActionDescriptor ? Option.some((ActionDescriptor) maybeTransition) : Option.<ActionDescriptor>none();
    }

    protected static Option<JiraWorkflow> getWorkflow(Map<String, Object> context) {
        final Object maybeWorkflow = context.get(WORKFLOW_KEY);
        return maybeWorkflow instanceof JiraWorkflow ? Option.some((JiraWorkflow) maybeWorkflow) : Option.<JiraWorkflow>none();
    }
}
