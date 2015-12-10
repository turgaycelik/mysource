package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.admin.workflows.WorkflowDesignerPage;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowTransitionView extends JIRAWebTest
{

    private static final String TRANSITION_NAME_1 = "My Transition 1";
    private static final String TRANSITION_NAME_2 = "My Transition 2";
    private static final String TRANSITION_DESC = "This is a test transition";
    private static final String TRANSITION_FIELD_SCREEN = "No view for transition";

    public TestWorkflowTransitionView(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreBlankInstance();
    }

    private void copyWorkflowAndAddTransitions()
    {
        administration.workflows().goTo()
                .copyWorkflow("jira", WORKFLOW_COPIED, "Workflow copied from JIRA default");
        assertTextPresent(WORKFLOW_COPIED);
        administration.workflows().goTo().workflowSteps(WORKFLOW_COPIED)
            .addTransition("Open", TRANSITION_NAME_1, TRANSITION_DESC, "Closed", TRANSITION_FIELD_SCREEN)
            .addTransition("Open", TRANSITION_NAME_2, TRANSITION_DESC, "Closed", TRANSITION_FIELD_SCREEN);
    }

    /** Tests that the default value in the Workflow Transision stays the same. Fix for JRA-9550 */
    public void testTransitionView1()
    {
        copyWorkflowAndAddTransitions();
        administration.workflows().goTo().workflowSteps("Copied Workflow");
        clickLinkWithText(TRANSITION_NAME_1);
        assertTextPresent("None - it will happen instantly");

        clickLink("edit_transition");
        assertFormElementEquals("view", "");
        submit("Update");
        assertTextPresent("None - it will happen instantly");

        clickLink("edit_transition");
        assertFormElementEquals("view", "");
        selectOption("view", "Default Screen");
        assertFormElementEquals("view", "1");
        submit("Update");
        assertTextPresent("Default Screen");

        clickLink("edit_transition");
        assertFormElementEquals("view", "1");
        submit("Update");
        assertTextPresent("Default Screen");

        clickLink("edit_transition");
        assertFormElementEquals("view", "1");
        selectOption("view", "Workflow Screen");
        assertFormElementEquals("view", "2");
        submit("Update");
        assertTextPresent("Workflow Screen");

        clickLink("edit_transition");
        assertFormElementEquals("view", "2");
        submit("Update");
        assertTextPresent("Workflow Screen");

        clickLink("edit_transition");
        assertFormElementEquals("view", "2");
        selectOption("view", "No view for transition");
        assertFormElementEquals("view", "");
        submit("Update");
        assertTextPresent("None - it will happen instantly");

        clickLink("edit_transition");
        assertFormElementEquals("view", "");
        submit("Update");
        assertTextPresent("None - it will happen instantly");
    }

    /** Tests that the default value in the Workflow Transision stays the same. Fix for JRA-9550 */
    public void testTransitionView2()
    {
        copyWorkflowAndAddTransitions();
        administration.workflows().goTo().workflowSteps("Copied Workflow");
        clickLinkWithText(TRANSITION_NAME_2);
        assertTextPresent("None - it will happen instantly");

        clickLink("edit_transition");
        assertFormElementEquals("view", "");
        selectOption("destinationStep", "Closed");
        assertFormElementEquals("view", "");
        submit("Update");
        assertTextPresent("None - it will happen instantly");

        clickLink("edit_transition");
        assertFormElementEquals("view", "");
        selectOption("view", "Resolve Issue Screen");
        assertFormElementEquals("view", "3");
        submit("Update");
        assertTextPresent("Resolve Issue Screen");

        clickLink("edit_transition");
        assertFormElementEquals("view", "3");
        selectOption("destinationStep", "Resolved");
        assertFormElementEquals("view", "3");
        submit("Update");
        assertTextPresent("Resolve Issue Screen");

        clickLink("edit_transition");
        assertFormElementEquals("view", "3");
        selectOption("view", "No view for transition");
        assertFormElementEquals("view", "");
        submit("Update");
        assertTextPresent("None - it will happen instantly");
    }

}
