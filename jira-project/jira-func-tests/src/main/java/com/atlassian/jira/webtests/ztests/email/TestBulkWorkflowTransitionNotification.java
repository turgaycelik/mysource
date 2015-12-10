package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.EMAIL, Category.WORKFLOW })
public class TestBulkWorkflowTransitionNotification extends EmailFuncTestCase
{
    private static final String COMMENT_TEXT = "This comment should appear in the email body.";
    private static final String WORKFLOW_RESOLVE = "jira_5_5";
    private static final String WORKFLOW_REOPEN = "jira_3_4";

    public void setUpTest()
    {
        super.setUpTest();

        // Notes about data:
        //  * no custom workflows
        //  * Default notification scheme enabled for Test project
        //  * devman & fred are watchers on all issues, so they should receive notifications
        //  * 1 issue unresolved, 1 resolved
        //  * jira-developers in "Administrators" role, jira-users in "Developers" role, jira-administrators in "Users" role
        //  * comment visibility options: Groups & Project Roles
        administration.restoreData("TestBulkWorkflowTransitionNotification.xml");

        configureAndStartSmtpServerWithNotify();
    }

    public void testResolvedCommentAppearsInEmail() throws MessagingException, InterruptedException, IOException
    {
        _testCommentAppearsInEmail("10000", "TST-1", WORKFLOW_RESOLVE, "Resolved", "Fixed", "[JIRATEST] (TST-1) First Issue", null);
    }

    public void testReopenedCommentAppearsInEmail() throws MessagingException, InterruptedException, IOException
    {
        _testCommentAppearsInEmail("10001", "TST-2", WORKFLOW_REOPEN, "Reopened", null, "[JIRATEST] (TST-2) Second Issue", null);
    }

    public void testCommentVisibilityInEmailForJiraAdmins() throws MessagingException, InterruptedException, IOException
    {
        final String commentLevel = "jira-administrators";
        _performBulkOperation("10000", WORKFLOW_RESOLVE, "Fixed", commentLevel);
        assertCommentVisibility(EasyMap.build("admin@example.com", Boolean.TRUE, "devman@example.com", Boolean.FALSE, "fred@example.com", Boolean.FALSE));
    }

    public void testCommentVisibilityInEmailForJiraDevs() throws MessagingException, InterruptedException, IOException
    {
        final String commentLevel = "jira-developers";
        _performBulkOperation("10000", WORKFLOW_RESOLVE, "Fixed", commentLevel);
        assertCommentVisibility(EasyMap.build("admin@example.com", Boolean.TRUE, "devman@example.com", Boolean.TRUE, "fred@example.com", Boolean.FALSE));
    }

    public void testCommentVisibilityInEmailForJiraUsers() throws MessagingException, InterruptedException, IOException
    {
        final String commentLevel = "jira-users";
        _performBulkOperation("10000", WORKFLOW_RESOLVE, "Fixed", commentLevel);
        assertCommentVisibility(EasyMap.build("admin@example.com", Boolean.TRUE, "devman@example.com", Boolean.TRUE, "fred@example.com", Boolean.TRUE));
    }

    public void testCommentVisibilityInEmailForProjectAdmins()
            throws MessagingException, InterruptedException, IOException
    {
        final String commentLevel = ADMIN_FULLNAME + "s";
        _performBulkOperation("10000", WORKFLOW_RESOLVE, "Fixed", commentLevel);
        assertCommentVisibility(EasyMap.build("admin@example.com", Boolean.TRUE, "devman@example.com", Boolean.TRUE, "fred@example.com", Boolean.FALSE));
    }

    public void testCommentVisibilityInEmailForProjectDevs()
            throws MessagingException, InterruptedException, IOException
    {
        final String commentLevel = "Developers";
        _performBulkOperation("10000", WORKFLOW_RESOLVE, "Fixed", commentLevel);
        assertCommentVisibility(EasyMap.build("admin@example.com", Boolean.TRUE, "devman@example.com", Boolean.TRUE, "fred@example.com", Boolean.TRUE));
    }

    public void testCommentVisibilityInEmailForProjectUsers()
            throws MessagingException, InterruptedException, IOException
    {
        final String commentLevel = "Users";
        _performBulkOperation("10000", WORKFLOW_RESOLVE, "Fixed", commentLevel);
        assertCommentVisibility(EasyMap.build("admin@example.com", Boolean.TRUE, "devman@example.com", Boolean.FALSE, "fred@example.com", Boolean.FALSE));
    }

    private void _testCommentAppearsInEmail(String chkId, String issueId, String workflow, String workflowName, String resolution, String subject, String commentLevel)
            throws InterruptedException, MessagingException, IOException
    {
        _performBulkOperation(chkId, workflow, resolution, commentLevel);

        // check the full details of admin's email
        List messagesForAdmin = getMessagesForRecipient("admin@example.com");
        assertTrue(messagesForAdmin.size() == 1);
        assertMailProperties((MimeMessage) messagesForAdmin.get(0), subject, COMMENT_TEXT, workflowName);

        // check only comment visibility for all users' emails
        assertCommentVisibility(EasyMap.build("admin@example.com", Boolean.TRUE, "devman@example.com", Boolean.TRUE, "fred@example.com", Boolean.TRUE));

        // also test that comments aren't created twice (however, comment appears twice because of open and closed divs)
        navigation.issue().viewIssue(issueId);
        assertions.getTextAssertions().assertTextPresentNumOccurences(new WebPageLocator(tester), COMMENT_TEXT, 2);
    }

    private void assertMailProperties(MimeMessage message, String subject, String comment, String workflowName)
            throws MessagingException, IOException
    {
        assertEmailSubjectEquals(message, subject);
        assertEmailFromEquals(message, "\"" + ADMIN_FULLNAME + " (JIRA)\" <jiratest@atlassian.com>");
        assertEmailBodyContains(message, comment);
    }

    private void assertCommentVisibility(Map visibility) throws MessagingException, InterruptedException, IOException
    {
        for (final Object o : visibility.keySet())
        {
            String recipient = (String) o;
            Boolean isCommentVisible = (Boolean) visibility.get(recipient);

            List messages = getMessagesForRecipient(recipient);
            assertTrue(messages.size() == 1);

            if (isCommentVisible.booleanValue())
            {
                assertEmailBodyContains((MimeMessage) messages.get(0), COMMENT_TEXT);
            }
            else
            {
                assertEmailBodyDoesntContain((MimeMessage) messages.get(0), COMMENT_TEXT);
            }
        }
    }

    private void _performBulkOperation(final String chkId, final String workflow, final String resolution, final String commentLevel)
            throws InterruptedException, MessagingException
    {
        navigation.issueNavigator().gotoNavigator();
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);

        tester.checkCheckbox("bulkedit_" + chkId);
        navigation.clickOnNext();

        tester.setFormElement("operation", "bulk.workflowtransition.operation.name");
        navigation.clickOnNext();

        navigation.workflows().chooseWorkflowAction(workflow);

        if (resolution != null)
        {
            tester.selectOption("resolution", resolution);
        }
        tester.checkCheckbox("commentaction", "comment");
        tester.setFormElement("comment", COMMENT_TEXT);
        if (commentLevel != null)
        {
            tester.selectOption("commentLevel", commentLevel);
        }
        navigation.clickOnNext();

        navigation.clickOnNext();

        waitAndReloadBulkOperationProgressPage();

        // there should be 3 notifications
        flushMailQueueAndWait(3);
        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(3, mimeMessages.length);

        // assert recipients of emails
        assertRecipientsHaveMessages(EasyList.build("admin@example.com", "devman@example.com", "fred@example.com"));
    }
}
