package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.JIRAServerSetup;
import com.atlassian.jira.webtests.ztests.email.ShareClient;
import com.google.common.collect.Sets;
import com.icegreen.greenmail.util.GreenMailUtil;

import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static com.atlassian.jira.testkit.client.IssuesControl.HSP_PROJECT_ID;
import static java.util.Collections.singleton;
import static javax.ws.rs.core.Response.Status;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@WebTest({Category.FUNC_TEST, Category.REST, Category.EMAIL})
public final class TestShareResource extends EmailFuncTestCase
{
    public static final String FAKE_EMAIL = "fake@example.com";
    private String issueKey;
    private ShareClient shareClient;

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
        mailService.addUser(FAKE_EMAIL, "fake", "fake");

        backdoor.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        backdoor.usersAndGroups().addUserToGroup(BOB_USERNAME, JIRA_DEV_GROUP);

        backdoor.userProfile().changeUserNotificationType(BOB_USERNAME, "text");
        backdoor.userProfile().changeUserNotificationType(FRED_USERNAME, "html");
    }

    public void testShareIssueWithSelf() throws Exception
    {
        final Response response = shareClient.shareIssue(issueKey, Sets.newHashSet(ADMIN_USERNAME), Sets.<String>newHashSet(), "This should be sent!");
        assertThat(response.statusCode, equalTo(Status.OK.getStatusCode()));

        flushMailQueueAndWait(0);
        //sharing with yourself should work regardless
        assertThat(mailService.getReceivedMessages().length, equalTo(1)); // messages!
    }

    public void testAlsoSharedWithInTextNotification() throws Exception
    {
        final Set<String> userNames = Sets.newHashSet(BOB_USERNAME);
        final Set<String> emails = Sets.newHashSet(FAKE_EMAIL);

        final Response response = shareClient.shareIssue(issueKey, userNames, emails, "This shouldn't be sent!");
        assertThat(response.statusCode, equalTo(Status.OK.getStatusCode()));

        flushMailQueueAndWait(0);

        final MimeMessage fredMessage = getMessagesForRecipient(BOB_EMAIL).get(0);
        assertEmailBodyContains(fredMessage, "Also shared with");
        assertEmailBodyContains(fredMessage, FAKE_EMAIL);
        assertEmailBodyDoesntContain(fredMessage, "</html");
    }

    public void testAlsoSharedWithInHtmlNotification() throws Exception
    {
        final Set<String> userNames = Sets.newHashSet(FRED_USERNAME);
        final Set<String> emails = Sets.newHashSet(FAKE_EMAIL);

        final Response response = shareClient.shareIssue(issueKey, userNames, emails, "This shouldn't be sent!");
        assertThat(response.statusCode, equalTo(Status.OK.getStatusCode()));

        flushMailQueueAndWait(0);

        final MimeMessage fredMessage = getMessagesForRecipient(FRED_EMAIL).get(0);
        assertEmailBodyContains(fredMessage, "Also shared with");
        assertEmailBodyContains(fredMessage, FAKE_EMAIL);
        assertEmailBodyContains(fredMessage, "</html>");
    }

    public void testShouldSentShareEvenIfMoreThanSixRecipients() throws Exception
    {
        final Set<String> userNames = Sets.newHashSet();
        final Set<String> emails = Sets.newHashSet("1@example.com", "2@example.com", "3@example.com", "4@example.com",
                "5@example.com", "6@example.com", "7@example.com");
        addMailsToService(emails);

        final Response response = shareClient.shareIssue(issueKey, userNames, emails, "This shouldn't be sent!");
        assertThat(response.statusCode, equalTo(Status.OK.getStatusCode()));

        flushMailQueueAndWait(0);

        for (String email : emails)
        {
            final MimeMessage message = getMessagesForRecipient(email).get(0);
            assertEmailBodyContains(message, "Also shared with");
            assertMessageContainsSharedWithWithoutRecipient(email, emails, message);
        }
    }

    public void testShouldNotIncludeAuthorInSharedWithListButSendHimMessage() throws Exception
    {
        shareClient.loginAs(BOB_USERNAME);
        final Set<String> userNames = Sets.newHashSet(FRED_USERNAME, BOB_USERNAME);
        final Set<String> emails = Sets.newHashSet(FAKE_EMAIL);

        final Response response = shareClient.shareIssue(issueKey, userNames, emails, "This shouldn't be sent!");
        assertThat(response.statusCode, equalTo(Status.OK.getStatusCode()));

        flushMailQueueAndWait(0);

        //assert that Bob go message
        assertThat(getMessagesForRecipient(BOB_EMAIL), hasSize(1));

        assertThatAuthorIsNotPresentInAlsoSharedWithList();
    }

    public void testShouldNotSendShareMessageWhenUserHaveNoRights() throws Exception
    {
        shareClient.loginAs(FRED_USERNAME);
        final Set<String> userNames = Sets.newHashSet(BOB_USERNAME);
        final Set<String> emails = Sets.newHashSet(FAKE_EMAIL);

        final Response response = shareClient.shareIssue(issueKey, userNames, emails, "Fred have no rights to share!");
        assertThat(response.statusCode, equalTo(Status.BAD_REQUEST.getStatusCode()));

        flushMailQueueAndWait(0);

        assertThat(mailService.getReceivedMessages().length, equalTo(0));
    }

    public void testShouldSendShareJql() throws Exception
    {
        final Set<String> userNames = Sets.newHashSet(BOB_USERNAME);
        final Set<String> emails = Sets.newHashSet(FAKE_EMAIL);
        final String jql = "Some_JQL_in_here";

        final Response response = shareClient.shareSearchQuery(jql, userNames, emails, "Share Jql");
        assertThat(response.statusCode, equalTo(Status.OK.getStatusCode()));

        flushMailQueueAndWait(0);

        final MimeMessage mailMessage = getMessagesForRecipient(BOB_EMAIL).get(0);
        assertEmailBodyContains(mailMessage, jql);
    }

    public void testShouldSendShareSavedFilter() throws Exception
    {
        final Set<String> userNames = Sets.newHashSet(BOB_USERNAME);
        final Set<String> emails = Sets.newHashSet(FAKE_EMAIL);
        final String jql = "issuekey=HSP-1";

        final String savedFilterId = backdoor.filters().createFilter(jql, "FilterName", ADMIN_USERNAME, JIRA_DEV_GROUP);

        final Response response = shareClient.shareSavedSearch(savedFilterId, userNames, emails, "Share saved filter");
        assertThat(response.statusCode, equalTo(Status.OK.getStatusCode()));

        flushMailQueueAndWait(0);

        final MimeMessage mailMessage = getMessagesForRecipient(BOB_EMAIL).get(0);
        assertEmailBodyContains(mailMessage, savedFilterId);
        assertEmailBodyDoesntContain(mailMessage, jql);
    }

    public void testShouldSendShareJqlInsteadOfSavedFilterIfRecipientHaveNoPermissionsToViewThatFilter() throws Exception
    {
        final Set<String> userNames = Sets.newHashSet(FRED_USERNAME);
        final Set<String> emails = Sets.newHashSet(FAKE_EMAIL);
        final String jql = "issuekey=HSP-1";

        final String savedFilterId = backdoor.filters().createFilter(jql, "FilterName", ADMIN_USERNAME, JIRA_DEV_GROUP);

        final Response response = shareClient.shareSavedSearch(savedFilterId, userNames, emails, "Share saved filter");
        assertThat(response.statusCode, equalTo(Status.OK.getStatusCode()));

        flushMailQueueAndWait(0);

        final MimeMessage mailMessage = getMessagesForRecipient(FRED_EMAIL).get(0);
        assertEmailBodyContains(mailMessage, toUrlParam(jql));
        assertEmailBodyDoesntContain(mailMessage, savedFilterId);
    }

    public void testShouldNotSendShareMessageWhenNoRecipientsSpecified() throws Exception
    {
        shareClient.loginAs(BOB_USERNAME);

        final Response response = shareClient.shareIssue(issueKey, Sets.<String>newHashSet(), Sets.<String>newHashSet(), "No recipients specified!");
        assertThat(response.statusCode, equalTo(Status.BAD_REQUEST.getStatusCode()));

        flushMailQueueAndWait(0);

        assertThat(mailService.getReceivedMessages().length, equalTo(0));
    }

    private String toUrlParam(final String jql)
    {
        return JiraUrlCodec.encode(jql, "UTF-8");
    }

    private void assertThatAuthorIsNotPresentInAlsoSharedWithList() throws MessagingException
    {
        final MimeMessage mailMessage = getMessagesForRecipient(FRED_EMAIL).get(0);
        final String mailBody = GreenMailUtil.getBody(mailMessage);
        final String footerPartOfBody = mailBody.substring(mailBody.indexOf("Also shared with"));
        final String footerWithoutAvatars = footerPartOfBody.substring(0, footerPartOfBody.indexOf("</html>"));

        assertThat(footerWithoutAvatars, allOf(containsString(FAKE_EMAIL), not(containsString(BOB_USERNAME))));
    }

    private void assertMessageContainsSharedWithWithoutRecipient(final String email, final Set<String> emails, final MimeMessage message) throws Exception
    {
        final Set<String> otherRecipients = Sets.difference(emails, singleton(email));
        for (String otherRecipient : otherRecipients)
        {
            assertEmailBodyContains(message, otherRecipient);
        }
    }

    private void addMailsToService(final Set<String> emails)
    {
        for (String email : emails)
        {
            mailService.addUser(email, email, email);
        }
    }

}
