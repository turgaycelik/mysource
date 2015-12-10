package com.atlassian.jira.web.action.admin.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;

import com.opensymphony.workflow.loader.StepDescriptor;

import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class AbstractWorkflowStep extends AbstractWorkflowAction
{
    protected final ConstantsManager constantsManager;
    private String stepName;
    private String stepStatus;
    protected final WorkflowService workflowService;

    public AbstractWorkflowStep(JiraWorkflow workflow, ConstantsManager constantsManager, WorkflowService workflowService)
    {
        super(workflow);
        this.constantsManager = constantsManager;
        this.workflowService = workflowService;
    }

    public String getStepName()
    {
        return stepName;
    }

    public void setStepName(String stepName)
    {
        this.stepName = stepName;
    }

    public Collection getUnlinkedStatusesGVs()
    {
        return getUnlinkedStatuses(true);
    }

    public Collection getUnlinkedStatuses()
    {
        return getUnlinkedStatuses(false);
    }

    public Collection getUnlinkedStatuses(boolean genericValues)
    {
        Collection<Status> statuses = constantsManager.getStatusObjects();
        List unlinkedStatuses = new ArrayList(statuses.size());

        for (final Status status : statuses)
        {
            GenericValue statusGV = status.getGenericValue();

            if (!isStatusLinked(statusGV))
            {
                if (genericValues)
                {
                    unlinkedStatuses.add(statusGV);
                }
                else
                {
                    unlinkedStatuses.add(status);
                }
            }
        }

        return unlinkedStatuses;
    }

    private boolean isStatusLinked(GenericValue status)
    {
        for (final Object o : workflow.getDescriptor().getSteps())
        {
            StepDescriptor stepDescriptor = (StepDescriptor) o;
            if (status.getString("id").equals(stepDescriptor.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY)))
            {
                return true;
            }
        }
        return false;
    }

    public String getStepStatus()
    {
        return stepStatus;
    }

    public void setStepStatus(String stepStatus)
    {
        this.stepStatus = stepStatus;
    }

    public JiraWorkflow getWorkflow()
    {
        return workflow;
    }

    public ConstantsManager getConstantsManager()
    {
        return constantsManager;
    }

    /**
     * This method is used to determine if workflowstep belongs to a draft workflow and is present on the original
     * workflow that was used to create the draft.
     * This means this step cannot be deleted, and cannot be associated with a different status.
     * @param stepDescriptor The stepDescriptor in being edited.
     * @return True if the step is an existing step and the workflow is a draft workflow
     */
    public boolean isOldStepOnDraft(StepDescriptor stepDescriptor)
    {
        // First check if it is a draft workflow
        if (!workflow.isDraftWorkflow())
        {
            return false;
        }
        // Does this step exist in the live workflow?
        JiraWorkflow jiraWorkflow = workflowService.getWorkflow(getJiraServiceContext(), workflow.getName());
        return jiraWorkflow.getDescriptor().getStep(stepDescriptor.getId()) != null;

    }

    public boolean isCanDeleteStep(StepDescriptor stepDescriptor) throws WorkflowException
    {
        if (!workflow.isEditable())
        {
            // Can only delete step if the worklfow can be edited
            // No need to do the checks below in this case, so just return
            return false;
        }

        // Determine if the step is a destination step of any existing transitions
        return workflow.getActionsWithResult(stepDescriptor).isEmpty() && !isOldStepOnDraft(stepDescriptor);
    }

    /**
     * Used to detect if a step on a draft, does not have any outgoing transitions on the original workflow.
     * Adding transitions to such a step is not allowed.
     *
     * @param stepId The id of the step in question.
     * @return True if the step does not have any outgoing transitions on the original workflow
     */
    public boolean isStepWithoutTransitionsOnDraft(int stepId)
    {
       return workflowService.isStepOnDraftWithNoTransitionsOnParentWorkflow(getJiraServiceContext(),
               getWorkflow(), stepId);
    }

}
