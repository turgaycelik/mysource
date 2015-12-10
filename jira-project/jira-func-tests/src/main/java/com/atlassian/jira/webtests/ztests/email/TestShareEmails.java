package com.atlassian.jira.webtests.ztests.email;

import java.io.IOException;
import java.util.Set;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.JIRAServerSetup;

import com.google.common.collect.Sets;
import com.icegreen.greenmail.util.GreenMailUtil;

import static com.atlassian.jira.functest.framework.util.RegexMatchers.regexMatches;
import static com.atlassian.jira.testkit.client.IssuesControl.HSP_PROJECT_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestShareEmails extends EmailFuncTestCase
{
    private ShareClient shareClient;
    private String issueKey;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreBlankInstance();

        startMailService(JIRAServerSetup.SMTP);
        int smtpPort = mailService.getSmtpPort();
        backdoor.mailServers().addSmtpServer(smtpPort);

        issueKey = backdoor.issues().createIssue(HSP_PROJECT_ID, "Issue 1", ADMIN_USERNAME).key();
        shareClient = new ShareClient(getEnvironmentData());

        mailService.addUser(BOB_EMAIL, BOB_USERNAME, BOB_PASSWORD);
        mailService.addUser(FRED_EMAIL, FRED_USERNAME, FRED_PASSWORD);
        mailService.addUser("fake@example.com", "fake", "fake");

        backdoor.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);

        backdoor.userProfile().changeUserNotificationType(BOB_USERNAME, "text");
        backdoor.userProfile().changeUserNotificationType(FRED_USERNAME, "html");
    }

    public void testAll() throws Exception
    {
        log("Running _testShareIssue");
        _testShareIssue();
        log("Running _testShareSavedSearch");
        _testShareSavedSearch();
        log("Running _testShareJqlSearch");
        _testShareJqlSearch();
    }

    public void _testShareIssue() throws Exception
    {
        Set<String> usernames = Sets.newHashSet(FRED_USERNAME, BOB_USERNAME);
        Set<String> emails = Sets.newHashSet("fake@example.com");
        String comment = "I thought you should know";
        shareClient.shareIssue(issueKey, usernames, emails, comment);

        flushMailQueueAndWait(3);
        MimeMessage[] mimeMessages = mailService.getReceivedMessagesAndClear();

        // 1. Check the HTML email
        MimeMessage message = getMessageForAddress(mimeMessages, FRED_EMAIL);
        String subject = message.getSubject();
        String body = GreenMailUtil.getBody(message);
        assertMessageIsHtml(message);
        assertEquals("[JIRATEST] Administrator shared \"HSP-1: Issue 1\" with you", subject);
        assertThat(body, containsString("shared</strong> an issue with you"));
        assertThat(body, containsString(comment));
        assertThat(body, regexMatches("/browse/HSP-1\".*>HSP-1</a>"));


        // 2. Check the Text email
        message = getMessageForAddress(mimeMessages, BOB_EMAIL);
        subject = message.getSubject();
        body = GreenMailUtil.getBody(message);
        assertMessageIsText(message);
        assertEquals("[JIRATEST] Administrator shared \"HSP-1: Issue 1\" with you", subject);
        assertThat(body, containsString("Administrator shared an issue with you"));
        assertThat(body, containsString("/browse/HSP-1"));
        assertThat(body, containsString(comment));
        assertThat(body, containsString("Key: HSP-1"));
        assertThat(body, containsString("Project: homosapien"));

        // 3. Check that the email sent to the email address is HTML
        message = getMessageForAddress(mimeMessages, "fake@example.com");
        assertMessageIsHtml(message);
    }

    public void _testShareSavedSearch() throws Exception
    {
        String searchJql = "project = HSP";
        String searchName = "Funky Homosapiens";
        String searchDescription = "Find those dudes!";
        String jsonShareString = "[{\"type\":\"global\"}]";
        String filterId = backdoor.searchRequests().createFilter("admin", searchJql, searchName, searchDescription, jsonShareString);

        Set<String> usernames = Sets.newHashSet(FRED_USERNAME, BOB_USERNAME);
        Set<String> emails = Sets.newHashSet("fake@example.com");
        String comment = "I thought you should know";
        shareClient.shareSavedSearch(filterId, usernames, emails, comment);

        flushMailQueueAndWait(3);
        MimeMessage[] mimeMessages = mailService.getReceivedMessagesAndClear();

        // 1. Check the HTML email
        MimeMessage message = getMessageForAddress(mimeMessages, FRED_EMAIL);
        String subject = message.getSubject();
        String body = GreenMailUtil.getBody(message);
        assertEquals("[JIRATEST] Administrator shared the filter \"Funky Homosapiens\" with you", subject);
        assertThat(body, containsString("Administrator</a>"));
        assertThat(body, containsString("<b>shared</b> a filter with you"));
        assertThat(body, containsString("I thought you should know"));
        //assertThat(body, regexMatches("/secure/IssueNavigator.jspa\\?mode=hide\\&amp;requestId=\\d+"));
        assertThat(body, regexMatches("/secure/IssueNavigator.jspa\\?mode=hide\\&amp;requestId="+filterId+"\" style=\".*\">"+searchName));

        // 2. Check the Text email
        message = getMessageForAddress(mimeMessages, BOB_EMAIL);
        subject = message.getSubject();
        body = GreenMailUtil.getBody(message);
        assertMessageIsText(message);
        assertEquals("[JIRATEST] Administrator shared the filter \"Funky Homosapiens\" with you", subject);
        assertThat(body, containsString("Administrator shared a filter with you"));
        assertThat(body, containsString("I thought you should know"));
        assertThat(body, containsString("/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId));

        // 3. Check that the email sent to the email address links to JQL, not the filter
        message = getMessageForAddress(mimeMessages, "fake@example.com");
        subject = message.getSubject();
        body = GreenMailUtil.getBody(message);
        assertMessageIsHtml(message);
        assertEquals("[JIRATEST] Administrator shared a search result with you", subject);
        assertThat(body, containsString("Administrator</a>"));
        assertThat(body, containsString("<b>shared</b> a search result with you"));
        assertThat(body, containsString("I thought you should know"));
//        assertThat(body, containsString("/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+HSP\">View search results"));
        assertThat(body, regexMatches("/secure/IssueNavigator.jspa\\?reset=true\\&amp;jqlQuery=project\\+%3D\\+HSP\""));
    }

    public void _testShareJqlSearch() throws Exception
    {
        String searchJql = "project = HSP";

        Set<String> usernames = Sets.newHashSet(FRED_USERNAME, BOB_USERNAME);
        String comment = "I thought you should know";
        shareClient.shareSearchQuery(searchJql, usernames, null, comment);

        flushMailQueueAndWait(2);
        MimeMessage[] mimeMessages = mailService.getReceivedMessages();

        // 1. Check the HTML email
        MimeMessage message = getMessageForAddress(mimeMessages, FRED_EMAIL);
        String subject = message.getSubject();
        String body = GreenMailUtil.getBody(message);
        assertMessageIsHtml(message);
        assertEquals("[JIRATEST] Administrator shared a search result with you", subject);
        assertThat(body, containsString("Administrator</a>"));
        assertThat(body, containsString("<b>shared</b> a search result with you"));
        assertThat(body, containsString("I thought you should know"));
        assertThat(body, regexMatches("/secure/IssueNavigator.jspa\\?reset=true\\&amp;jqlQuery=project\\+%3D\\+HSP\" style=\".*\">View search results"));

        // 2. Check the Text email
        message = getMessageForAddress(mimeMessages, BOB_EMAIL);
        subject = message.getSubject();
        body = GreenMailUtil.getBody(message);
        assertMessageIsText(mimeMessages[0]);
        assertEquals("[JIRATEST] Administrator shared a search result with you", subject);
        assertThat(body, containsString("Administrator shared a search result with you"));
        assertThat(body, containsString(comment));
        assertThat(body, containsString("/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+HSP"));
    }

    private void assertMessageIsText(MimeMessage textMessage) throws MessagingException, IOException
    {
        assertMessageContainsContentWithType(textMessage, "text/plain; charset=UTF-8");
    }

    private void assertMessageIsHtml(MimeMessage textMessage) throws MessagingException, IOException
    {
        assertMessageContainsContentWithType(textMessage, "text/html; charset=UTF-8");
    }

    private void assertMessageContainsContentWithType(MimeMessage htmlMessage, String expectedType) throws MessagingException, IOException
    {
        final Object content = htmlMessage.getContent();
        if(content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0 ; i < multipart.getCount() ; i++) {
                final BodyPart bodyPart = multipart.getBodyPart(i);
                if(expectedType.equals(bodyPart.getContentType())) {
                    return; // At least one part is text/html
                }
            }
            fail("Message did not contain any BodyPart with type: " + expectedType);
        }
        assertEquals(expectedType, htmlMessage.getContentType());
    }

    private MimeMessage getMessageForAddress(MimeMessage[] messages, String toAddress) throws MessagingException
    {
        for (MimeMessage message : messages)
        {
            Address[] recipients = message.getRecipients(Message.RecipientType.TO);
            if (recipients[0].toString().equals(toAddress))
            {
                return message;
            }
        }
        fail("Didn't find a message for : " + toAddress);
        return null;
    }
}
