package com.atlassian.jira.webtests.ztests.admin.statuses;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the Statuses administration interface
 *
 * @since v3.12.4
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestStatuses extends FuncTestCase
{
    public void testWorkflowLinksUrlEncoded()
    {
        administration.restoreData("TestStatuses.xml");
        tester.gotoPage("secure/admin/ViewWorkflowsForStatus.jspa?id=1");
        tester.assertTextNotPresent("workflowName=<xxx>Crazy workflow</xxx>");
        tester.assertTextPresent("workflowName=%3Cxxx%3ECrazy%20workflow%3C%2Fxxx%3E");
    }

    public void testDeleteActiveStatusViaUrlFails() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdmin();
        // Click Link 'Statuses' (id='statuses').
        tester.clickLink("statuses");

        // Confirm that there is no inactive status
        tester.assertLinkNotPresentWithText("Delete");

        // Try to delete active status
        tester.gotoPage("secure/admin/DeleteStatus!default.jspa?id=1");
        tester.assertTextPresent("Delete status: Open");
        tester.assertTextPresent("Confirm that you want to delete this status ...");
        tester.assertTextNotPresent("Given status is currently associated with a workflow(s) and cannot be deleted");
        tester.submit("Delete");

        // Assert user gets the error message
        tester.assertTextPresent("Delete status: Open");
        tester.assertTextPresent("Confirm that you want to delete this status ...");
        tester.assertTextPresent("Given status is currently associated with a workflow(s) and cannot be deleted");
    }

    public void testDeletePassiveStatusViaSucceeds() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdmin();
        // Click Link 'Statuses' (id='statuses').
        tester.clickLink("statuses");

        // Confirm that there is no inactive status
        tester.assertLinkNotPresentWithText("Delete");

        // Add new status
        tester.gotoPage("secure/admin/AddStatus!default.jspa");
        tester.setFormElement("name", "Not used");
        tester.setFormElement("description", "why bother");
        tester.submit("Add");

        // Confirm that new status was created and is inactive
        tester.assertTextPresent("Not used");
        tester.assertTextPresent("why bother");
        tester.assertLinkPresentWithText("Delete");
        tester.assertLinkPresent("del_10000");

        // Delete inactive status
        tester.gotoPage("secure/admin/DeleteStatus!default.jspa?id=10000");
        tester.assertTextPresent("Delete status: Not used");
        tester.assertTextPresent("Confirm that you want to delete this status ...");
        tester.submit("Delete");

        // Confirm status deleted
        tester.assertTextNotPresent("Not used");
        tester.assertTextNotPresent("why bother");
        tester.assertLinkNotPresentWithText("Delete");
        tester.assertLinkNotPresent("del_10000");
    }

    //JRA-18985: Make sure that we can't change a status such that it can be become a duplicate.
    public void testEditStatusAlreadyExists() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdmin();

        // Click Link 'Statuses' (id='statuses').
        tester.clickLink("statuses");

        //Check to see that we can't add a status of the same name.
        tester.gotoPage("secure/admin/AddStatus!default.jspa");
        tester.setFormElement("name", "closed");
        tester.submit();
        assertDuplicateStatusError();

        tester.setFormElement("name", "CLOSED");
        tester.submit();
        assertDuplicateStatusError();

        //Check to see that we can't change a status name to an already existing name.
        tester.gotoPage("secure/admin/EditStatus!default.jspa?id=5");

        //Check to see that we can't edit a status such that becomes a duplicate.
        tester.setFormElement("name", "closed");
        tester.submit();
        assertDuplicateStatusError();

        tester.setFormElement("name", "CLOSED");
        tester.submit();
        assertDuplicateStatusError();
    }

    //JRA-22219: Make sure the change to a staus name is seen in the workflow.
    public void testRenamePropagates() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdmin();

        // Goto the page and look at the status.  We need this to set up the cache originally.
        tester.clickLink("workflows");
        tester.clickLink("steps_live_jira");

        navigation.gotoAdmin();

        // Click Link 'Statuses' (id='statuses').
        tester.clickLink("statuses");

        //Check to see that we can't change a status name to an already existing name.
        tester.gotoPage("secure/admin/EditStatus!default.jspa?id=5");

        //Check to see we are in the right place
        tester.assertFormElementEquals("name", "Resolved");

        tester.setFormElement("name", "what-do-you-mean-resolved");
        tester.submit();

        navigation.gotoAdmin();
        tester.clickLink("workflows");

        tester.clickLink("steps_live_jira");
        tester.assertTextPresent("what-do-you-mean-resolved");

        //Restore our state so other tests don't fail.
        tester.gotoPage("secure/admin/EditStatus!default.jspa?id=5");
        tester.setFormElement("name", "Resolved");
        tester.submit();

    }

    private void assertDuplicateStatusError()
    {
        assertEquals("A status with that name already exists, please enter a different name.", locator.css(".error").getNode().getNodeValue());
    }
}
