package com.atlassian.jira.functest.framework;

/**
 * Navigation actions that pertain to workflows and bulk migrations/transitions.
 *
 * @since v4.0
 */
public interface Workflows
{
    public static final String STEP_PREFIX = "Bulk Operation: ";
    public static final String STEP_OPERATION_DETAILS = "Operation Details";
    public static final String BULK_TRANSITION_ELEMENT_NAME = "wftransition";

    /**
     * Chooses the Execute Worfklow Action radio button in the Step "Operation Details"
     *
     * @param workflowFormElement the workflow radio option that should be selected
     */
    void chooseWorkflowAction(String workflowFormElement);

    /**
     * Asserts that we are currently on the step "Operation Details"
     */
    void assertStepOperationDetails();
}
