package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.url.URLUtil;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.webtests.util.RunOnce;

import java.net.URL;

/**
 *
 * @since v5.1
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestEditWorkflowDispatcher extends FuncTestCase
{
    private static final RunOnce RESTORE_ONCE = new RunOnce();

    private static final String INACTIVE_WORKFLOW = "Inactive Workflow";
    private static final String ACTIVE_WORKFLOW = "Active Workflow";
    private static final String SYSTEM_WORKFLOW = "jira";
    private static final String WORKFLOW_SCHEME_NAME = "Workflow Scheme";
    private static final String PROJECT_NAME = "Project";
    private static final String PROJECT_KEY = "PRJCT";

    @Override
    public void setUpTest()
    {
        RESTORE_ONCE.run(new Runnable()
        {
            @Override
            public void run()
            {
                administration.restoreBlankInstance();
            }
        });
    }

    public void testEditWorkflowDispatcherSystemWorkflow()
    {
        tester.gotoPage(page.addXsrfToken("/secure/admin/workflows/EditWorkflowDispatcher.jspa?wfName="+ URLUtil.encode(SYSTEM_WORKFLOW)));
        URL url = tester.getDialog().getResponse().getURL();
        assertions.getURLAssertions().assertURLAreSimilair("", environmentData.getBaseUrl() +"/secure/admin/workflows/ViewWorkflowSteps.jspa?workflowMode=live&workflowName="+ URLUtil.encode(SYSTEM_WORKFLOW), url.toString());
    }

    public void testEditWorkflowDispatcherWithInactiveWorkflow()
    {
        administration.workflows().goTo().addWorkflow(INACTIVE_WORKFLOW, "");
        tester.gotoPage(page.addXsrfToken("/secure/admin/workflows/EditWorkflowDispatcher.jspa?wfName="+ URLUtil.encode(INACTIVE_WORKFLOW)));
        URL url = tester.getDialog().getResponse().getURL();
        assertions.getURLAssertions().assertURLAreSimilair("", environmentData.getBaseUrl() +"/secure/admin/workflows/WorkflowDesigner.jspa?workflowMode=live&wfName="+ URLUtil.encode(INACTIVE_WORKFLOW), url.toString());
    }

    public void testEditWorkflowDispatcherWithActiveWorkflow()
    {
        administration.workflows().goTo().addWorkflow(ACTIVE_WORKFLOW, "");
        backdoor.workflowSchemes().createScheme(new WorkflowSchemeData().setName(WORKFLOW_SCHEME_NAME).setDefaultWorkflow(ACTIVE_WORKFLOW));
        administration.project().addProject(PROJECT_NAME, PROJECT_KEY, "admin");
        administration.project().associateWorkflowScheme(PROJECT_NAME, WORKFLOW_SCHEME_NAME);

        tester.gotoPage(page.addXsrfToken("/secure/admin/workflows/EditWorkflowDispatcher.jspa?wfName="+ URLUtil.encode(ACTIVE_WORKFLOW)));
        URL url = tester.getDialog().getResponse().getURL();
        assertions.getURLAssertions().assertURLAreSimilair("", environmentData.getBaseUrl() +"/secure/admin/workflows/WorkflowDesigner.jspa?workflowMode=draft&wfName="+ URLUtil.encode(ACTIVE_WORKFLOW), url.toString());
    }

    public void testEditWorkflowDispatcherWithInvalidWorkflow()
    {
        tester.gotoPage(page.addXsrfToken("/secure/admin/workflows/EditWorkflowDispatcher.jspa?wfName="));
        assertions.getJiraFormAssertions().assertFormErrMsg("It seems that you have tried to perform an illegal workflow operation.");
    }
}
