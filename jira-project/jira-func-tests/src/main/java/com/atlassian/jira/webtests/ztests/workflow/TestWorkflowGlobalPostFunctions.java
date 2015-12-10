package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowGlobalPostFunctions extends JIRAWebTest
{
    private static final String workflowName = "GlobalPostFunctions";

    public TestWorkflowGlobalPostFunctions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("TestWorkflowGlobalPostFunctions.xml");
    }

    public void testGlobalPostFunctionsCreatedFromCustomImportedWorkflowXMLDisplayOnOtherTab()
    {
        administration.workflows().goTo().workflowSteps(workflowName);

        tester.assertTextPresent("Workflow");
        assertions.assertNodeHasText(new CssLocator(tester, "h2 .workflow-name"), "GlobalPostFunctions");
        tester.clickLinkWithText("Move to Intermediate");

        assertions.assertNodeHasText(new CssLocator(tester, ".aui-page-header h2"), "Move to Intermediate");
        assertions.assertNodeHasText(new CssLocator(tester, ".menu-item #view_other"), "Other");
        tester.clickLink("view_other");

        assertions.assertNodeHasText(new CssLocator(tester, ".active-tab #view_other"), "Other");
        assertions.assertNodeHasText(new CssLocator(tester, ".active-pane .criteria-group-actions"), "Global Post Functions");
    }
}
