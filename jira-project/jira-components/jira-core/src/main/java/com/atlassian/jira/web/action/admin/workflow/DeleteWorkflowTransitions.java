/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.Collection;
import java.util.LinkedList;

@WebSudoRequired
public class DeleteWorkflowTransitions extends AbstractWorkflowAction
{
    private final StepDescriptor step;
    private final WorkflowService workflowService;
    private int[] transitionIds;
    private String originatingUrl;

    public DeleteWorkflowTransitions(JiraWorkflow workflow, StepDescriptor step, WorkflowService workflowService)
    {
        super(workflow);
        this.step = step;
        this.workflowService = workflowService;
    }

    public String doConfirm()
    {
        return "confirm";
    }

    protected void doValidation()
    {
        if (transitionIds == null || transitionIds.length == 0)
        {
            addError("transitionIds", getText("admin.errors.workflows.must.select.transition"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        for (int transitionId : transitionIds)
        {
            ActionDescriptor transition = step.getAction(transitionId);
            if (transition != null)
            {
                step.getActions().remove(transition);
            }
            else
            {
                addErrorMessage(getText("admin.errors.workflows.cannot.find.transition.with.id", "'" + transitionId + "'"));
                return ERROR;
            }

            if (transition.isCommon())
            {
                step.getCommonActions().remove(new Integer(transition.getId()));
                // Check if the common action is not referenced from any steps in the workflow
                if (workflow.getStepsForTransition(transition).isEmpty())
                {
                    // If so, delete it from the workflow completely
                    workflow.getDescriptor().getCommonActions().remove(new Integer(transition.getId()));
                }
            }

            if (step.getActions().isEmpty() && step.getCommonActions().isEmpty())
            {
                // Need to call this method to let workflow know that this step does not have
                // any actions, otherwise validation will fail
                step.removeActions();
            }
        }

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());
        return redirectToView();
    }

    private String redirectToView()
    {
        return getRedirect(getCancelUrl());
    }

    public JiraWorkflow getWorkflow()
    {
        return workflow;
    }

    public StepDescriptor getStep()
    {
        return step;
    }

    public Collection getTransitions()
    {
        // The actions list should contain all the common and ordinary actions.
        // (And no global actions, which cannot be deleted from here).
        return step.getActions();
    }

    public void setTransitionIds(int[] transitionIds)
    {
        this.transitionIds = transitionIds;
    }

    public Collection getSelectedTransitions()
    {
        Collection transitions = new LinkedList();
        if (transitionIds != null)
        {
            for (final int transitionId : transitionIds)
            {
                transitions.add(step.getAction(transitionId));
            }
        }

        return transitions;
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