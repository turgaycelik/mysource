package com.atlassian.jira.webtests.ztests.workflow;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import org.hamcrest.Matchers;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkFlowActions extends JIRAWebTest
{
    public static final String PROJECT_KEY = "HSP";
    public static final String issueKey = "HSP-1";
    private static final String DODGY_WORKFLOW_NAME = "'><script>altert('hello')</script>";

    public TestWorkFlowActions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestWorkflowActions.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testCopyWorkflowWithXSS()
    {
        administration.workflows().goTo().addWorkflow(DODGY_WORKFLOW_NAME, "Some desc");

        administration.workflows().goTo();

        tester.clickLink("copy_" + DODGY_WORKFLOW_NAME);
        tester.assertTextPresent("&#39;&gt;&lt;script&gt;altert(&#39;hello&#39;)&lt;/script&gt;");
        tester.setFormElement("newWorkflowName", "Copy of " + DODGY_WORKFLOW_NAME);
        tester.submit("Update");

        tester.assertTextPresent("Copy of &#39;&gt;&lt;script&gt;altert(&#39;hello&#39;)&lt;/script&gt;");
    }

    public void testWorkFlowActions()
    {
        try
        {
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
            assignIssue(issueKey);
        }
        finally
        {
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }
        resolveIssue(issueKey);
        closeAndReopenIssue(issueKey);
        closeIssueFromOpen(issueKey);

        navigation.issue().deleteIssue(issueKey);
    }

    public void testInvalidWorkflowAction() throws Exception
    {
        navigation.issue().viewIssue(issueKey);
        tester.gotoPage("/secure/WorkflowUIDispatcher.jspa?id=10000&action=3&atl_token=" + page.getXsrfToken());
        tester.assertTextPresent("Workflow Action Invalid");

        // check the action name is present
        tester.assertTextPresent("Reopen Issue");

        tester.assertLinkPresent("refreshIssue");
    }

    public void testInvalidWorkflowActionDoesNotBreakThePage() throws Exception
    {
        administration.restoreData("TestWorkflowActionsDodgyWorkflow.xml");
        navigation.issue().viewIssue("TST-1");
        tester.assertTextPresent("Details");
        tester.assertLinkNotPresentWithText("Close Issue");
        tester.assertLinkNotPresentWithText("Resolve Issue");
    }

    //Test for JRA-18745
    public void testWorkflowWithReturnUrl() throws UnsupportedEncodingException
    {
        //Try to resolve an issue.
        tester.gotoPage("/secure/WorkflowUIDispatcher.jspa?id=10000&action=5&atl_token=" + page.getXsrfToken() + "&returnUrl=%2Fsecure%2FIssueNavigator.jspa%3Freset%3Dtrue%26jqlQuery%3Dproject%2B%3D%2BHSP%26selectedIssueId%3D10000");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");

        //Ensure that after resolving the issue we end up at the correct URL as given in the returnUrl parameter from the previous link.
        final String currentUrl = URLDecoder.decode(tester.getDialog().getResponse().getURL().toExternalForm(), "UTF-8");
        assertThat("Did not get redirected to the issue navigator.", currentUrl, Matchers.containsString("/issues/?jql=project = HSP"));
    }

    /** Put an issue through work flow */
    public void assignIssue(String issueKey)
    {
        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        // Add user to the jira-developers group. so that he can be assigned issues
        try
        {
            administration.usersAndGroups().addUserToGroup(BOB_USERNAME, Groups.DEVELOPERS);
        }
        catch (Throwable t)
        {
            log(BOB_USERNAME + " is already part of " + Groups.DEVELOPERS);
        }

        navigation.issue().assignIssue(issueKey, "issue assigned", BOB_FULLNAME);

        // Remove user from group
        administration.usersAndGroups().removeUserFromGroup(BOB_USERNAME, Groups.DEVELOPERS);

        // Re-assign issue to user with 'ASsignable User' permission
        navigation.issue().assignIssue(issueKey, "issue assigned", ADMIN_FULLNAME);
    }

    public void resolveIssue(String issueKey)
    {
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Open", "resolution", "Unresolved", "key", issueKey), null, issueKey);
        progressAndResolve(issueKey, 5, "issue resolved");
        //check that resolving issue updates the index
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Resolved", "resolution", "Fixed", "key", issueKey), null, issueKey);
    }

    public void closeAndReopenIssue(String issueKey)
    {
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Resolved", "resolution", "Fixed", "key", issueKey, "version", "New Version 1"), null, issueKey);
        progressWorkflow(issueKey, 701, "issue closed");
        //check that the workflow action has updated the index
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Closed", "key", issueKey), null, issueKey);
        progressWorkflow(issueKey, 3, "issue reopened");
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Reopened", "key", issueKey), null, issueKey);
    }

    public void closeIssueFromOpen(String issueKey)
    {
        progressAndResolve(issueKey, 2, "issue resolved and closed");
        progressWorkflow(issueKey, 3, "issue reopened");
    }

    public void testDeleteWorkflowLinkShownIfWorkflowIsUnassigned()
    {
        addWorkflow();
        administration.workflows().goTo();

        String deleteLinkId = getDeleteLinkId();
        tester.assertLinkPresent(deleteLinkId);
    }

    public void testDeleteWorkflowLinkIsNotShownIfWorkflowIsAssignedToScheme()
    {
        addWorkflow();
        assignToScheme();

        administration.workflows().goTo();

        String deleteLinkId = getDeleteLinkId();

        tester.assertLinkNotPresent(deleteLinkId);
    }

    public void testDeleteWorkflowLinkIsNotShownIfWorkflowIsAssignedToDraftScheme()
    {
        addWorkflow();
        assignToDraftScheme();

        administration.workflows().goTo();

        String deleteLinkId = getDeleteLinkId();

        tester.assertLinkNotPresent(deleteLinkId);
    }

    public void testCanDeleteUnassignedWorkflow()
    {
        addWorkflow();

        administration.workflows().goTo();

        String deleteLinkId = getDeleteLinkId();

        tester.clickLink(deleteLinkId);
        tester.submit("Delete");

        tester.assertTextNotPresent(DODGY_WORKFLOW_NAME);
    }

    private String getDeleteLinkId()
    {
        return "del_" + DODGY_WORKFLOW_NAME;
    }

    public void testCanNotDeleteWorkflowAssignedToScheme()
    {
        addWorkflow();
        assignToScheme();

        tester.gotoPage("/secure/admin/workflows/DeleteWorkflow.jspa?workflowMode=live&workflowName=" + DODGY_WORKFLOW_NAME + "&atl_token=" + page.getXsrfToken());

        tester.submit("Delete");

        tester.assertTextPresent("Cannot delete workflow as it is associated with the following schemes: &#39;WF scheme&#39;");
    }

    public void testCanNotDeleteWorkflowAssignedToDraftScheme()
    {
        addWorkflow();
        assignToDraftScheme();

        tester.gotoPage("/secure/admin/workflows/DeleteWorkflow.jspa?workflowMode=live&workflowName=" + DODGY_WORKFLOW_NAME + "&atl_token=" + page.getXsrfToken());

        tester.submit("Delete");

        tester.assertTextPresent("Cannot delete workflow as it is associated with the following schemes: Draft of &#39;Default Workflow Scheme&#39;");
    }

    public void testCanNotDeleteSystemWorkflow()
    {
        tester.gotoPage("/secure/admin/workflows/DeleteWorkflow.jspa?workflowMode=live&workflowName=jira&atl_token=" + page.getXsrfToken());

        tester.submit("Delete");

        tester.assertTextPresent("Workflow cannot be deleted as it is not editable.");
    }

    public void testErrorIsShownIfTryingToDeleteNonExistingWorkflow()
    {
        tester.gotoPage("/secure/admin/workflows/DeleteWorkflow.jspa?workflowMode=live&workflowName=Not existing WF&atl_token=" + page.getXsrfToken());

        tester.submit("Delete");

        tester.assertTextPresent("Workflow with name &#39;&#39;Not existing WF&#39;&#39; does not exist.");
    }

    private void addWorkflow()
    {
        administration.workflows().goTo().addWorkflow(DODGY_WORKFLOW_NAME, "desc");
    }

    private void assignToScheme()
    {
        backdoor.workflowSchemes().createScheme(new WorkflowSchemeData().setName("WF scheme").setDescription("desc").setMapping(ISSUE_BUG, DODGY_WORKFLOW_NAME));
    }

    private void assignToDraftScheme()
    {
        administration.project().createWorkflowSchemeDraft(PROJECT_KEY);
        administration.project().assignToDraftScheme(PROJECT_KEY, DODGY_WORKFLOW_NAME, "1");
    }
}
