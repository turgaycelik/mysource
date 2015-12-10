/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 24, 2004
 * Time: 6:05:14 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.net.URLEncoder;
import java.util.Collection;

@WebSudoRequired
public class DeleteWorkflowStep extends AbstractWorkflowStep
{
    private Collection destinationTransitions;
    final StepDescriptor step;
    private String originatingUrl;

    public DeleteWorkflowStep(JiraWorkflow workflow, StepDescriptor step, WorkflowService workflowService,
            ConstantsManager constantsManager)
    {
        super(workflow, constantsManager, workflowService);
        this.step = step;
    }

    protected void doValidation()
    {
        if (isOldStepOnDraft(step))
        {
            addErrorMessage(getText("admin.errors.delete.step.draft"));
        }
        if(!workflow.isEditable())
        {
            addErrorMessage(getText("admin.errors.delete.step.not.editable"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        workflow.removeStep(step);
        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        return getRedirect("ViewWorkflowSteps.jspa" + getBasicWorkflowParameters());
    }

    public Collection getDestinationTransitions()
    {
        if (destinationTransitions == null)
            destinationTransitions = workflow.getActionsWithResult(step);

        return destinationTransitions;
    }

    public JiraWorkflow getWorkflow()
    {
        return workflow;
    }

    public StepDescriptor getStep()
    {
        return step;
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