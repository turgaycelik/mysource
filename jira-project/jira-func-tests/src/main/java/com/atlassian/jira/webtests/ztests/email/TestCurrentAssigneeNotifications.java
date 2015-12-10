package com.atlassian.jira.webtests.ztests.email;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.RestRule;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.util.issue.IssueInlineEdit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestCurrentAssigneeNotifications extends EmailFuncTestCase
{
    @Override
    public void setUpTest()
    {
        super.setUpTest();
        restoreDataAndConfigureSmtp("TestCurrentAssigneeNotifications.xml");
        backdoor.darkFeatures().enableForSite("no.frother.assignee.field");
    }

    @Override
    public void tearDownTest()
    {
        backdoor.darkFeatures().disableForSite("no.frother.assignee.field");
        super.tearDownTest();
    }

    public void testAssignIssueSendsEmailToNewAndOldAssignees() throws Exception
    {
        navigation.issue().assignIssue("HSP-1", "This has been re-assigned", "homer");
        assertHomerAndBartsEmails(1, 1);
    }

    public void testAssignIssueTriggersIssueAssignedEvent() throws Exception
    {
        navigation.issue().assignIssue("HSP-1", "This has been re-assigned", "homer");

        assertEmailSentToUserListeningOnlyToIssueAssignedEvents();
    }

    public void testEditIssueChangingAssigneeSendsEmailToNewAndOldAssignees() throws Exception
    {
        editIssueAndChangeAssignee("HSP-1", "This has been re-assigned", "homer");
        assertHomerAndBartsEmails(1, 1);
    }

    public void testEditIssueChangingAssigneeTriggersIssueAssignedEvent() throws Exception
    {
        editIssueAndChangeAssignee("HSP-1", "This has been re-assigned", "homer");

        assertEmailSentToUserListeningOnlyToIssueAssignedEvents();
    }

    public void testTransitionIssueChangingAssigneeSendsEmailToNewAndOldAssignees() throws Exception
    {
        workflowIssueAndChangeAssignee("HSP-1", "homer");
        assertHomerAndBartsEmails(1, 1);
    }

    public void testTransitionIssueChangingAssigneeTriggersIssueAssignedEvent() throws Exception
    {
        workflowIssueAndChangeAssignee("HSP-1", "homer");

        assertEmailSentToUserListeningOnlyToIssueAssignedEvents();
    }

    public void testBulkEditIssueChangingAssigneeSendsEmailToNewAndOldAssignees() throws Exception
    {
        bulkEditHSP_1("homer");
        assertHomerAndBartsEmails(1, 1);
    }

    public void testBulkEditIssueChangingAssigneeTriggersIssueAssignedEvent() throws Exception
    {
        bulkEditHSP_1("homer");

        assertEmailSentToUserListeningOnlyToIssueAssignedEvents();
    }

    public void testBulkTransitionIssueChangingAssigneeSendsEmailToNewAndOldAssignees() throws Exception
    {
        bulkTransitionHSP_1("homer");
        assertHomerAndBartsEmails(1, 1);
    }

    public void testBulkTransitionIssueChangingAssigneeTriggersIssueAssignedEvent() throws Exception
    {
        bulkTransitionHSP_1("homer");

        assertEmailSentToUserListeningOnlyToIssueAssignedEvents();
    }

    public void testInlineEditingAssigneeSendsEmailToNewAndOldAssignees() throws Exception
    {
        inlineAssign("HSP-1", "10000", "homer");

        assertHomerAndBartsEmails(1, 1);
    }

    public void testInlineEditingAssigneeTriggersIssueAssignedEvent() throws Exception
    {
        inlineAssign("HSP-1", "10000", "homer");

        assertEmailSentToUserListeningOnlyToIssueAssignedEvents();
    }

    private void inlineAssign(String issueKey, String issueId, String newAssignee) throws Exception
    {
        navigation.issue().gotoIssue(issueKey);

        IssueInlineEdit inlineEdit = new IssueInlineEdit(locator, tester, new RestRule(this));
        inlineEdit.inlineEditField(issueId, "assignee", newAssignee);
    }

    private void bulkEditHSP_1(final String newAssigneeName)
    {
        // Click Link 'find issues' (id='find_link').
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.edit.operation.name");
        tester.submit("Next");
        tester.checkCheckbox("actions", "assignee");
        // Select 'Administrator' from select box 'assignee'.
        tester.selectOption("assignee", newAssigneeName);
        tester.checkCheckbox("sendBulkNotification", "true");
        tester.submit("Next");
        tester.submit("Confirm");

        waitAndReloadBulkOperationProgressPage();
    }

    private void bulkTransitionHSP_1(String newAssigneeName)
    {
        // Click Link 'find issues' (id='find_link').
        navigation.issueNavigator().displayAllIssues();
        // Click Link 'all 1 issue(s)' (id='bulkedit_all').
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.submit("Next");
        tester.checkCheckbox("wftransition", "jira_5_5");
        tester.submit("Next");
        // Select 'Won't Fix' from select box 'resolution'.
        tester.checkCheckbox("actions", "resolution");
        tester.selectOption("resolution", "Won't Fix");

        tester.checkCheckbox("actions", "assignee");
        tester.selectOption("assignee", newAssigneeName);
        tester.checkCheckbox("sendBulkNotification", "true");
        tester.submit("Next");
        tester.submit("Next");

        waitAndReloadBulkOperationProgressPage();
    }

    private void editIssueAndChangeAssignee(final String issueKey, final String commentStr, final String newAssigneeName)
    {
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("edit-issue");

        tester.selectOption("assignee", newAssigneeName);
        tester.setFormElement("comment", commentStr);
        tester.submit("Update");
    }

    private void workflowIssueAndChangeAssignee(final String issueKey, final String newAssigneeName)
    {
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("action_id_5");
        tester.setWorkingForm("issue-workflow-transition");
        tester.selectOption("resolution", "Won't Fix");
        tester.selectOption("assignee", newAssigneeName);
        tester.submit("Transition");
    }


    private void assertHomerAndBartsEmails(int expectedHomerEmailCount, int expectedBartEmailCount)
            throws MessagingException, InterruptedException, IOException
    {
        flushMailQueueAndWait(expectedHomerEmailCount + expectedBartEmailCount);
        assertHomerWasAssignedEmails("homer@localhost", expectedHomerEmailCount);
        assertHomerWasAssignedEmails("bart@localhost", expectedBartEmailCount);
    }

    private void assertHomerWasAssignedEmails(String emailAddress, int expectedEmailCount)
            throws MessagingException, IOException
    {
        List messagesForRecipient = getMessagesForRecipient(emailAddress);
        assertEquals(expectedEmailCount, messagesForRecipient.size());

        for (Object msg : messagesForRecipient)
        {
            final MimeMessage message = (MimeMessage) msg;
            final String subject = message.getSubject();
            assertTrue(subject.contains("HSP-1"));
            assertEmailBodyContains(message, "HSP-1");
            assertEmailBodyContains(message, "Assignee:");
            assertEmailBodyContainsLine(message, ".*diffremovedchars.*bart.*");
            assertEmailBodyContainsLine(message, ".*diffaddedchars.*homer.*");
        }
    }

    private void restoreDataAndConfigureSmtp(final String fileName)
    {
        administration.restoreData(fileName);
        configureAndStartSmtpServer();
    }

    private void assertEmailSentToUserListeningOnlyToIssueAssignedEvents() throws Exception
    {
        flushMailQueueAndWait(1);

        List<MimeMessage> messagesForRecipient = getMessagesForRecipient("test@test.com");
        assertThat(messagesForRecipient.size(), is(1));
    }
}
