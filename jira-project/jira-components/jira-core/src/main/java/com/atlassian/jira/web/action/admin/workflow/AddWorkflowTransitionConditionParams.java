/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.WorkflowConditionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowPluginConditionFactory;
import com.atlassian.jira.web.action.util.workflow.WorkflowEditorTransitionConditionUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.Map;

/**
 * Used to query the user for condition's parameters if any and actually create the
 * condition workflow descriptor. The action will create the workflow descriptor without
 * querying the user if the condition does not need any parameters.
 */
@WebSudoRequired
public class AddWorkflowTransitionConditionParams extends AbstractAddWorkflowTransitionDescriptorParams
{
    private String count;
    private boolean nested;
    private String currentConditionCount;

    public AddWorkflowTransitionConditionParams(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public AddWorkflowTransitionConditionParams(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected void addWorkflowDescriptor() throws WorkflowException
    {
        ConditionDescriptor condition = DescriptorFactory.getFactory().createConditionDescriptor();
        condition.setType("class");
        final Map conditionArgs = condition.getArgs();
        conditionArgs.put("class.name", getDescriptor().getImplementationClass().getName());

        // Add parameters to the workflow condition descriptor
        // Make the factory process it
        WorkflowPluginConditionFactory workflowConditionFactory = (WorkflowPluginConditionFactory) getDescriptor().getModule();
        conditionArgs.putAll(workflowConditionFactory.getDescriptorParams(getDescriptorParams()));

        // Now create a nested ConditionsDescriptor with the new condition
        WorkflowEditorTransitionConditionUtil wetcu = new WorkflowEditorTransitionConditionUtil();

        if (isNested())
            currentConditionCount = wetcu.addNestedCondition(getTransition(), getCount(), condition);
        else
            currentConditionCount = wetcu.addCondition(getTransition(), getCount(), condition);

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());
    }

    protected String getRedirectUrl()
    {
        if (TextUtils.stringSet(currentConditionCount))
            return super.getRedirectUrl() + "&currentCount=workflow-condition" + currentConditionCount;
        else
            return super.getRedirectUrl();
    }

    public String getWorkflowDescriptorName()
    {
        return "Condition";
    }

    protected Class getWorkflowModuleDescriptorClass()
    {
        return WorkflowConditionModuleDescriptor.class;
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
}