package com.atlassian.jira.functest.framework;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * Navigation actions that pertain to workflows and bulk migrations/transitions.
 *
 * @since v4.0
 */
public class WorkflowsImpl extends AbstractFuncTestUtil implements Workflows
{
    private final Navigation navigation;

    public WorkflowsImpl(WebTester tester, JIRAEnvironmentData environmentData, final Navigation navigation)
    {
        super(tester, environmentData, 2);
        this.navigation = navigation;
    }

    public void chooseWorkflowAction(final String workflowFormElement)
    {
        assertStepOperationDetails();
        tester.setFormElement(BULK_TRANSITION_ELEMENT_NAME, workflowFormElement);
        tester.assertRadioOptionSelected("wftransition", workflowFormElement);
        log("Workflow action selected");
        navigation.clickOnNext();
    }

    public void assertStepOperationDetails()
    {
        tester.assertTextPresent(STEP_PREFIX + STEP_OPERATION_DETAILS);
    }
}
