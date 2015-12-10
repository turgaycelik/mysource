package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.fields.AssigneeField;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.dialogs.quickedit.EditIssueDialog;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.jira.pageobjects.pages.viewissue.AssignIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.people.PeopleSection;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for editing an issue using quick edit
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@ResetData
public class TestFrotherisedAssignee extends BaseJiraWebTest
{

    private String issueKey;

    @Before
    public void setUp() throws Exception
    {
        issueKey = backdoor.issues().createIssue(10000, "First test issue").key();
        backdoor.usersAndGroups().addUserToGroup("fred", "jira-developers");
        backdoor.usersAndGroups().addUser("bob");
    }

    // https://jdog.atlassian.net/browse/FLAKY-44
//    @Test
//    public void testAssigneePicker()
//    {
//        ViewIssuePage issuePage = jira.quickLoginAsSysadmin(ViewIssuePage.class, issueKey);
//        PeopleSection peopleSection = issuePage.getPeopleSection();
//        assertEquals("Administrator", peopleSection.getAssignee());
//
//        AssignIssueDialog assignDialog = issuePage.assignIssueViaKeyboardShortcut();
//
//        assertTrue(assignDialog.isOpen());
//        assertTrue(assignDialog.isAutoComplete());
//
//        assignDialog.setAssignee("fred").submit();
//
//        issuePage = pageBinder.bind(ViewIssuePage.class, issueKey);
//        assertEquals("Fred Normal", issuePage.getPeopleSection().getAssignee());
//
//        //try a user that doesn't have the assign issues permission
//        assignDialog = issuePage.assignIssueViaKeyboardShortcut();
//        assertTrue(assignDialog.isOpen());
//        assertTrue(assignDialog.isAutoComplete());
//
//        try
//        {
//            assignDialog.setAssignee("bob");
//            fail("Should have thrown exception trying to select bob. Doesn't have assign issue permission.");
//        }
//        catch (Exception e)
//        {
//            //yay!
//        }
//    }

    @Test
    public void testAssigneeFieldOnEditForm()
    {
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, issueKey);
        final EditIssueDialog editIssueDialog = issuePage.editIssueViaKeyboardShortcut();

        final AssigneeField assigneeField = pageBinder.bind(AssigneeField.class);
        assertTrue(assigneeField.isAutocomplete());

        assertEquals("Administrator", assigneeField.getAssignee());
        assigneeField.setAssignee("fred");

        issuePage = editIssueDialog.submitExpectingViewIssue(issueKey);

        assertEquals("Fred Normal", issuePage.getPeopleSection().getAssignee());
    }

    @Test
    public void testBlurringSelectsAutomatic()
    {
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, issueKey);
        PeopleSection peopleSection = issuePage.getPeopleSection();
        assertEquals("Administrator", peopleSection.getAssignee());

        AssignIssueDialog assignDialog = issuePage.assignIssueViaKeyboardShortcut();
        assignDialog.setAssignee("fred").submit();

        issuePage = jira.goTo(ViewIssuePage.class, issueKey);
        peopleSection = issuePage.getPeopleSection();
        assertEquals("Fred Normal", peopleSection.getAssignee());

        assignDialog = issuePage.assignIssueViaKeyboardShortcut();

        assertTrue(assignDialog.isOpen().now());

        assignDialog.typeAssignee("blah")
                .typeAssignee("")
                .typeAssignee("blahblah")
                .typeAssignee("");

        assignDialog.submit();

        issuePage = jira.goTo(ViewIssuePage.class, issueKey);
        peopleSection = issuePage.getPeopleSection();
        assertEquals("Administrator", peopleSection.getAssignee());
    }

    @Test
    public void testAssigneeFieldOnCreateForm()
    {
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, issueKey);
        issuePage.execKeyboardShortcut("c");

        final CreateIssueDialog createIssueDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE)
                .switchToFullMode()
                .fill("summary", "test");

        AssigneeField assigneeField = pageBinder.bind(AssigneeField.class);
        assertTrue(assigneeField.isAutocomplete());

        assigneeField.setAssignee("fred");

        createIssueDialog.submit(GlobalMessage.class);
        issuePage = jira.goTo(ViewIssuePage.class, "HSP-2");
        assertEquals("Fred Normal", issuePage.getPeopleSection().getAssignee());


        //no lets test the assigned to me link
        final EditIssueDialog editIssueDialog = issuePage.editIssueViaKeyboardShortcut();

        editIssueDialog.switchToFullMode();

        assigneeField = pageBinder.bind(AssigneeField.class);
        assigneeField.assignToMe();

        issuePage = editIssueDialog.submitExpectingViewIssue("HSP-2");
        assertEquals("Administrator", issuePage.getPeopleSection().getAssignee());
    }

    @Test
    public void testAssignToMeHiddenifAssigneeisMe()
    {
        final ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, issueKey);
        final EditIssueDialog editIssueDialog = issuePage.editIssueViaKeyboardShortcut();
        editIssueDialog.switchToFullMode();
        final AssigneeField assigneeField = pageBinder.bind(AssigneeField.class);
        assertFalse("Expected assign to me to be hidden as assignee is current user", assigneeField.hasAssignToMe());
    }

}
