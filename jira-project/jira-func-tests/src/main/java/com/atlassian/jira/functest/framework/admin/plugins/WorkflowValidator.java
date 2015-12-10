package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;

import java.util.Map;

/**
 * Represents the workflow validator module of the reference plugin.
 *
 * @since 4.4
 */
public class WorkflowValidator extends ReferencePluginModule
{
    private static final String MODULE_KEY = "reference-workflow-validator";
    private static final String MODULE_NAME = "Reference Workflow Validator";

    private final Administration administration;

    public WorkflowValidator(Administration administration)
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
                .canAddWorkflowValidator(completeModuleKey());
    }

    public void addTo(String workflowName, int stepId, int transitionId)
    {
         administration.workflows().goTo().workflowSteps(workflowName).editTransition(stepId, transitionId)
                .addWorkflowValidator(completeModuleKey());
    }

    public void addTo(String workflowName, int stepId, int transitionId, Map<String,String> configFormParams)
    {
         administration.workflows().goTo().workflowSteps(workflowName).editTransition(stepId, transitionId)
                .addWorkflowValidator(completeModuleKey(), configFormParams);
    }

    public void addToInitialAction(String workflowName, String workflowValidatorKey)
    {
        administration.workflows().goTo().workflowInitialStep(workflowName).createTransition().addWorkflowValidator(workflowValidatorKey);
    }
}
