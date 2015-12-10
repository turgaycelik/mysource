package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditWorkflowStep extends AbstractWorkflowStep
{
    private final StepDescriptor step;

    private Collection statuses;
    private String originatingUrl;

    public EditWorkflowStep(JiraWorkflow workflow, StepDescriptor step, ConstantsManager constantsManager,
            WorkflowService workflowService)
    {
        super(workflow, constantsManager, workflowService);
        this.step = step;
    }

    public String doDefault() throws Exception
    {
        setStepName(step.getName());

        initStatuses();

        return super.doDefault();
    }

    private void initStatuses()
    {
        if (step.getMetaAttributes() != null && step.getMetaAttributes().containsKey(JiraWorkflow.STEP_STATUS_KEY))
        {
            setStepStatus((String) step.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY));

            statuses = new ArrayList();
            // Add existing status to the list
            statuses.add(getConstantsManager().getStatus(getStepStatus()));
            // Add all the other available statuses
            statuses.addAll(getUnlinkedStatusesGVs());
        }
        else
        {
            addErrorMessage(getText("admin.errors.no.associated.status"));
        }
    }

    protected void doValidation()
    {
        // Validate name
        if (TextUtils.stringSet(getStepName()))
        {
            // Only validate step name if it has changed
            if (!getStepName().equals(step.getName()))
            {
                List existingSteps = getWorkflow().getDescriptor().getSteps();
                for (final Object existingStep1 : existingSteps)
                {
                    StepDescriptor existingStep = (StepDescriptor) existingStep1;
                    if (getStepName().equalsIgnoreCase(existingStep.getName()))
                    {
                        addError("stepName", getText("admin.errors.step.with.name.already.exists"));
                    }
                }
            }
        }
        else
        {
            addError("stepName", getText("admin.errors.step.name.must.be.specified"));
        }

        // Validate status.  Note that we only do this for non-draft workflows and new steps on draft workflows.
        if (!isOldStepOnDraft(step))
        {
            if (TextUtils.stringSet(getStepStatus()))
            {
                // Only validate status if it has changed
                if (!getStepStatus().equals(step.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY)))
                {
                    List existingSteps = getWorkflow().getDescriptor().getSteps();
                    for (final Object existingStep1 : existingSteps)
                    {
                        StepDescriptor existingStep = (StepDescriptor) existingStep1;
                        if (getStepStatus().equalsIgnoreCase((String) existingStep.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY)))
                        {
                            addError("stepStatus", getText("admin.errors.existing.step.already.linked"));
                        }
                    }
                }
            }
            else
            {
                addError("stepStatus", getText("admin.errors.step.must.be.linked.to.status"));
            }
        }
        else
        {
            // Make sure the user hasn't tried to change this old step to use a different status.
            // The UI itself should normally not give them this option, but this is a safety net in case they try to
            // hack the URL or the UI gets broken.
            if (getStepStatus() != null && !getStepStatus().equals(step.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY)))
            {
                addError("stepStatus", getText("admin.errors.step.edit.draft.status"));
            }
        }

        if (invalidInput())
        {
            initStatuses();
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        StepDescriptor stepDescriptor = getWorkflow().getDescriptor().getStep(step.getId());
        stepDescriptor.setName(getStepName());

        //For a draft workflow, you can only changed the linked status for new steps.
        if (!isOldStepOnDraft(step))
        {
            stepDescriptor.getMetaAttributes().put(JiraWorkflow.STEP_STATUS_KEY, getStepStatus());
        }

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        return getRedirect(getCancelUrl());
    }

    public StepDescriptor getStep()
    {
        return step;
    }

    public Collection getStatuses()
    {
        return statuses;
    }

    public String getOriginatingUrl()
    {
        return originatingUrl;
    }

    public void setOriginatingUrl(String originatingUrl)
    {
        this.originatingUrl = originatingUrl;
    }

    public String getCancelUrl()
    {
        if ("viewWorkflowStep".equals(getOriginatingUrl()))
        {
            return "ViewWorkflowStep.jspa" + getBasicWorkflowParameters() +
                   "&workflowStep=" + step.getId();
        }

        return "ViewWorkflowSteps.jspa" + getBasicWorkflowParameters();
    }
}
