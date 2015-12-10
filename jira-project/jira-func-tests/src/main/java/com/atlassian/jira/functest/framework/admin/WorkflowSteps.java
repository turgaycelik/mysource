package com.atlassian.jira.functest.framework.admin;

/**
 * Represents the 'Workflow steps' page functionality
 *
 * @since v4.3
 */
public interface WorkflowSteps
{
    /**
     * Edit transition with given <tt>transitionId</tt> for step with given <tt>stepId</tt>.
     *
     * @param stepId ID of the step
     * @param transitionId ID of the transition
     * @return workflow transition functionality representation
     */
    WorkflowTransition editTransition(int stepId, int transitionId);

    /**
     * Adds a step the current workflow.
     * @param stepName The name of the step.
     * @param linkedStatus The status to be linked to this step.
     * @return this instance of the workflow steps page.
     */
    WorkflowSteps add(String stepName, String linkedStatus);

    /**
     * Adds a transition to the current workflow
     * @param stepName The origin workflow step for this transition.
     * @param transitionName The name of the transition to add.
     * @param transitionDescription A description for the transition to add.
     * @param destinationStep The destination step for this transition.
     * @param transitionFieldScreen The screen to display.
     * @return this instance of the workflow steps page.
     */
    WorkflowSteps addTransition(String stepName, String transitionName, String transitionDescription,
            String destinationStep, String transitionFieldScreen);
}
