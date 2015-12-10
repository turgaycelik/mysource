package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionalResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@WebSudoRequired
public class ViewWorkflowTransitionConditionalResult extends AbstractWorkflowTransitionAction
{
    private int resultCount;
    private ConditionalResultDescriptor resultDescriptor;
    private String resultXml;

    public ViewWorkflowTransitionConditionalResult(JiraWorkflow workflow, StepDescriptor step,
            ActionDescriptor transition, PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public ViewWorkflowTransitionConditionalResult(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    public int getResultCount()
    {
        return resultCount;
    }

    public void setResultCount(int resultCount)
    {
        this.resultCount = resultCount;
    }

    protected void doValidation()
    {
        if (getTransition().getConditionalResults() == null)
        {
            addErrorMessage(getText("admin.errors.workflows.no.conditional.results.defined"));
        }
        else if (resultCount < 1 || resultCount > getTransition().getConditionalResults().size())
        {
            addErrorMessage(getText("admin.errors.workflows.invalid.step.count", ""+ resultCount));
        }

        super.doValidation();
    }

    protected String doExecute() throws Exception
    {
        final List conditionalResults = getTransition().getConditionalResults();
        resultDescriptor = (ConditionalResultDescriptor) conditionalResults.get(resultCount - 1);

        final StringWriter stringWriter = new StringWriter();
        PrintWriter writer =  new PrintWriter(stringWriter);
        resultDescriptor.writeXML(writer, 0);
        writer.flush();
        resultXml = stringWriter.getBuffer().toString();

        return super.doExecute();
    }

    public StepDescriptor getDestinationStepDescriptor()
    {
        int targetStepId = resultDescriptor.getStep();
        return getWorkflow().getDescriptor().getStep(targetStepId);
    }

    public String getResultXML()
    {
        return resultXml;
    }

}
