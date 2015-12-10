package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.Collection;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractWorkflowTransition extends AbstractWorkflowAction
{
    private final StepDescriptor step;

    private String transitionName;
    private String description;
    private int destinationStep;
    private String view;

    private String originatingUrl;
    protected final WorkflowService workflowService;

    protected AbstractWorkflowTransition(JiraWorkflow workflow, WorkflowService workflowService)
    {
        this(workflow, null, workflowService);
    }

    protected AbstractWorkflowTransition(JiraWorkflow workflow, StepDescriptor step, WorkflowService workflowService)
    {
        super(workflow);
        this.step = step;
        this.workflowService = workflowService;
    }

    public JiraWorkflow getWorkflow()
    {
        return workflow;
    }

    public StepDescriptor getStep()
    {
        return step;
    }

    public String getTransitionName()
    {
        return transitionName;
    }

    public void setTransitionName(String transitionName)
    {
        this.transitionName = transitionName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getDestinationStep()
    {
        return destinationStep;
    }

    public void setDestinationStep(int destinationStep)
    {
        this.destinationStep = destinationStep;
    }

    public String getView()
    {
        return view;
    }

    public void setView(String view)
    {
        this.view = view;
    }

    public String getOriginatingUrl()
    {
        return originatingUrl;
    }

    public void setOriginatingUrl(String originatingUrl)
    {
        this.originatingUrl = originatingUrl;
    }

    protected void checkDuplicateTransitionName(Collection tranistionCollection, String transitionName)
    {
        // Check for duplicate transition names
        for (final Object aTranistionCollection : tranistionCollection)
        {
            ActionDescriptor actionDescriptor = (ActionDescriptor) aTranistionCollection;
            if (transitionName.equalsIgnoreCase(actionDescriptor.getName()))
            {
                addError("transitionName", getText("admin.errors.workflows.transition.already.exists.for.step", getStep().getName()));
                break;
            }
        }
    }

}
