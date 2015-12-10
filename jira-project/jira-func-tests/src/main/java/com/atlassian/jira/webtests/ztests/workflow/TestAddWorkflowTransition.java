package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;

@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestAddWorkflowTransition extends FuncTestCase
{
    private static final String WORKFLOW_NAME = "Test Workflow";
    private static final String STEP_NAME_OPEN = "Open";
    private static final String STEP_NAME_AND = "SpecialChar&";
    private static final String STEP_NAME_LESSTHAN = "SpecialChar<";
    private static final String STEP_NAME_QUOTE = "SpecialChar\"";
    private static final String TRANSITION_NAME_AND = "To&";
    private static final String TRANSITION_NAME_LESSTHAN = "To<";
    private static final String TRANSITION_NAME_QUOTE = "To\"";
    private static final String ISSUE_STATUS_VALUE_ID = "status-val";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestAddWorkflowTransition.xml");
    }

    public void testAddWorkflowTransitionSpecialCharacters()
    {
        administration.workflows().goTo().workflowSteps(WORKFLOW_NAME).
                add(STEP_NAME_AND, null).
                add(STEP_NAME_LESSTHAN, null).
                add(STEP_NAME_QUOTE, null).
                addTransition(STEP_NAME_OPEN, TRANSITION_NAME_AND, null, STEP_NAME_AND, null).
                addTransition(STEP_NAME_AND, TRANSITION_NAME_LESSTHAN, null, STEP_NAME_LESSTHAN, null).
                addTransition(STEP_NAME_LESSTHAN, TRANSITION_NAME_QUOTE, null, STEP_NAME_QUOTE, null);

        // Associate the project with the new workflow
        backdoor.workflowSchemes().createScheme(new WorkflowSchemeData().setName(WORKFLOW_SCHEME).setDescription("Test workflow scheme.").setMapping(ISSUE_BUG, WORKFLOW_NAME));

        administration.project().associateWorkflowScheme(PROJECT_HOMOSAP, WORKFLOW_SCHEME);

        navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, "Test Special Characters");
        transitionCurrentIssueTo(TRANSITION_NAME_AND);
        transitionCurrentIssueTo(TRANSITION_NAME_LESSTHAN);
        transitionCurrentIssueTo(TRANSITION_NAME_QUOTE);
        assertCurrentIssueIsResolved();
    }

    private void transitionCurrentIssueTo(String transitionName)
    {
        navigation.clickLinkWithExactText(transitionName);
    }

    private void assertCurrentIssueIsResolved()
    {
        text.assertTextPresent(locator.id(ISSUE_STATUS_VALUE_ID), "Resolved");
    }
}
