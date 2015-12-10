package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import org.apache.commons.lang.StringUtils;

import javax.mail.internet.MimeMessage;

/**
 * Test for notification e-mails.
 *
 * @since v4.1
 */
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestNotificationEmails extends EmailFuncTestCase
{
    private static final String ADMIN_ONLY_KEY = "HSP-1";
    private static final String ADMIN_ONLY_SUMMARY = "Admin Not Watched";

    private static final String BOTH_KEY = "HSP-2";
    private static final String BOTH_SUMMARY = "Admin Watched by Fred";

    private static final String EMAIL_PREFIX = "JIRATEST";

    private static final String EDIT_ISSUE_COMMENT = "Edit issue comment.";
    private static final String COMMENT_ON_ISSUE_COMMENT = "Comment on the issue comment.";
    private static final String ASSIGN_ISSUE_COMMENT = "I am assigning this issue to myself.";
    private static final String RESOLVE_ISSUE_COMMENT = "I resolved the issue.";
    private static final String REOPEN_ISSUE_COMMENT = "I reopened the issue.";

    private static final String ADMIN_EMAIL = "admin@example.com";

    private static final String GROUP_PRIVATE = "GROUP PRIVATE: ";
    private static final String ROLE_PRIVATE = "ROLE PRIVATE: ";

    private static final String GROUPS_ADMINS = "jira-administrators";
    private static final String ROLE_ADMINS = "Administrators";

    private static final String UNASSIGNED = "Unassigned";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreData("TestNotificationEmails.xml");
        backdoor.darkFeatures().enableForSite("no.frother.assignee.field");
        configureAndStartSmtpServerWithNotify();
        backdoor.userProfile().changeUserNotificationType("admin", "text");
    }

    @Override
    public void tearDownTest()
    {
        backdoor.darkFeatures().disableForSite("no.frother.assignee.field");
        super.tearDownTest();
    }

    //Make sure a comment is sent in the e-mail when someone edits an issue.
    public void testEmailHasCommentsWhenEditIssue() throws Exception
    {
        // Test for TXT.
        final MailBox adminFolder = getMailBox(ADMIN_EMAIL);
        final MailBox fredFolder = getMailBox(FRED_EMAIL);

        //Test for HTML.
        navigation.issue().gotoIssue(ADMIN_ONLY_KEY);

        //Only the Admin should get this message with the comment when editing this issue.
        updateIssue(EDIT_ISSUE_COMMENT, null, null);

        MimeMessage message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, ADMIN_ONLY_KEY, ADMIN_ONLY_SUMMARY);
        text.assertTextSequence(message.getFrom()[0].toString(), "Administrator", "jiratest@atlassian.com");
        assertMessageAndType(message, EDIT_ISSUE_COMMENT, false);

        //Both and Admin and Fred should get the message with the comment.
        navigation.issue().gotoIssue(BOTH_KEY);
        updateIssue(EDIT_ISSUE_COMMENT, null, null);

        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, EDIT_ISSUE_COMMENT, false);

        message = fredFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, EDIT_ISSUE_COMMENT, true);

        // Now lets try e-mail that should not go through Fred because he cannot see the comment (group permissions).
        // NOTE: For some reason, when coming from non-workflow actions we do not deliver e-mails if we cannot see the comment.
        String comment = createGroupComment(EDIT_ISSUE_COMMENT);
        navigation.issue().gotoIssue(BOTH_KEY);
        updateIssue(comment, GROUPS_ADMINS, "Blocker");

        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, comment, false);
        assertNull(fredFolder.nextMessage());

        // Now lets try e-mail that should not go through Fred because he cannot see the comment (role permissions).
        // NOTE: For some reason, when coming from non-workflow actions we do not deliver e-mails if we cannot see the comment.
        comment = createRoleComment(EDIT_ISSUE_COMMENT);
        navigation.issue().gotoIssue(BOTH_KEY);
        updateIssue(comment, ROLE_ADMINS, "Minor");

        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, comment, false);
        assertNull(fredFolder.nextMessage());
    }

    //Make sure a comment is sent in the e-mail when someone comments on an issue.
    public void testEmailHasCommentWhenAddingComment() throws Exception
    {
        final MailBox adminFolder = getMailBox(ADMIN_EMAIL);
        final MailBox fredFolder = getMailBox(FRED_EMAIL);

        //Only the Admin should see this comment because Fred is not watching the issue.
        addIssueComment(ADMIN_ONLY_KEY, COMMENT_ON_ISSUE_COMMENT, null);
        MimeMessage message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, ADMIN_ONLY_KEY, ADMIN_ONLY_SUMMARY);
        assertMessageAndType(message, COMMENT_ON_ISSUE_COMMENT, false);
        assertNull(fredFolder.nextMessage());

        //Both and Admin and Fred should get the message with the comment as Fred is watching the issue.
        addIssueComment(BOTH_KEY, COMMENT_ON_ISSUE_COMMENT, null);
        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, COMMENT_ON_ISSUE_COMMENT, false);
        message = fredFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, COMMENT_ON_ISSUE_COMMENT, true);

        //Now lets try e-mail that should not go through Fred because he cannot see the comment (group permissions).
        String comment = createGroupComment(COMMENT_ON_ISSUE_COMMENT);
        addIssueComment(BOTH_KEY, comment, GROUPS_ADMINS);
        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, comment, false);
        assertNull(fredFolder.nextMessage());

        //Now lets try e-mail that should not go through Fred because he cannot see the comment (role permissions).
        comment = createRoleComment(COMMENT_ON_ISSUE_COMMENT);
        addIssueComment(BOTH_KEY, comment, ROLE_ADMINS);
        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, comment, false);
        assertNull(fredFolder.nextMessage());
    }

    //Make sure a comment is sent in the e-mail when someone assigns an issue.
    public void testEmailHasCommentWhenAssigningIssue() throws Exception
    {
        final MailBox adminFolder = getMailBox(ADMIN_EMAIL);
        final MailBox fredFolder = getMailBox(FRED_EMAIL);

        //Only the Admin should see this comment because Fred is not watching the issue.
        assignIssue(ADMIN_ONLY_KEY, ADMIN_FULLNAME, ASSIGN_ISSUE_COMMENT, null);
        MimeMessage message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, ADMIN_ONLY_KEY, ADMIN_ONLY_SUMMARY);
        assertMessageAndType(message, ASSIGN_ISSUE_COMMENT, false);

        assertNull(fredFolder.nextMessage());

        //Both and Admin and Fred should get the message with the comment as Fred is watching the issue.
        assignIssue(BOTH_KEY, ADMIN_FULLNAME, ASSIGN_ISSUE_COMMENT, null);
        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, ASSIGN_ISSUE_COMMENT, false);
        message = fredFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, ASSIGN_ISSUE_COMMENT, true);

        //Now lets try e-mail that should not go through Fred because he cannot see the comment (group permissions).
        // NOTE: For some reason, when coming from non-workflow actions we do not deliver e-mails if we cannot see the comment.
        String comment = createGroupComment(ASSIGN_ISSUE_COMMENT);
        assignIssue(BOTH_KEY, UNASSIGNED, comment, GROUPS_ADMINS);
        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, comment, false);
        assertNull(fredFolder.nextMessage());

        //Now lets try e-mail that should not go through Fred because he cannot see the comment (group permissions).
        // NOTE: For some reason, when coming from non-workflow actions we do not deliver e-mails if we cannot see the comment.
        comment = createRoleComment(ASSIGN_ISSUE_COMMENT);
        assignIssue(BOTH_KEY, ADMIN_FULLNAME, comment, ROLE_ADMINS);
        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, comment, false);
        assertNull(fredFolder.nextMessage());
    }

    /*
     * JRA-20604, JRADEV-1246: Make sure that comments are sent in the e-mails that result from transitioning an issue.
     */
    public void testEmailHasCommentWhenTransitioningAnIssue() throws Exception
    {
        final MailBox adminFolder = getMailBox(ADMIN_EMAIL);
        final MailBox fredFolder = getMailBox(FRED_EMAIL);

        //Only the Admin should see this comment because Fred is not watching the issue.
        resolveIssue(ADMIN_ONLY_KEY, RESOLVE_ISSUE_COMMENT, null);
        MimeMessage message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, ADMIN_ONLY_KEY, ADMIN_ONLY_SUMMARY);
        assertMessageAndType(message, RESOLVE_ISSUE_COMMENT, false);

        assertNull(fredFolder.nextMessage());

        //Both and Admin and Fred should get the message with the comment as Fred is watching the issue.
        resolveIssue(BOTH_KEY, RESOLVE_ISSUE_COMMENT, null);
        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, RESOLVE_ISSUE_COMMENT, false);
        message = fredFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, RESOLVE_ISSUE_COMMENT, true);

        //Two e-mails should be sent, however, the comment should be hidden for Fred that cannot see the comment at a
        // group level.
        String comment = createGroupComment(REOPEN_ISSUE_COMMENT);
        reopenIssue(BOTH_KEY, comment, GROUPS_ADMINS);
        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, comment, false);
        message = fredFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertNotMessageAndType(message, comment, true);

        //Two e-mails should be sent, however, the comment should be hidden for Fred that cannot see the comment at a
        // role level.
        comment = createRoleComment(RESOLVE_ISSUE_COMMENT);
        resolveIssue(BOTH_KEY, comment, ROLE_ADMINS);
        message = adminFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertMessageAndType(message, comment, false);
        message = fredFolder.awaitMessage();
        text.assertTextSequence(message.getSubject(), EMAIL_PREFIX, BOTH_KEY, BOTH_SUMMARY);
        assertNotMessageAndType(message, comment, true);
    }

    private void updateIssue(final String comment, final String security, final String priority)
            throws InterruptedException
    {
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement("comment", comment);
        if (StringUtils.isNotBlank(priority))
        {
            tester.selectOption("priority", priority);
        }

        if (StringUtils.isNotBlank(security))
        {
            tester.selectOption("commentLevel", security);
        }
        tester.clickButton("issue-edit-submit");
        flushMailQueueAndWait(1);
    }

    private void addIssueComment(final String key, final String comment, final String security)
            throws InterruptedException
    {
        navigation.issue().addComment(key, comment, security);
        flushMailQueueAndWait(1);
    }

    private void assignIssue(final String key, final String username, final String comment, final String security)
            throws InterruptedException
    {
        navigation.issue().gotoIssue(key);
        tester.clickLink("assign-issue");
        tester.selectOption("assignee", username);
        if (comment != null)
        {
            tester.setFormElement("comment", comment);
        }
        if (security != null)
        {
            tester.selectOption("commentLevel", security);
        }
        tester.clickButton("assign-issue-submit");
        flushMailQueueAndWait(1);
    }

    private void reopenIssue(final String key, final String comment, final String security) throws InterruptedException
    {
        doWorkflow(key, 3, comment, security);
    }

    private void resolveIssue(final String key, final String comment, final String security) throws InterruptedException
    {
        doWorkflow(key, 5, comment, security);
    }

    private void doWorkflow(final String key, int id, final String comment, final String security)
            throws InterruptedException
    {
        navigation.issue().gotoIssue(key);
        tester.clickLink("action_id_" + id);
        tester.setWorkingForm("issue-workflow-transition");
        if (comment != null)
        {
            tester.setFormElement("comment", comment);
        }
        if (security != null)
        {
            tester.selectOption("commentLevel", security);
        }
        tester.submit("Transition");
        flushMailQueueAndWait(1);
    }

    private String createGroupComment(final String comment)
    {
        return GROUP_PRIVATE + comment;
    }

    private String createRoleComment(final String comment)
    {
        return ROLE_PRIVATE + comment;
    }
}
