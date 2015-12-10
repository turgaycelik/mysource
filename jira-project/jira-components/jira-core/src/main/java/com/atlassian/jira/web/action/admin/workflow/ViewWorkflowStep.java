package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: amazkovoi
 * Date: 13/09/2004
 * Time: 13:09:08
 */
@WebSudoRequired
public class ViewWorkflowStep extends AbstractWorkflowStep
{
    private final StepDescriptor step;

    public ViewWorkflowStep(JiraWorkflow workflow, StepDescriptor step, ConstantsManager constantsManager,
            WorkflowService workflowService)
    {
        super(workflow, constantsManager, workflowService);
        this.step = step;
    }


    public StepDescriptor getStep()
    {
        return step;
    }

    public Collection getInboundTransitions()
    {
        return workflow.getActionsWithResult(step);
    }

    public Collection getOutboundTransitions()
    {
        // TODO add global actions
        Collection transitions = new LinkedList(step.getActions());
        transitions.addAll(workflow.getDescriptor().getGlobalActions());
        return transitions;
    }

    public boolean isGlobal(ActionDescriptor actionDescriptor)
    {
        return workflow.isGlobalAction(actionDescriptor);
    }

    public boolean isCommon(ActionDescriptor actionDescriptor)
    {
        return workflow.isCommonAction(actionDescriptor);
    }

    public boolean isInitial(ActionDescriptor actionDescriptor)
    {
        return workflow.isInitialAction(actionDescriptor);
    }

    public Collection getStepsForTransition(ActionDescriptor actionDescriptor)
    {
        return workflow.getStepsForTransition(actionDescriptor);
    }

    public GenericValue getStatus(String id)
    {
        return constantsManager.getStatus(id);
    }

    public Status getStatusObject(String id)
    {
        return constantsManager.getStatusObject(id);
    }

}
