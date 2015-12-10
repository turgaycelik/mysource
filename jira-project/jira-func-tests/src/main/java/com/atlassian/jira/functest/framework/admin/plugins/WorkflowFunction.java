package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;

import java.util.Map;

/**
 * Represents the workflow function module of the reference plugin.
 *
 * @since 4.4
 */
public class WorkflowFunction extends ReferencePluginModule
{
    private static final String MODULE_KEY = "reference-workflow-function";
    private static final String MODULE_NAME = "Reference Workflow Function";

    private final Administration administration;

    public WorkflowFunction(Administration administration)
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
                .canAddWorkflowFunction(completeModuleKey());
    }

    public void addTo(String workflowName, int stepId, int transitionId)
    {
         administration.workflows().goTo().workflowSteps(workflowName).editTransition(stepId, transitionId)
                .addWorkflowFunction(completeModuleKey());
    }

    public void addTo(String workflowName, int stepId, int transitionId, Map<String,String> configFormParams)
    {
         administration.workflows().goTo().workflowSteps(workflowName).editTransition(stepId, transitionId)
                .addWorkflowFunction(completeModuleKey(), configFormParams);
    }
}
