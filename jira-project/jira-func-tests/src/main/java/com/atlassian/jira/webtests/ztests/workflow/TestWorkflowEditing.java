package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import static com.atlassian.jira.functest.framework.admin.ViewWorkflows.WorkflowState;

/**
 * Test that workflow editing works ok.
 * TODO: currently only checks that subtask blocking conditions work
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowEditing extends JIRAWebTest
{

    public TestWorkflowEditing(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("editableworkflow.xml");
    }

    /**
     * Confirming the add, edit and delete operations on subtask blocking conditions.
     * Originally added to cover JRA-9934.
     */
    public void testSubtaskBlockingConditions()
    {
        administration.workflows().goTo().workflowSteps("Copy of jira");
        tester.clickLinkWithText("Close Issue");

        // add new subtask blocking condition
        tester.clickLinkWithText("Add condition", 0);
        tester.checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:subtaskblocking-condition");
        tester.submit("Add");
        tester.checkCheckbox("issue_statuses", "1");
        tester.checkCheckbox("issue_statuses", "3");
        tester.checkCheckbox("issue_statuses", "4");
        tester.submit("Add");
        assertTextSequence(new String[] {
                "All sub-tasks must have one of the following statuses to allow parent issue transitions:",
                "Open", "In Progress", "or", "Reopened" });

        // now edit and add a status
        tester.clickLinkWithText("Edit", 3);
        tester.checkCheckbox("issue_statuses", "1");
        tester.checkCheckbox("issue_statuses", "3");
        tester.checkCheckbox("issue_statuses", "4");
        tester.checkCheckbox("issue_statuses", "5");
        tester.submit("Update");
        assertTextSequence(new String[] {
                "All sub-tasks must have one of the following statuses to allow parent issue transitions:",
                "Open",
                "In Progress",
                "Reopened",
                "or",
                "Resolved" });

        // test the condition is deleted
        tester.clickLinkWithText("Delete", 3);
        tester.assertTextNotPresent("All sub-tasks must have one of the following statuses to allow parent issue transitions:");
    }

    public void testWorkflowAddFromXmlNotAvailableToAdmins()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            administration.workflows().goTo();

            assertFalse(administration.workflows().isImportWorkflowFromXmlButtonPresent());
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testWorkflowAddFromXmlAvailableToSysAdmins()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            administration.workflows().goTo();

            assertTrue(administration.workflows().isImportWorkflowFromXmlButtonPresent());
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }
}
