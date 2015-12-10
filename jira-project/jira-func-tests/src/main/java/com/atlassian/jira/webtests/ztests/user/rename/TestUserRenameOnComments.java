package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;

import static com.atlassian.jira.webtests.Groups.DEVELOPERS;

/**
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.PROJECTS})
public class TestUserRenameOnComments extends FuncTestCase
{

    public static final String ALL_USERS = "All Users";
    public static final String ISSUE_FOR_COMMENT = "COW-4";

    @Override
    public void setUpTest()
    {
        administration.restoreData("user_rename.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        //    KEY       USERNAME    NAME
        //    bb	    betty	    Betty Boop
        //    ID10001	bb	        Bob Belcher
        //    cc	    cat	        Crazy Cat
        //    ID10101	cc	        Candy Chaos
    }

    public void testRenamedUserRetainsCommentOwnership()
    {
        renameUser("cc", "sweetsmayhem");

        // cat, a developer, adds a comment
        navigation.login("cat");
        navigation.issue().gotoIssue(ISSUE_FOR_COMMENT);
        addComment(ALL_USERS, "Cat's comment");

        // sweets, a user, adds a comment to the same issue
        navigation.login("sweetsmayhem", "cc");
        navigation.issue().gotoIssue(ISSUE_FOR_COMMENT);
        addComment(ALL_USERS, "My very own comment");

        // sweets is renamed to cc
        navigation.login("admin");
        renameUser("sweetsmayhem", "cc");

        // cc can edit her own comment, but not cat's (whose original name she has taken)
        navigation.login("cc");
        navigation.issue().gotoIssue(ISSUE_FOR_COMMENT);
        assertCannotEdit(10300);
        editComment(10301, "I may do as I please with my own comment");
        text.assertTextPresent("I may do as I please with my own comment");
        assertCannotDelete(10300);
        deleteComment(10301);
        text.assertTextNotPresent("I may do as I please with my own comment");
    }

    public void testRenamedUserCorrectlyDisplayedAsCommentAuthorAndEditor()
    {
        backdoor.permissionSchemes().replaceGroupPermissions(0L, COMMENT_EDIT_ALL, DEVELOPERS);
        navigation.login("betty");
        navigation.issue().gotoIssue(ISSUE_FOR_COMMENT);
        addComment(ALL_USERS, "Comment by a renamed user");
        navigation.login("bb");
        navigation.issue().gotoIssue(ISSUE_FOR_COMMENT);
        addComment(ALL_USERS, "Comment by a recycled user");
        editComment(10300, "Bob Belcher edited this comment");
        navigation.login("admin");
        renameUser("betty", "bboop");
        navigation.issue().gotoIssue(ISSUE_FOR_COMMENT);
        assertCommenterDisplayNameIs(10300, "Betty Boop");
        assertEditorDisplayNameIs(10300, "Bob Belcher");
    }

    public void testRenamedUserRetainsCommentingAndViewingPermissions()
    {
        // alter permissions so that only developers can comment
        backdoor.permissionSchemes().replaceGroupPermissions(0L, COMMENT_ISSUE, DEVELOPERS);

        // cat, a developer, should be able to comment
        navigation.login("cat");
        navigation.issue().gotoIssue(ISSUE_FOR_COMMENT);
        addComment("Developers", "Crazy Cat can comment");
        text.assertTextPresent("Crazy Cat can comment");

        // cc, a recycled user, has cat's old name, but should not be allowed to comment
        navigation.login("cc");
        assertCannotComment();
        navigation.login("admin");
        renameUser("cat", "feline");
        renameUser("cc", "cat");

        // cat should be able to comment even after being renamed
        navigation.login("feline", "cat");
        navigation.issue().gotoIssue(ISSUE_FOR_COMMENT);
        addComment("Developers", "Comment with new name");
        text.assertTextPresent("Comment with new name");

        navigation.login("cat", "cc");
        navigation.issue().gotoIssue(ISSUE_FOR_COMMENT);
        assertCannotComment();
    }

    public void testRenamedUserIdentifiedAsCommenterAndUpdaterInCommentRESTResource()
    {
        navigation.login("cat");
        navigation.issue().gotoIssue(ISSUE_FOR_COMMENT);
        addComment(ALL_USERS, "cat commented");
        Issue cow2Representation = backdoor.issues().getIssue(ISSUE_FOR_COMMENT);
        assertEquals("cat", cow2Representation.getComments().iterator().next().author.name);

        navigation.login("admin");
        renameUser("cc", "candy");
        renameUser("cat","crazy");
        cow2Representation = backdoor.issues().getIssue(ISSUE_FOR_COMMENT);
        assertEquals("crazy", cow2Representation.getComments().iterator().next().author.name);
    }

    private void assertCannotDelete(long commentId)
    {
        Locator deleteButtonLocator = locator.id("delete_comment_" + commentId);
        assertFalse("User should not be allowed to delete this comment", deleteButtonLocator.exists());
    }

    private void deleteComment(long commentId)
    {
        tester.clickLink("delete_comment_" + commentId);
        tester.submit("Delete");
    }

    private void editComment(long commentId, String newContent)
    {
        tester.clickLink("edit_comment_" + commentId);
        tester.setWorkingForm("comment-edit");
        tester.setFormElement("comment", newContent);
        tester.submit("Save");
    }

    private void assertCannotEdit(long commentId)
    {
        Locator editButtonLocator = locator.id("edit_comment_" + commentId);
        assertFalse("User should not be allowed to edit this comment", editButtonLocator.exists());
    }

    private void assertCannotComment()
    {
        assertFalse(locator.id("comment-issue").exists());
    }

    private void assertEditorDisplayNameIs(long commentId, String displayName)
    {
        // the editor's display name is hidden away in a title attribute
        Locator commentLocator = locator.css(String.format("#comment-%d .action-details .redText", commentId));
        commentLocator.getNode().getAttributes();
        String editedNodeTitle = commentLocator.getNode().getAttributes().getNamedItem("title").getNodeValue();
        assertTrue("The comment update author name was not as expected.", editedNodeTitle.startsWith(displayName));
    }

    private void assertCommenterDisplayNameIs(long commentId, String displayName)
    {
        Locator commentLocator = locator.id(String.format("commentauthor_%d_verbose", commentId));
        text.assertTextPresent(commentLocator, displayName);
    }

    private void addComment(String visibleTo, String comment)
    {
        tester.clickLink("footer-comment-button");
        form.selectOption("commentLevel", visibleTo);
        tester.setFormElement("comment", comment);
        tester.submit();
    }

    private void renameUser(String from, String to)
    {
        navigation.gotoPage(String.format("secure/admin/user/EditUser!default.jspa?editName=%s", from));
        tester.setFormElement("username", to);
        tester.submit("Update");
    }
}
