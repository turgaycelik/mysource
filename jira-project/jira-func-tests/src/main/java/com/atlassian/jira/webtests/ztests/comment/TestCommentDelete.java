package com.atlassian.jira.webtests.ztests.comment;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * 
 */
@WebTest ({ Category.FUNC_TEST, Category.COMMENTS })
public class TestCommentDelete extends JIRAWebTest
{
    public TestCommentDelete(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestCommentDelete.xml");

    }

    public void testDeleteOwnCommentWithOwnPermission()
    {
        logout();
        // Login as simple user (with "Delete Own Comment" permission)
        login("detkin", "detkin");

        // Attempt to delete own comment
        gotoIssue("TST-1");
        clickLink("delete_comment_10020");

        submit("Delete");

        // Assert comment has been deleted
        assertLinkNotPresent("delete_comment_10020");
    }

    public void testDeleteCommentWithAllPermission()
    {
        gotoIssue("TST-1");
        clickLink("delete_comment_10020");

        submit("Delete");

        // Assert comment has been deleted
        assertLinkNotPresent("delete_comment_10020");
    }

    public void testDeleteOthersCommentWithOwnPermission()
    {
        // Login as another user (with the "Delete Own Attachment" permission) and assert that the delete link is hidden
        logout();
        login("barney", "barney");
        gotoIssue("TST-1");
        // Assert comment has been deleted
        assertLinkNotPresent("delete_comment_10020");
    }

    public void testDeleteCommentLinkNotAvailableWithNonEditableWorkflowState()
    {
        gotoIssue("TST-1");
        // Close the issue
        clickLinkWithText(TRANSIION_NAME_CLOSE);
        setWorkingForm("issue-workflow-transition");
        submit("Transition");

        // We should not be able to delete the comment
        assertLinkNotPresent("delete_comment_10020");
    }

    public void testDeleteCommentReindexesIssue()
    {
        // Check to see if returned in search
        navigation.issueNavigator().createSearch("comment ~ \"Unique Comment\"");
        assertTextPresent("TST-1");

        // Delete the comment
        gotoIssue("TST-1");
        clickLink("delete_comment_10020");
        submit("Delete");

        // Check to see that the issue is not returned in the search (i.e. it has reindexed)
        navigation.issueNavigator().createSearch("comment ~ \"Unique Comment\"");
        assertTextNotPresent("TST-1");
        assertElementNotPresent("issuetable");
    }

    public void testDeleteCommentWithNoPermissions()
    {
        // Remove the permission to delete a comment
        removeRolePermission(36, 10002);
        removeRolePermission(37, 10000);

        // Jump to the page
        gotoPage("/secure/DeleteComment!default.jspa?id=10000&commentId=10020");
        tester.assertTitleEquals("Error - Your Company JIRA");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "You do not have the permission for this comment.");
    }

    public void testDeleteCommentWithNonEditableWorkflowState()
    {
        // Close the issue
        gotoIssue("TST-1");
        clickLinkWithText(TRANSIION_NAME_CLOSE);
        setWorkingForm("issue-workflow-transition");
        submit("Transition");

        // Jump to the page
        gotoPage("/secure/DeleteComment!default.jspa?id=10000&commentId=10020");

        tester.assertTitleEquals("Error - Your Company JIRA");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "You do not have the permission for this comment.");
    }

    public void testOneCanNotDeleteCommentThatOneCanNotSee()
    {
        // admin adds comment on 'Developers' role level
        gotoIssue("TST-1");
        addCommentOnCurrentIssue("This comment is visible by developers only!", "Developers");
        assertTextPresent("This comment is visible by developers only!");

        // add user 'barney' to the administrator group
        addUserToGroup("barney", "jira-administrators");

        // login as 'barney' user
        logout();
        login("barney", "barney");

        // go to 'TST-1' issue and assert that cannot see the comment
        gotoIssue("TST-1");
        assertTextNotPresent("This comment is visible by developers only!");

        // now try to delete the comment
        gotoPage("/secure/DeleteComment!default.jspa?id=10000&commentId=10030");
        tester.assertTitleEquals("Error - Your Company JIRA");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "You do not have the permission for this comment.");

        // now try to delete the comment
        gotoPage("/secure/DeleteComment!default.jspa?id=10000&commentId=100301"); // invalid comment id
        tester.assertTitleEquals("Error - Your Company JIRA");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "You do not have the permission for this comment.");

        // login as 'admin' user
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);

        // go to 'TST-1' issue and assert that comment is still there
        gotoIssue("TST-1");
        assertTextPresent("This comment is visible by developers only!");
    }

    public void testOneCanNotEditCommentThatOneCanNotSee()
    {
        // admin adds comment on 'Developers' role level
        gotoIssue("TST-1");
        addCommentOnCurrentIssue("This comment is visible by developers only!", "Developers");
        assertTextPresent("This comment is visible by developers only!");

        // add user 'barney' to the administrator group
        addUserToGroup("barney", "jira-administrators");

        // login as 'barney' user
        logout();
        login("barney", "barney");

        // go to 'TST-1' issue and assert that cannot see the comment
        gotoIssue("TST-1");
        assertTextNotPresent("This comment is visible by developers only!");

        // now try to edit the comment
        gotoPage("/secure/EditComment!default.jspa?id=10000&commentId=10030");
        tester.assertTitleEquals("Error - Your Company JIRA");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "You do not have the permission for this comment.");


        // now try to edit the comment
        gotoPage("/secure/EditComment!default.jspa?id=10000&commentId=100301"); // invalid comment id
        tester.assertTitleEquals("Error - Your Company JIRA");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "You do not have the permission for this comment.");


        // login as 'admin' user
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);

        // go to 'TST-1' issue and assert that comment is still there
        gotoIssue("TST-1");
        assertTextPresent("This comment is visible by developers only!");
    }

}
