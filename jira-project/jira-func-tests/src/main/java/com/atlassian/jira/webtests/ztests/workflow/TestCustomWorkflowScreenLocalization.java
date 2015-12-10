package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *  This func test verifies that the i18n of custom workflow screens is working.
 *  The i18n keys for some labels are retrieved from properties of the transition.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestCustomWorkflowScreenLocalization extends FuncTestCase
{

    public static final String WORKFLOW_NAME = "Workflow2";

    public void setUpTest()
    {
        administration.restoreData("TestCustomWorkflowScreenLocalization.xml");
    }


    public void testSubmitButtonLabelIsTransitionName()
    {
        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");
    }

    public void testSubmitButtonLabelIsLocalized()
    {
        administration.workflows().goTo().createDraft(WORKFLOW_NAME);

        backdoor.workflow().setTransitionProperty(WORKFLOW_NAME, true, 11, "jira.i18n.submit", "resolveissue.title");

        administration.workflows().goTo().publishDraft(WORKFLOW_NAME).publish();

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve Issue");
    }


    public void testFallBackToTransitionName()
    {
        administration.workflows().goTo().createDraft(WORKFLOW_NAME);
        administration.workflows().goTo().edit(WORKFLOW_NAME).textView().goTo();

        backdoor.workflow().setTransitionProperty(WORKFLOW_NAME, true, 11, "jira.i18n.submit", "blah.doesnt.exist");

        administration.workflows().goTo().publishDraft(WORKFLOW_NAME).publish();

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");
    }

    public void testTransitionNameTitle()
    {
        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");
        tester.assertTitleEquals("Resolve [HMS-1] - Your Company JIRA");
    }

    public void testLocalizedTitle()
    {
        administration.workflows().goTo().createDraft(WORKFLOW_NAME);
        administration.workflows().goTo().edit(WORKFLOW_NAME).textView().goTo();

        backdoor.workflow().setTransitionProperty(WORKFLOW_NAME, true, 11, "jira.i18n.title", "resolveissue.title");

        administration.workflows().goTo().publishDraft(WORKFLOW_NAME).publish();

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");

        tester.assertTitleEquals("Resolve Issue [HMS-1] - Your Company JIRA");

    }


    public void testFixedDescription()
    {
        administration.workflows().goTo().createDraft(WORKFLOW_NAME);
        administration.workflows().goTo().edit(WORKFLOW_NAME).textView().goTo();

        backdoor.workflow().setTransitionProperty(WORKFLOW_NAME, true, 11, "description", "My Special description");

        administration.workflows().goTo().publishDraft(WORKFLOW_NAME).publish();

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");

        tester.assertTextPresent("My Special description");
    }

}
