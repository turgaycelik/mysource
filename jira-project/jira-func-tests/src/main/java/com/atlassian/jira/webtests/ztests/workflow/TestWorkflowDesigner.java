package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.ViewWorkflows;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * The Workflow Designer is a (bundled) plugin
 *
 * We are testing it here because we can't figure out how to make tests work inside the actual plugin
 * (integration tests get run as unit tests, or not at all, weird problems)
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowDesigner extends FuncTestCase
{
    protected void setUpTest ()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testWorkflowDesignerValidWorkflowName()
    {
        ViewWorkflows viewWorkflows = administration.workflows().goTo();

        final String name = "UNIQUE_WORKFLOW_NAME";
        final String description = "UNIQUE_WORKFLOW_DESCRIPTION";

        viewWorkflows.addWorkflow(name, description);

        viewWorkflows.launchDesigner(name);

        tester.assertTextPresent(name);
        tester.assertTextPresent(description);
    }

    public void testWorkflowDesignerInvalidWorkflowName()
    {
        final String name = "UNIQUE_WORKFLOW_NAME";

        navigation.gotoPage("secure/admin/workflows/WorkflowDesigner.jspa?wfName=" + name);

        tester.assertTextPresent("No workflow with name '" + name + "' could be found.");
    }

    public void testWorkflowDesignerNoWorkflowName()
    {
        administration.workflows().goTo();

        final String name = "";

        navigation.gotoPage("secure/admin/workflows/WorkflowDesigner.jspa?wfName=" + name);

        tester.assertTextPresent("No workflow with name '" + name + "' could be found.");
    }

    public void testWorkflowDesignerScriptTagInNameAndDescription()
    {
        ViewWorkflows viewWorkflows = administration.workflows().goTo();

        final String name = "\"><script>alert('hello');</script>";
        final String description = "\"><script>alert('world');</script>";

        viewWorkflows.addWorkflow(name, description);

        viewWorkflows.launchDesigner(name);

        tester.assertTextNotPresent(name);
        tester.assertTextNotPresent(description);

        tester.assertTextPresent("&gt;&lt;script&gt;alert(&#39;hello&#39;);&lt;/script&gt;");
        tester.assertTextPresent("&gt;&lt;script&gt;alert(&#39;world&#39;);&lt;/script&gt;");
    }

    public void testOldWorkflowEditorLinksToDesigner()
    {
        ViewWorkflows viewWorkflows = administration.workflows().goTo();

        final String name = "UNIQUE_WORKFLOW_NAME";
        final String description = "UNIQUE_WORKFLOW_DESCRIPTION";

        viewWorkflows.addWorkflow(name, description);

        viewWorkflows.workflowSteps(name);

        tester.assertLinkPresentWithText("Diagram");
        tester.clickLinkWithText("Diagram");
        tester.assertTextPresent("Workflow Designer");
        tester.assertTextPresent(name);
    }
}
