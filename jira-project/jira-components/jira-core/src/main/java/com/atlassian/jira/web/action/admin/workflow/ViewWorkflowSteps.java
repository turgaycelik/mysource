package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.web.component.WorkflowHeaderWebComponent;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@WebSudoRequired
public class ViewWorkflowSteps extends AbstractWorkflowStep
{
    private WorkflowHeaderWebComponent workflowHeaderWebComponent;
    private WorkflowViewMode viewMode;

    public ViewWorkflowSteps(JiraWorkflow workflow, ConstantsManager constantsManager, WorkflowService workflowService, ComponentFactory factory)
    {
        super(workflow, constantsManager, workflowService);
        this.workflowHeaderWebComponent = factory.createObject(WorkflowHeaderWebComponent.class);
    }

    @RequiresXsrfCheck
    public String doAddStep()
    {
        if (isNotBlank(getStepName()))
        {
            List<StepDescriptor> existingSteps = getWorkflow().getDescriptor().getSteps();
            for (StepDescriptor existingStep : existingSteps)
            {
                if (getStepName().equalsIgnoreCase(existingStep.getName()))
                {
                    addError("stepName", getText("admin.errors.step.with.name.already.exists"));
                }

                if (getStepStatus().equalsIgnoreCase((String) existingStep.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY)))
                {
                    addError("stepStatus", getText("admin.errors.existing.step.already.linked"));
                }
            }
        }
        else
        {
            addError("stepName", getText("admin.errors.step.name.must.be.specified"));
        }

        if (!invalidInput())
        {
            StepDescriptor newStep = DescriptorFactory.getFactory().createStepDescriptor();
            newStep.setName(getStepName());
            newStep.setId(WorkflowUtil.getNextId(getWorkflow().getDescriptor().getSteps()));
            newStep.getMetaAttributes().put(JiraWorkflow.STEP_STATUS_KEY, getStepStatus());

            newStep.setParent(getWorkflow().getDescriptor());
            getWorkflow().getDescriptor().addStep(newStep);

            workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());
        }

        return SUCCESS;
    }

    public boolean isTransitionWithoutStepChange(ActionDescriptor transition)
    {
        return transition.getUnconditionalResult().getStep() == JiraWorkflow.ACTION_ORIGIN_STEP_ID;
    }

    public Status getStatus(String id)
    {
        return getConstantsManager().getStatusObject(id);
    }

    public String getHeaderHtml()
    {
        return workflowHeaderWebComponent.getHtml(getWorkflow(), "workflow", getProject());
    }

    public String getLinksHtml()
    {
        String viewMode = workflow.isEditable() || !isDiagramMode() ? "text" : "diagram";

        return workflowHeaderWebComponent.getLinksHtml(getWorkflow(), getProject(), viewMode, getWorkflow().isEditable());
    }

    public boolean isDiagramMode()
    {
        if (viewMode == null)
        {
            viewMode = WorkflowViewMode.parseFromAction(this);
        }
        return viewMode == WorkflowViewMode.DIAGRAM;
    }

}