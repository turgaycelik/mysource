package com.atlassian.jira.webtests.ztests.comment;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.xml.sax.SAXException;

import java.util.Collections;

import static com.atlassian.jira.permission.ProjectPermissions.ADD_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_ALL_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_OWN_COMMENTS;

@WebTest ({ Category.FUNC_TEST, Category.COMMENTS })
public class TestEditComment extends JIRAWebTest
{
    private static final String ISSUE_KEY = "HSP-1";

    private static final String COMMENT_ID_ADMIN = "10031";
    private static final String COMMENT_ID_FRED = "10040";
    private static final String COMMENT_ID_ANON = "10041";
    private static final String OLD_COMMENT_ANON = "Just a random comment by noname.";
    private static final String OLD_COMMENT_ADMIN = "I&#39;m a hero!";
    private static final String OLD_COMMENT_FRED = "Australia is an island continent.";
    private static final String NEW_COMMENT_ANON = "Firefox rocks!";
    private static final String NEW_COMMENT_ADMIN = "Thunderbird is cool!";
    private static final String NEW_COMMENT_FRED = "Linux just works!";

    private static final String SAVE_BUTTON_NAME = "Save";

    public TestEditComment(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestEditComment.xml");
    }

    public void testSimpleCommentEdit()
    {
        gotoIssue("HSP-1");
        clickLink("edit_comment_10031");
        setWorkingForm("comment-edit");
        assertTextPresent(OLD_COMMENT_ADMIN);
        setFormElement("comment", "new func test value");
        submit(SAVE_BUTTON_NAME);

        // Make sure we got back to the view issue page
        assertTextPresent("Edit comment issue summary for our test");

        // Make sure the updated comment is there
        assertTextPresent("new func test value");
    }

    public void testSimpleErrorNoBodyProvided()
    {
        gotoIssue("HSP-1");
        clickLink("edit_comment_10031");
        setWorkingForm("comment-edit");
        assertTextPresent(OLD_COMMENT_ADMIN);
        setFormElement("comment", "");
        submit(SAVE_BUTTON_NAME);

        // Make sure we do not go back to the view issue page
        tester.assertFormElementPresent("comment");
        
        // Make sure the updated comment is there
        assertTextPresent("Comment body can not be empty!");

        // Make sure that user can re-submit the form - Save button is present
        assertFormElementPresent(SAVE_BUTTON_NAME);
    }

    public void testCommentEditPermissionGroup() throws SAXException
    {
        final String COMMENT1 = "This comment is updated";

        gotoIssue("HSP-1");
        clickLink("edit_comment_10031");
        setWorkingForm("comment-edit");
        assertTextPresent(OLD_COMMENT_ADMIN);
        setFormElement("comment", COMMENT1);
        selectOption("commentLevel", "jira-developers");
        submit(SAVE_BUTTON_NAME);

        // Make sure we got back to the view issue page
        assertTextPresent("Edit comment issue summary for our test");

        // Make sure the updated comment is there
        assertTextPresent(COMMENT1);
        assertTextPresent("Restricted to <span class=redText>jira-developers</span>");

        // Verify that the correct option is selected by default
        clickLink("edit_comment_10031");
        assertOptionEquals("commentLevel", "jira-developers");
    }

    public void testCommentEditPermissionRole() throws SAXException
    {
        final String COMMENT1 = "This comment is updated";

        gotoIssue("HSP-1");
        clickLink("edit_comment_10031");
        setWorkingForm("comment-edit");
        assertTextPresent(OLD_COMMENT_ADMIN);
        setFormElement("comment", COMMENT1);
        selectOption("commentLevel", "Developers");
        submit(SAVE_BUTTON_NAME);

        // Make sure we got back to the view issue page
        assertTextPresent("Edit comment issue summary for our test");

        // Make sure the updated comment is there
        assertTextPresent(COMMENT1);
        assertTextPresent("Restricted to <span class=redText>Developers</span>");

        // Verify that the correct option is selected by default
        clickLink("edit_comment_10031");
        assertOptionEquals("commentLevel", "Developers");
    }

    public void testCommentEditRemovePermission() throws SAXException
    {
        final String COMMENT1 = "This comment is updated";

        gotoIssue("HSP-1");
        clickLink("edit_comment_10031");
        setWorkingForm("comment-edit");
        assertTextPresent(OLD_COMMENT_ADMIN);
        setFormElement("comment", COMMENT1);
        selectOption("commentLevel", "All Users");
        submit(SAVE_BUTTON_NAME);

        // Make sure we got back to the view issue page
        assertTextPresent("Edit comment issue summary for our test");

        // Make sure the updated comment is there
        assertTextPresent(COMMENT1);
        assertTextNotPresent("Restricted to <span class=redText>Administrators</span>");
    }

    public void testCommentEditCommentNotEditedBefore() throws SAXException
    {
        gotoIssue("HSP-1");

        clickLink("edit_comment_10031");
        assertTextPresent(OLD_COMMENT_ADMIN);

        // Make sure that the edit author and date are not shown since this comment has not been updated yet.

        assertTextNotPresent("Edited by");
        assertTextNotPresent("Edited on");
    }

    public void testCommentEditCommentIsSearchable() throws SAXException
    {
        gotoIssue("HSP-1");

        clickLink("edit_comment_10031");
        setWorkingForm("comment-edit");
        assertTextPresent(OLD_COMMENT_ADMIN);

        final String STRANGE_COMMENT = "STRANGE COMMENT";
        setFormElement("comment", STRANGE_COMMENT);
        selectOption("commentLevel", "All Users");
        submit(SAVE_BUTTON_NAME);

        navigation.issueNavigator().createSearch("comment ~ \"STRANGE COMMENT\"");
        assertTextPresent("Edit comment issue summary for our test");
    }

    /**
     * Test that you can create a comment when not logged in
     */
    public void testAnonymousCanCreateComment()
    {
        addCreateCommentPermissionToAnonymous();
        logout();
        gotoPage("/secure/Dashboard.jspa");
        gotoIssue(ISSUE_KEY);
        final String newComment = "Can I add a comment?";
        setFormElement("comment", newComment);
        submit();
        assertTextPresent(newComment);
    }

    public void testAnonymousCanSeeAllComments()
    {
        logout();
        gotoPage("/secure/Dashboard.jspa");
        gotoIssue(ISSUE_KEY);
        assertTextPresent(OLD_COMMENT_ANON);
        assertTextPresent(OLD_COMMENT_ADMIN);
        assertTextPresent(OLD_COMMENT_FRED);
    }

    public void testNoEditPermissions()
    {
        // By default, in the imported data, we have the EDIT_ALL permission granted to ANYONE and jira-administrators
        removeEditAllCommentsPermission("jira-administrators");
        removeEditAllCommentsPermission(""); // delete permission for anonymous

        // By default, in the imported data, we have the EDIT_OWN permission granted to ANYONE and jira-users
        removeEditOwnCommentsPermission("jira-users");
        removeEditOwnCommentsPermission(""); // delete permission for anonymous

        assertAdminCanEditAnonymousComment(false);
        assertAdminCanEditAdminsComment(false);
        assertAdminCanEditFredsComment(false);
        logout();
        assertAnonymousCanEditAnonymousComment(false);
        assertAnonymousCanEditAdminsComment(false);
        assertAnonymousCanEditFredsComment(false);
    }

    public void testEditOwnPermissions()
    {
        // By default, in the imported data, we have the EDIT_ALL permission granted to jira-administrators
        removeEditAllCommentsPermission("jira-administrators");
        removeEditAllCommentsPermission(""); // delete permission for anonymous
        // By default, in the imported data, we have the EDIT_OWN permission granted to ANYONE and jira-users
        removeEditOwnCommentsPermission("jira-users");

        assertAdminCanEditAnonymousComment(false);
        assertAdminCanEditAdminsComment(true);
        assertAdminCanEditFredsComment(false);
        logout();
        // JRA-16138 Now Anonymous Cannot Edit own Comment, as we don't really know who "owns" it.  
        assertAnonymousCanEditAnonymousComment(false);
        assertAnonymousCanEditAdminsComment(false);
        assertAnonymousCanEditFredsComment(false);
    }

    public void testEditAllPermissions()
    {
        // By default, in the imported data, we have the EDIT_ALL permission granted to jira-administrators
        removeEditAllCommentsPermission("jira-administrators");
        // By default, in the imported data, we have the EDIT_OWN permission granted to jira-users
        removeEditOwnCommentsPermission("jira-users");
        removeEditOwnCommentsPermission(""); // delete permission for anonymous

        assertAdminCanEditAnonymousComment(true);
        assertAdminCanEditAdminsComment(true);
        assertAdminCanEditFredsComment(true);
        logout();
        assertAnonymousCanEditAnonymousComment(true);
        assertAnonymousCanEditAdminsComment(true);
        assertAnonymousCanEditFredsComment(true);
    }

    public void testErrorCases()
    {
        // issue does not exist
        gotoPage("/secure/EditComment!default.jspa?id=123&commentId=10041");
        assertTextPresent("The issue no longer exists.");
        assertFormElementNotPresent(SAVE_BUTTON_NAME);

        // invalid comment id, issue exists
        final String INVALID_COMMENT_ID = "123";
        gotoPage("/secure/EditComment!default.jspa?id=10000&commentId=" + INVALID_COMMENT_ID);
        assertTextSequence(new String[]{"Error", "You do not have the permission for this comment."});

        // test no permission
        removeEditAllCommentsPermission("jira-administrators");
        removeEditAllCommentsPermission(""); // delete permission for anonymous
        removeEditOwnCommentsPermission("jira-users");
        removeEditOwnCommentsPermission(""); // delete permission for anonymous
        gotoPage("/secure/EditComment!default.jspa?id=10000&commentId=10041");

        tester.assertTitleEquals("Error - jWebTest JIRA installation");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "You do not have the permission for this comment.");
    }

    public void testEditCommentWithTooLongBody()
    {
        getBackdoor().advancedSettings().setTextFieldCharacterLengthLimit(10);
        navigation.issue().gotoIssue("HSP-1");
        clickLink("edit_comment_10031");
        setWorkingForm("comment-edit");
        assertTextPresent(OLD_COMMENT_ADMIN);
        setFormElement("comment", "too long comment");
        submit(SAVE_BUTTON_NAME);

        assertTextPresent("The entered text is too long. It exceeds the allowed limit of 10 characters.");
    }

    public void testEditCommentWithTextLengthLimitOn()
    {
        getBackdoor().advancedSettings().setTextFieldCharacterLengthLimit(10);
        navigation.issue().gotoIssue("HSP-1");
        clickLink("edit_comment_10031");
        setWorkingForm("comment-edit");
        assertTextPresent(OLD_COMMENT_ADMIN);
        final String correctComment = "AllGood";
        setFormElement("comment", correctComment);
        submit(SAVE_BUTTON_NAME);

        assertTextPresent("Edit comment issue summary for our test");
        assertTextPresent(correctComment);
        assertions.comments(Collections.singletonList(correctComment)).areVisibleTo("admin", "HSP-1");
    }


// --------------------------------------------------------------------------------------------------------------------

    private void assertAdminCanEditAnonymousComment(boolean expected)
    {
        verifyUserCanEditComment(expected, COMMENT_ID_ANON, NEW_COMMENT_ANON, true);
    }

    private void assertAdminCanEditAdminsComment(boolean expected)
    {
        verifyUserCanEditComment(expected, COMMENT_ID_ADMIN, NEW_COMMENT_ADMIN, true);
    }

    private void assertAdminCanEditFredsComment(boolean expected)
    {
        verifyUserCanEditComment(expected, COMMENT_ID_FRED, NEW_COMMENT_FRED, true);
    }

    private void assertAnonymousCanEditAnonymousComment(boolean expected)
    {
        verifyUserCanEditComment(expected, COMMENT_ID_ANON, NEW_COMMENT_ANON, false);
    }

    private void assertAnonymousCanEditAdminsComment(boolean expected)
    {
        verifyUserCanEditComment(expected, COMMENT_ID_ADMIN, NEW_COMMENT_ADMIN, false);
    }

    private void assertAnonymousCanEditFredsComment(boolean expected)
    {
        verifyUserCanEditComment(expected, COMMENT_ID_FRED, NEW_COMMENT_FRED, false);
    }

    private void verifyUserCanEditComment(boolean expected, String commentId, String commentBody, boolean userExists)
    {
        gotoPage("/secure/Dashboard.jspa");
        gotoIssue(ISSUE_KEY);
        if (expected)
        {
            verifyEditLinkExistsAndEdit(commentId, commentBody, userExists);
        }
        else
        {
            verifyEditLinkDoesNotExist(commentId);
            verifyEditCommentPagePrintsError(commentId);
            verifyCommentUpdatePrintsError(commentId);
        }
    }

    private void verifyEditLinkExistsAndEdit(String commentId, String newComment, boolean userExists)
    {
        final String commentLink = "edit_comment_" + commentId;

        // Verify that the link is present since the user can make the comment
        assertLinkPresent(commentLink);

        // lets make sure that we can edit the comment
        clickLink(commentLink);
        setWorkingForm("comment-edit");
        setFormElement("comment", newComment);
        if (userExists)
        {
            // anonymous user cannot set comment visibility level
            selectOption("commentLevel", "All Users");
        }
        submit(SAVE_BUTTON_NAME);

        assertTextPresent("Edit comment issue summary for our test");

        assertTextPresent(newComment);
    }

    private void verifyEditLinkDoesNotExist(String commentId)
    {
        gotoIssue(ISSUE_KEY);
        final String commentLink = "edit_comment_" + commentId;
        assertLinkNotPresent(commentLink);
    }

    private void verifyEditCommentPagePrintsError(String commentId)
    {
        // Now fake the go to edit comment page with all the params and make sure we get an error
        gotoPage(page.addXsrfToken("/secure/EditComment!default.jspa?id=10000&commentId=" + commentId));
        tester.assertTitleEquals("Error - jWebTest JIRA installation");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "You do not have the permission for this comment.");
    }

    private void verifyCommentUpdatePrintsError(String commentId)
    {
        // Now fake the submit with all the params and make sure we get an error
        gotoPage(page.addXsrfToken("/secure/EditComment.jspa?id=10000&commentId=" + commentId + "&comment=WooHoo"));
        tester.assertTitleEquals("Error - jWebTest JIRA installation");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "You do not have the permission for this comment.");
    }

    private void removeEditOwnCommentsPermission(String group)
    {
        goToDefaultPermissionScheme();
        clickLink("del_perm_" + EDIT_OWN_COMMENTS.permissionKey() + "_" + group);
        submit("Delete");
    }

    private void removeEditAllCommentsPermission(String group)
    {
        // Remove the permission to EDIT_ALL
        goToDefaultPermissionScheme();
        clickLink("del_perm_" + EDIT_ALL_COMMENTS.permissionKey() + "_" + group);
        submit("Delete");
    }

    private void addCreateCommentPermissionToAnonymous()
    {
        goToDefaultPermissionScheme();
        clickLink("add_perm_" + ADD_COMMENTS.permissionKey());
        checkCheckbox("type", "group");
        submit(" Add ");
    }

    private void goToDefaultPermissionScheme()
    {
        navigation.gotoAdmin();
        clickLink("permission_schemes");
        clickLinkWithText("Default Permission Scheme");
    }

}
