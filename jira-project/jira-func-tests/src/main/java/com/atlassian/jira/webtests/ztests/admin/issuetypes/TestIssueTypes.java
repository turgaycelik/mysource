package com.atlassian.jira.webtests.ztests.admin.issuetypes;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.ISSUES })
public class TestIssueTypes extends FuncTestCase
{
    public void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void tearDownTest()
    {
        administration.restoreBlankInstance();
    }

    public void testDeleteIssueTypeAndMoveRelatedIssues()
    {
        navigation.issue().createIssue("homosapien", "Bug", "Test Issue 1");
        tester.gotoPage("secure/admin/DeleteIssueType!default.jspa?id=1");
        tester.assertTextPresent("Delete Issue Type: Bug");

        tester.assertTextPresent("There are currently <b>1</b> matching issues, that must be changed to another issue type.");
        tester.assertTextPresent("New type for matching issues");
        tester.assertFormElementPresent("newId");
        String[] valuesFor = tester.getDialog().getOptionsFor("newId");
        List list = EasyList.build(valuesFor);
        assertTrue(list.contains("Improvement"));
        assertTrue(list.contains("New Feature"));
        assertTrue(list.contains("Task"));

        tester.submit("Delete");
        tester.assertTextNotPresent("A problem which impairs or prevents the functions of the product.");
        tester.assertTextPresent("An improvement or enhancement to an existing feature or task.");
        tester.assertTextPresent("A new feature of the product, which has yet to be developed.");
        tester.assertTextPresent("A task that needs to be done.");
    }

    public void testDeleteIssueTypeWithNoRelatedIssues()
    {
        tester.gotoPage("secure/admin/DeleteIssueType!default.jspa?id=1");
        tester.assertTextPresent("Delete Issue Type: Bug");

        tester.assertTextPresent("There are currently no matching issues");
        tester.assertTextNotPresent("New type for matching issues:");
        tester.assertFormElementNotPresent("newId");

        tester.submit("Delete");
        tester.assertTextNotPresent("A problem which impairs or prevents the functions of the product.");
        tester.assertTextPresent("An improvement or enhancement to an existing feature or task.");
        tester.assertTextPresent("A new feature of the product, which has yet to be developed.");
        tester.assertTextPresent("A task that needs to be done.");
    }

    public void testRefuseToDeleteLastIssueType()
    {
        tester.gotoPage("secure/admin/DeleteIssueType!default.jspa?id=1");
        tester.submit("Delete");
        tester.gotoPage("secure/admin/DeleteIssueType!default.jspa?id=2");
        tester.submit("Delete");
        tester.gotoPage("secure/admin/DeleteIssueType!default.jspa?id=3");
        tester.submit("Delete");
        tester.assertTextNotPresent("A problem which impairs or prevents the functions of the product.");
        tester.assertTextNotPresent("A new feature of the product, which has yet to be developed.");
        tester.assertTextNotPresent("A task that needs to be done.");
        tester.assertTextPresent("An improvement or enhancement to an existing feature or task.");
        tester.assertLinkNotPresentWithText("Delete");
    }

    public void testRefuseToDeleteLastSubTaskIssueTypeWithRelatedIssues()
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        administration.subtasks().enable();

        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Test Issue 1");
        navigation.issue().createSubTask(issueKey, "Sub-task", "subtask test 1", "description");

        tester.gotoPage("secure/admin/DeleteIssueType!default.jspa?id=10000");
        tester.assertTextPresent("This issue type cannot be deleted - there are currently");
        tester.assertTextPresent("<b>1</b>");
        tester.assertTextPresent("In order for an issue type to be deleted, it needs to be associated with one workflow, field configuration and field screen scheme across all projects");
        tester.clickLinkWithText("1");
        tester.assertTextPresent("HSP-1");
        tester.assertSubmitButtonNotPresent("Delete");
    }

    public void testDeleteIssueTypeWithDifferentWorkflows()
    {
        administration.restoreData("TestDeleteIssueType.xml");
        tester.gotoPage("/secure/admin/DeleteIssueType!default.jspa?id=1");
        tester.assertTextPresent("This issue type cannot be deleted");
        tester.assertTextPresent("In order for an issue type to be deleted, it needs to be associated with one workflow, field configuration and field screen scheme across all projects");
        tester.clickLinkWithText("2");
        tester.assertTextPresent("MKY-1");
        tester.assertTextPresent("HSP-1");
    }
}