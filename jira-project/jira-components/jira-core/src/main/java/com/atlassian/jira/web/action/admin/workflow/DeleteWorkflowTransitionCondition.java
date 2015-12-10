/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.util.workflow.WorkflowEditorTransitionConditionUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.commons.lang.StringUtils;

import java.net.URLEncoder;
import java.util.List;

@WebSudoRequired
public class DeleteWorkflowTransitionCondition extends AbstractWorkflowTransitionAction
{
    private String count;

    public DeleteWorkflowTransitionCondition(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public DeleteWorkflowTransitionCondition(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(getCount()))
        {
            addErrorMessage(getText("admin.errors.workflows.invalid.count", "" + count));
        }

        checkDescriptor();

        if (!invalidInput())
        {
            final List descriptors = getDescriptorCollection();

            if (descriptors == null || descriptors.isEmpty())
            {
                addErrorMessage(getText("admin.errors.workflows.no.descriptors.to.delete"));
            }
            else if (descriptors.size() < getConditionIndex())
            {
                addErrorMessage(getText("admin.errors.workflows.count.too.large", "" + getConditionIndex(), "" + descriptors.size()));
            }
        }
    }

    protected void checkDescriptor()
    {
        final RestrictionDescriptor restriction = getTransition().getRestriction();

        if (restriction == null)
        {
            addErrorMessage(getText("admin.errors.workflows.cannot.delete.condition"));
        }
    }

    protected List getDescriptorCollection()
    {
        final ConditionsDescriptor conditionsDescriptor = getConditionsDescriptor();
        return conditionsDescriptor.getConditions();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        deleteWorkflowDescriptor();

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        if (getStep() == null)
        {
            return getRedirect("ViewWorkflowTransition.jspa" + getBasicWorkflowParameters() +
                               "&workflowTransition=" + getTransition().getId());
        }
        else
        {
            return getRedirect("ViewWorkflowTransition.jspa" + getBasicWorkflowParameters() +
                               "&workflowStep=" + getStep().getId() +
                               "&workflowTransition=" + getTransition().getId());
        }
    }

    public String getWorkflowDescriptorName()
    {
        return "Condition";
    }

    protected void deleteWorkflowDescriptor() throws WorkflowException
    {

        WorkflowEditorTransitionConditionUtil wetcu = new WorkflowEditorTransitionConditionUtil();
        wetcu.deleteCondition(getTransition(), getCount());
    }

    private ConditionsDescriptor getConditionsDescriptor()
    {
        RestrictionDescriptor restriction = getTransition().getRestriction();

        if (restriction != null)
        {
            WorkflowEditorTransitionConditionUtil wetcu = new WorkflowEditorTransitionConditionUtil();
            return wetcu.getParentConditionsDescriptor(restriction, getCount());
        }
        else
        {
            return null;
        }
    }

    private int getConditionIndex()
    {
        String[] counts = StringUtils.split(getCount(), ".");
        return Integer.parseInt(counts[counts.length - 1]);
    }

    public String getCount()
    {
        return count;
    }

    public void setCount(String count)
    {
        this.count = count;
    }
}