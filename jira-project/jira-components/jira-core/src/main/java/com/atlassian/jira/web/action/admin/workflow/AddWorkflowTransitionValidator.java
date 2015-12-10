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
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

@WebSudoRequired
public class AddWorkflowTransitionValidator extends AbstractAddWorkflowTransitionDescriptor
{
    public AddWorkflowTransitionValidator(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public AddWorkflowTransitionValidator(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    public String getDescriptorNameKey()
    {
        return "admin.workflowtransition.validator";
    }

    protected String getParamsActionName()
    {
        return "AddWorkflowTransitionValidatorParams!default.jspa";
    }

    protected boolean isModulePresent(Class implementationClass)
    {
        return false;
    }

    protected String getWorkflowModuleDescriptorType()
    {
        return JiraWorkflowPluginConstants.MODULE_NAME_WORKFLOW_VALIDATOR;
    }
}