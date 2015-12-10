package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;

import java.util.Map;

/**
 * Represents the workflow condition module of the reference plugin.
 *
 * @since 4.4
 */
public class WorkflowCondition extends ReferencePluginModule
{
    private static final String MODULE_KEY = "reference-workflow-condition";
    private static final String MODULE_NAME = "Reference Workflow Condition";

    private final Administration administration;

    public WorkflowCondition(Administration administration)
    {
        super(administration);
        this.administration = administration;
    }

    @Override
    public String moduleKey()
    {
        return MODULE_KEY;
    }

    @Override
    public String moduleName()
    {
        return MODULE_NAME;
    }

    public boolean canAddTo(String workflowName, int stepId, int transitionId)
    {
        return administration.workflows().goTo().workflowSteps(workflowName).editTransition(stepId, transitionId)
                .canAddWorkflowCondition(completeModuleKey());
    }

    public void addTo(String workflowName, int stepId, int transitionId)
    {
         administration.workflows().goTo().workflowSteps(workflowName).editTransition(stepId, transitionId)
                .addWorkflowCondition(completeModuleKey());
    }

    public void addTo(String workflowName, int stepId, int transitionId, Map<String,String> configFormParams)
    {
         administration.workflows().goTo().workflowSteps(workflowName).editTransition(stepId, transitionId)
                .addWorkflowCondition(completeModuleKey(), configFormParams);
    }
}
