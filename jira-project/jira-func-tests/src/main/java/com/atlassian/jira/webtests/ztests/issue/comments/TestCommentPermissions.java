package com.atlassian.jira.webtests.ztests.issue.comments;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests Permissions to delete and edit Comments.
 * See http://jira.atlassian.com/browse/JRA-16138
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.COMMENTS, Category.PERMISSIONS })
public class TestCommentPermissions extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestCommentPermissions.xml");
    }

    public void testAdminCanEditAndDeleteAllComments() throws Exception
    {
        // Browse to RAT-2
        navigation.issue().viewIssue("RAT-2");
        // Should see 3 comments
        tester.assertTextPresent("Commented by admin");
        tester.assertTextPresent("Commented by Fred");
        tester.assertTextPresent("Anonymous comment");

        // Being Project admin, he has Edit All Comments and Delete All Comments.
        // Assert the links are there
        tester.assertLinkPresent("edit_comment_10000");
        tester.assertLinkPresent("delete_comment_10000");
        tester.assertLinkPresent("edit_comment_10001");
        tester.assertLinkPresent("delete_comment_10001");
        tester.assertLinkPresent("edit_comment_10002");
        tester.assertLinkPresent("delete_comment_10002");

        // test that admin can edit other user's comments
        tester.clickLink("edit_comment_10001");
        tester.setWorkingForm("comment-edit");
        tester.setFormElement("comment", "admin can edit Fred's comment");
        tester.submit("Save");
        // Back on the issue page
        tester.assertTextPresent("Commented by admin");
        tester.assertTextPresent("admin can edit Fred&#39;s comment");
        tester.assertTextPresent("Anonymous comment");

        // test that admin can even edit the anonymous comment.
        tester.clickLink("edit_comment_10002");
        tester.setWorkingForm("comment-edit");
        tester.setFormElement("comment", "admin can edit anonymous");
        tester.submit("Save");
        // Back on the issue page
        tester.assertTextPresent("Commented by admin");
        tester.assertTextPresent("admin can edit Fred&#39;s comment");
        tester.assertTextPresent("admin can edit anonymous");

        // Assert he can delete Fred's
        tester.clickLink("delete_comment_10001");
        tester.submit("Delete");
        // Back on the issue page
        tester.assertTextPresent("Commented by admin");
        tester.assertTextNotPresent("admin can edit Fred&#39;s comment");
        tester.assertTextPresent("admin can edit anonymous");

        // Assert he can delete anon
        tester.clickLink("delete_comment_10002");
        tester.submit("Delete");
        // Back on the issue page
        tester.assertTextPresent("Commented by admin");
        tester.assertTextNotPresent("admin can edit Fred&#39;s comment");
        tester.assertTextNotPresent("admin can edit anonymous");
    }

    public void testCantEditOrDeleteCommentsInClosedIssues() throws Exception
    {
        // Browse to RAT-2
        navigation.issue().viewIssue("RAT-2");
        // Should see 3 comments
        tester.assertTextPresent("Commented by admin");
        tester.assertTextPresent("Commented by Fred");
        tester.assertTextPresent("Anonymous comment");

        // Being Project admin, he has Edit All Comments and Delete All Comments.
        // Assert the links are there
        tester.assertLinkPresent("edit_comment_10000");
        tester.assertLinkPresent("delete_comment_10000");
        tester.assertLinkPresent("edit_comment_10001");
        tester.assertLinkPresent("delete_comment_10001");
        tester.assertLinkPresent("edit_comment_10002");
        tester.assertLinkPresent("delete_comment_10002");

        // Click Link 'Close Issue' (id='action_id_2').
        tester.clickLink("action_id_2");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");

        // Edit and delete comments should disappear
        tester.assertLinkNotPresent("edit_comment_10000");
        tester.assertLinkNotPresent("delete_comment_10000");
        tester.assertLinkNotPresent("edit_comment_10001");
        tester.assertLinkNotPresent("delete_comment_10001");
        tester.assertLinkNotPresent("edit_comment_10002");
        tester.assertLinkNotPresent("delete_comment_10002");
    }

    public void testFredCanEditAndDeleteOwnComments() throws Exception
    {
        // log in as fred
        navigation.login(FRED_USERNAME);

        // Browse to RAT-2
        navigation.issue().viewIssue("RAT-2");
        // Should see 3 comments
        tester.assertTextPresent("Commented by admin");
        tester.assertTextPresent("Commented by Fred");
        tester.assertTextPresent("Anonymous comment");

        // Being normal user, he has Edit own Comments and Delete own Comments.
        // Assert the links are there only for his own comment
        tester.assertLinkNotPresent("edit_comment_10000");
        tester.assertLinkNotPresent("delete_comment_10000");
        tester.assertLinkPresent("edit_comment_10001");
        tester.assertLinkPresent("delete_comment_10001");
        tester.assertLinkNotPresent("edit_comment_10002");
        tester.assertLinkNotPresent("delete_comment_10002");

        // test that fred can edit his own comment
        tester.clickLink("edit_comment_10001");
        tester.setWorkingForm("comment-edit");
        tester.setFormElement("comment", "Fred can edit Fred's comment");
        tester.submit("Save");
        // Back on the issue page
        tester.assertTextPresent("Commented by admin");
        tester.assertTextPresent("Fred can edit Fred&#39;s comment");
        tester.assertTextPresent("Anonymous comment");

        // Assert he can delete Fred's
        tester.clickLink("delete_comment_10001");
        tester.submit("Delete");
        // Back on the issue page
        tester.assertTextPresent("Commented by admin");
        tester.assertTextNotPresent("Fred can edit Fred&#39;s comment");
        tester.assertTextPresent("Anonymous comment");
    }

    public void testAnonCanEditAndDeleteNothing() throws Exception
    {
        // logout (now I'm anonymous).
        navigation.logout();

        // Browse to RAT-2
        navigation.issue().viewIssue("RAT-2");
        // Should see 3 comments
        tester.assertTextPresent("Commented by admin");
        tester.assertTextPresent("Commented by Fred");
        tester.assertTextPresent("Anonymous comment");

        // Shouldn't be able to delete/edit anything
        tester.assertLinkNotPresent("edit_comment_10000");
        tester.assertLinkNotPresent("delete_comment_10000");
        tester.assertLinkNotPresent("edit_comment_10001");
        tester.assertLinkNotPresent("delete_comment_10001");
        tester.assertLinkNotPresent("edit_comment_10002");
        tester.assertLinkNotPresent("delete_comment_10002");
    }

}
