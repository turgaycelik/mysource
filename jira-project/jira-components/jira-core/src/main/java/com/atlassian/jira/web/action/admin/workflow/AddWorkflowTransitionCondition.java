/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

@WebSudoRequired
public class AddWorkflowTransitionCondition extends AbstractAddWorkflowTransitionDescriptor
{
    private static final String ADMIN_WORKFLOWTRANSITION_CONDITION = "admin.workflowtransition.condition";

    private String count;

    private boolean nested;

    public AddWorkflowTransitionCondition(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public AddWorkflowTransitionCondition(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected String getWorkflowModuleDescriptorType()
    {
        return JiraWorkflowPluginConstants.MODULE_NAME_WORKFLOW_CONDITION;
    }

    public String getDescriptorNameKey()
    {
        return ADMIN_WORKFLOWTRANSITION_CONDITION;
    }

    protected String getParamsActionName()
    {
        return "AddWorkflowTransitionConditionParams!default.jspa";
    }

    protected boolean isModulePresent(Class implementationClass)
    {
        // Conditions should not be unique due to the possibility of condition nesting
        return false;
    }

    public String getCount()
    {
        return count;
    }

    public void setCount(String count)
    {
        this.count = count;
    }

    public boolean isNested()
    {
        return nested;
    }

    public void setNested(boolean nested)
    {
        this.nested = nested;
    }

    protected String getRedirectUrl()
    {
        String url = super.getRedirectUrl();

        if (TextUtils.stringSet(getCount()))
        {
            url += "&count=" + getCount();
        }

        if (isNested())
        {
            url += "&nested=" + String.valueOf(isNested());
        }

        return url;
    }
}