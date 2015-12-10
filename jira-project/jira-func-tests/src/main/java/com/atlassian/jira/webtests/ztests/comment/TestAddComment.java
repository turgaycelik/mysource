package com.atlassian.jira.webtests.ztests.comment;


import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.webtests.Groups;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;


import static com.atlassian.jira.permission.ProjectPermissions.ADD_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;
import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;

public abstract class TestAddComment extends FuncTestCase
{
    private static final String smartQuoteOpen = "\u201C";
    private static final String smartQuoteClose = "\u201D";
    private static final String htmlEscQuote = "&quot;";
    private static final String chineseChars = "\u963F\u725B\u54E5"; // UTF code for 3 chinese characters
    private static final String fiveHTMLquotes = htmlEscQuote + htmlEscQuote + htmlEscQuote + htmlEscQuote + htmlEscQuote;
    private static final String TEST_ADD_COMMENT_XML = "TestAddComment.xml";

    private final String commentLinkid;

    TestAddComment(String commentLinkid)
    {
        this.commentLinkid = commentLinkid;
    }

    public void testRemoveMe() throws Exception
    {
        //This is just here to ensure there is a test a test bamboo does not report and error.
    }

    //    Needs to be moved to selenium see: JRADEV-1844
    @Override
    public void setUpTest()
    {
        administration.restoreBlankInstance();
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    public void testCannotAddCommentWithoutIssue()
    {
        navigation.gotoPage(page.addXsrfToken("/secure/AddComment.jspa"));
        tester.assertTitleEquals("Error - jWebTest JIRA installation");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "The issue no longer exists.");
    }

//    /**
//     * Test that the Smart quotes are escaped properly - JRA-4330
//     */
//   Commenting out with the filter that does this - tentative
//    public void testSmartQuoteRemoval()
//    {
//        navigation.issue().gotoIssue(addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "summary"));
//        tester.clickLink("comment-issue");
//        tester.setFormElement("comment", smartQuoteOpen + chineseChars + smartQuoteClose);
//        tester.submit();
//        tester.assertTextNotPresent(fiveHTMLquotes);
//        tester.assertTextPresent(htmlEscQuote);
////        tester.assertTextPresent(htmlEscQuote + "???" + htmlEscQuote); // commented out as it *shouldn't* be '???'
//        tester.assertTextPresentAfterText(htmlEscQuote, htmlEscQuote); //check that there are two quotes after each other
//    }

    public void testCommentVisiblityOrdering()
    {
        administration.restoreData(TEST_ADD_COMMENT_XML);
        final String FIRST_GROUP_NAME = "a group";
        final String LAST_GROUP_NAME = "z group";
        administration.usersAndGroups().addGroup(FIRST_GROUP_NAME);
        administration.usersAndGroups().addGroup(LAST_GROUP_NAME);
        administration.usersAndGroups().addUserToGroup(ADMIN_USERNAME, FIRST_GROUP_NAME);
        administration.usersAndGroups().addUserToGroup(ADMIN_USERNAME, LAST_GROUP_NAME);
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLink(commentLinkid);
        tester.setWorkingForm("comment-add");
        String[] commentLevels = tester.getDialog().getOptionsFor("commentLevel");
        assertEquals(9, commentLevels.length);
        assertEquals("All Users", commentLevels[0]);
        assertEquals("Administrators", commentLevels[1]);
        assertEquals("Developers", commentLevels[2]);
        assertEquals("Users", commentLevels[3]);
        assertEquals(FIRST_GROUP_NAME, commentLevels[4]);
        assertEquals("jira-administrators", commentLevels[5]);
        assertEquals("jira-developers", commentLevels[6]);
        assertEquals("jira-users", commentLevels[7]);
        assertEquals(LAST_GROUP_NAME, commentLevels[8]);
    }

    public void testAddInvalidComment()
    {
        administration.restoreData(TEST_ADD_COMMENT_XML);
        navigation.issue().gotoIssue("HSP-1");

        // empty is not ok for AddComment action
        addComment("All Users", "");
        tester.assertTextPresent("Comment body can not be empty!");

        // all spaces on the other hand is not considered kosher!
        navigation.issue().gotoIssue("HSP-1");
        addComment("All Users", "     ");
        tester.assertTextPresent("Comment body can not be empty!");
    }

    public void testAddCommentWithVisibility()
    {
        String allUsersComment = "This is a comment assigned to all users";
        String jiraUsersGroupComment = "this comment visible to jira-users group";
        String jiraUsersRoleComment = "this is a comment visible to Users role"; //role Users
        String jiraDevelopersGroupComment = "this is a comment visible to jira-developers group";
        String jiraDevelopersRoleComment = "this is a comment visible to Developers role"; //role Developers
        String jiraAdminsGroupComment = "this is a comment visible to jira-admin group";
        String jiraAdminsRoleComment = "this is a comment visible to Administrators role"; //Administrators role

        administration.restoreData("TestBlankInstancePlusAFewUsers.xml");

        navigation.issue().gotoIssue("HSP-5");

        //create comment visible to all users
        addComment("All Users", allUsersComment);

        //create comments visible to all jira users
        addComment("jira-users", jiraUsersGroupComment);
        addComment("Users", jiraUsersRoleComment);

        //create comments visible to jira developers
        addComment("jira-developers", jiraDevelopersGroupComment);
        addComment("Developers", jiraDevelopersRoleComment);

        //create comments visible to all admins
        addComment("jira-administrators", jiraAdminsGroupComment);
        addComment("Administrators", jiraAdminsRoleComment);

        List<String> userComments = asList(allUsersComment, jiraUsersRoleComment, jiraUsersGroupComment);
        List<String> devComments = asList(jiraDevelopersGroupComment, jiraDevelopersRoleComment);
        List<String> adminComments = asList(jiraAdminsGroupComment, jiraAdminsRoleComment);

        // verify that Fred can see general comment but not others as he is not in the visibility groups
        checkCommentVisibility(FRED_USERNAME, "HSP-5", userComments, concat(devComments, adminComments));

        // verify that Admin can see all comments as he is not in all visibility groups
        //list userComments now becomes all comments as we have added two lists together
        checkCommentVisibility(ADMIN_USERNAME, "HSP-5", concat(concat(userComments, devComments), adminComments), null);

        //verify that developer only user can only view developer comments
        checkCommentVisibility("devman", "HSP-5", concat(devComments, userComments), adminComments);

        //veryify that onlyadmin guy can only see admin stuff
        checkCommentVisibility("onlyadmin", "HSP-5", concat(adminComments, userComments), devComments);
    }

    private void checkCommentVisibility(String username, String issueKey, Iterable<String> expectedVisible, Iterable<String> expectedNotVisible)
    {
        if (expectedVisible != null)
        {
            assertions.comments(expectedVisible).areVisibleTo(username, issueKey);
        }
        if (expectedNotVisible != null)
        {
            assertions.comments(expectedNotVisible).areNotVisibleTo(username, issueKey);
        }
    }

    public void testAddCommentErrorWhenLoggedOut()
    {
        administration.restoreData(TEST_ADD_COMMENT_XML);
        navigation.logout();
        String theComment = "comment with html <input type=\"input\" id=\"invalid\"/>";
        String theCommentEscaped = "comment with html &lt;input type=&quot;input&quot; id=&quot;invalid&quot;/&gt;";
        page.getFreshXsrfToken();
        navigation.gotoPage(page.addXsrfToken("/secure/AddComment.jspa?id=10000&comment=" + theComment));
        tester.assertTextPresent(
                "It seems that you have tried to perform an operation which you are not permitted to perform.");
    }

    public void testAddCommentErrorWhenNoPermission()
    {
        administration.restoreData(TEST_ADD_COMMENT_XML);
        administration.permissionSchemes().defaultScheme().removePermission(COMMENT_ISSUE, Groups.USERS);
        String theComment = "comment with html <input type=\"input\" id=\"invalid\"/>";
        String theCommentEscaped = "comment with html &lt;input type=&quot;input&quot; id=&quot;invalid&quot;/&gt;";
        navigation.gotoPage(page.addXsrfToken("/secure/AddComment.jspa?id=10000&comment=" + theComment));
        tester.assertTextPresent(ADMIN_FULLNAME + ", you do not have the permission to comment on this issue.");
    }

    public void testAddCommentErrorWhenIssueDoesNotExist()
    {
        administration.restoreData(TEST_ADD_COMMENT_XML);
        navigation.issue().deleteIssue("HSP-1");
        String theComment = "comment with html <input type=\"input\" id=\"invalid\"/>";
        String theCommentEscaped = "comment with html &lt;input type=&quot;input&quot; id=&quot;invalid&quot;/&gt;";
        navigation.gotoPage(page.addXsrfToken("/secure/AddComment.jspa?id=10000&comment=" + theComment));
        tester.assertTextPresent("The issue no longer exists.");
        tester.assertTextNotPresent(theComment); //should have been escaped
        tester.assertTextPresent("The issue no longer exists.");
    }

    private void addComment(String visibleTo, String comment)
    {
        tester.clickLink(commentLinkid);
        tester.selectOption("commentLevel", visibleTo);
        tester.setFormElement("comment", comment);
        tester.submit();
    }

    public void testAddCommentWithGroupButNotLoggedIn() throws Exception
    {
        String key = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "Test Issue");
        navigation.issue().gotoIssue(key);
        tester.assertTextPresent("Test Issue");

        final String id = navigation.issue().getId(key);

        navigation.gotoPage(page.addXsrfToken("/secure/AddComment.jspa?id=" + id + "&comment=Hello"));
        tester.assertTextPresent("Test Issue");
        tester.assertTextPresent("Hello");
        tester.assertTextNotPresent("Ahoj");

        navigation.gotoPage(page.addXsrfToken(
                "/secure/AddComment.jspa?id=" + id + "&comment=Ahoj&commentLevel=group%3Ajira-users"));
        tester.assertTextPresent("Test Issue");
        tester.assertTextPresent("Hello");
        tester.assertTextPresent("Ahoj");

        /// Make HSP project visible to anonymous users
        addBrowseProjectPermissionToAnonymous();
        addCreateCommentPermissionToAnonymous();
        administration.timeTracking().enable(TimeTracking.Mode.MODERN);
        navigation.logout();

        page.getFreshXsrfToken();
        navigation.gotoPage(page.addXsrfToken(
                "/secure/AddComment.jspa?id=" + id + "&comment=Hola&commentLevel=group%3Ajira-users"));
        tester.assertTextNotPresent("NullPointerException");
        tester.assertTextPresent(
                "You cannot add a comment for specific groups or roles, as your session has expired. Please log in and try again.");

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testAddCommentCheckUpdatedDate() throws ParseException
    {
        administration.restoreData(TEST_ADD_COMMENT_XML);
        navigation.issue().gotoIssue("HSP-1");
        final String startTagNoQuote = "<span class=date>";
        text.assertTextPresent(new IdLocator(tester, "create-date"), "14/Aug/06 4:26 PM");
        text.assertTextPresent(new IdLocator(tester, "updated-date"), "14/Aug/06 4:26 PM");
        final String commentText = "This is my first test comment!";
        tester.assertTextNotPresent(commentText);

        tester.clickLink(commentLinkid);
        tester.setFormElement("comment", commentText);
        tester.submit();

        tester.assertTextPresent(commentText);

        //now check the updated time is the same as the one of the comment (kinda, since the updated time is pretty formatted while the comments is not, but we can assert the time)
        String dateString = locator.css("span.date").getNodes()[0].getNodeValue();
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yy h:mm a");
        final Date date = format.parse(dateString);

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        String timeString = timeFormat.format(date);
        text.assertTextPresent(new IdLocator(tester, "create-date"), "14/Aug/06 4:26 PM");
        text.assertTextPresent(new IdLocator(tester, "updated-date"), timeString);
    }

    public void testAddTooLongComment()
    {
        administration.restoreData(TEST_ADD_COMMENT_XML);
        backdoor.advancedSettings().setTextFieldCharacterLengthLimit(10);
        navigation.issue().gotoIssue("HSP-1");

        addComment("All Users", "This is too long comment");
        tester.assertTextPresent("The entered text is too long. It exceeds the allowed limit of 10 characters.");
    }

    public void testAddCommentWithTextLengthLimitOn()
    {
        administration.restoreData(TEST_ADD_COMMENT_XML);
        backdoor.advancedSettings().setTextFieldCharacterLengthLimit(10);
        navigation.issue().gotoIssue("HSP-1");

        final String correctCommentBody = "AllGood";
        addComment("All Users", correctCommentBody);
        assertions.comments(Collections.singletonList(correctCommentBody)).areVisibleTo("admin", "HSP-1");
    }

    private void addBrowseProjectPermissionToAnonymous()
    {
        goToDefaultPermissionScheme();
        tester.clickLink("add_perm_" + BROWSE_PROJECTS.permissionKey());
        tester.checkCheckbox("type", "group");
        tester.submit(" Add ");
    }

    private void addCreateCommentPermissionToAnonymous()
    {
        goToDefaultPermissionScheme();
        tester.clickLink("add_perm_" + ADD_COMMENTS.permissionKey());
        tester.checkCheckbox("type", "group");
        tester.submit(" Add ");
    }

    private void goToDefaultPermissionScheme()
    {
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLinkWithText("Default Permission Scheme");
    }
}
