package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.Permissions;
import org.junit.Ignore;

import javax.mail.MessagingException;
import java.io.IOException;

@WebTest ({ Category.FUNC_TEST, Category.EMAIL, Category.FIELDS })
public class TestCustomFieldNotifications extends EmailFuncTestCase
{
    private static final String ISSUE_COMMENT = "If it bleeds we can kill it!";
    private static final String SUBJECT = "[JIRATEST] (HSP-1) Dude";


    public void testCFNotifications() throws InterruptedException, MessagingException, IOException
    {
        administration.restoreData("TestCustomFieldNotifications.xml");
        configureAndStartSmtpServerWithNotify();

        //lets add a comment.  This should trigger 5 notifications.
        navigation.issue().addComment("HSP-1", ISSUE_COMMENT, null);

        //lets check all the right notifications were sent out.
        flushMailQueueAndWait(6);

        assertCorrectNumberEmailsSent(6);

        assertEmailSent("user@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("multiuser@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("singlegroup@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("multigroup@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("admin@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("fred@example.com", SUBJECT, ISSUE_COMMENT);
    }

    public void testCFNotificationsPublicIssue() throws InterruptedException, MessagingException, IOException
    {
        administration.restoreData("TestCustomFieldNotifications.xml");
        configureAndStartSmtpServerWithNotify();

        //now give Anyone the permission to view HSP-1, which should add the single e-mail notification to the list.
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.BROWSE, "");

        navigation.issue().addComment("HSP-1", ISSUE_COMMENT, null);

        //lets check all the right notifications were sent out.
        flushMailQueueAndWait(7);

        assertCorrectNumberEmailsSent(7);

        assertEmailSent("user@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("multiuser@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("singlegroup@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("multigroup@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("admin@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("fred@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("johnson@example.com", SUBJECT, ISSUE_COMMENT);
    }

    public void testDeletedCFDoesntCauseErrors() throws InterruptedException, IOException, MessagingException
    {
        //this data contains a couple of invalid customfield notifications.
        administration.restoreData("TestInvalidCustomFieldNotifications.xml");
        configureAndStartSmtpServerWithNotify();

        //give Anyone the permission to view HSP-1, which should add the single e-mail notification to the list.
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.BROWSE, "");

        navigation.issue().addComment("HSP-1", ISSUE_COMMENT, null);

        //lets check all the right notifications were sent out.
        flushMailQueueAndWait(3);

        assertCorrectNumberEmailsSent(3);

        assertEmailSent("admin@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("fred@example.com", SUBJECT, ISSUE_COMMENT);
        assertEmailSent("johnson@example.com", SUBJECT, ISSUE_COMMENT);
    }

}