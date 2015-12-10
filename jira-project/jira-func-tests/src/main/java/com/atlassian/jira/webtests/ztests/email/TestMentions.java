package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.icegreen.greenmail.store.FolderException;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestMentions extends EmailFuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestMentions.xml");
        configureAndStartSmtpServerWithNotify();
        mailService.addUser("admin@example.com", "admin", "admin");
        mailService.addUser("bob@example.com", "bob", "bob");
        mailService.addUser("fred@example.com", "fred", "fred");
        backdoor.userProfile().changeUserNotificationType("admin", "text");
    }

    public void testSendMentions() throws InterruptedException, MessagingException, IOException, FolderException
    {
        navigation.issue().addComment("HSP-1", "I think [~bob] should have a look at this!");

        flushMailQueueAndWait(2);
        MimeMessage[] mimeMessages = mailService.getReceivedMessages();

        assertEquals(2, mimeMessages.length);
        MimeMessage mimeMessage = getMentionMail(mimeMessages);

        assertEmailToEquals(mimeMessage, "bob@example.com");
        assertEmailSubjectEquals(mimeMessage, "[JIRATEST] Administrator mentioned you (JIRA)");
        assertEmailBodyContains(mimeMessage, "Bob Brown");
        assertEmailBodyContains(mimeMessage, "should have a look at this");


        navigation.issue().addComment("HSP-1", "This is a test comment for [~admin].");
        flushMailQueueAndWait(1);

        //now lets try editing
        mailService.removeAllReceivedMessages();
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(34, "jira-administrators");
        navigation.issue().viewIssue("HSP-1");
        tester.clickLink("edit_comment_10201");
        tester.setFormElement("comment", "I think [~admin] should have a look at this! [~bob] why don't you check this out too?");
        tester.submit();

        flushMailQueueAndWait(2);
        mimeMessages = mailService.getReceivedMessages();
        assertEquals(2, mimeMessages.length);
        mimeMessage = getMentionMail(mimeMessages);

        assertEmailToEquals(mimeMessage, "bob@example.com");
        assertEmailSubjectEquals(mimeMessage, "[JIRATEST] Administrator mentioned you (JIRA)");
        assertEmailBodyContains(mimeMessage, "Bob Brown");
        assertEmailBodyContains(mimeMessage, "why don't you check this out too?");
    }

    public void testSendMentionsOnlySendsOneEmail() throws InterruptedException, MessagingException, IOException, FolderException
    {
        navigation.issue().addComment("HSP-1", "I think [~admin] should have a look at this!");

        flushMailQueueAndWait(1);
        MimeMessage[] mimeMessages = mailService.getReceivedMessages();

        assertEquals(1, mimeMessages.length);
        MimeMessage mimeMessage = getMentionMail(mimeMessages);

        //we only got a notification e-mail!
        assertNull(mimeMessage);
        assertEmailBodyContains(mimeMessages[0], "Administrator commented on HSP-1");
        assertEmailBodyDoesntContain(mimeMessages[0], "mentioned");
    }

    public void testNoMentionsSentForUserWithoutBrowseUsersPermission() throws InterruptedException, MessagingException
    {
        navigation.logout();
        navigation.login("bob");

        navigation.userProfile().changeAutowatch(false);
        navigation.issue().addComment("HSP-1", "I think [~admin] should have a look at this!");

        navigation.logout();
        navigation.login("admin");

        flushMailQueueAndWait(1);

        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(1, mimeMessages.length);
        assertFalse(mimeMessages[0].getSubject().contains("mentioned"));
    }

    public void testNoMentionsSentForUserWithoutBrowseIssuePermission()
            throws InterruptedException, MessagingException, IOException
    {
        navigation.issue().addComment("HSP-1", "I think [~fred] should have a look at this!");

        flushMailQueueAndWait(1);

        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(1, mimeMessages.length);
        assertFalse(mimeMessages[0].getSubject().contains("mentioned"));
    }

    public void testSendHtmlMention()
            throws InterruptedException, MessagingException, IOException
    {
        navigation.issue().addComment("HSP-1", "I think [~bob] should have a look at this!");

        flushMailQueueAndWait(2);

        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(2, mimeMessages.length);
        MimeMessage mimeMessage = getMentionMail(mimeMessages);

        assertEmailSubjectEquals(mimeMessage, "[JIRATEST] Administrator mentioned you (JIRA)");
        assertEmailBodyContains(mimeMessage, "Bob Brown</a>");
        assertEmailBodyContains(mimeMessage, "<strong>mentioned you</strong> on");
    }

    public void testSendMentionForDescription()
            throws InterruptedException, MessagingException, IOException, FolderException
    {
        navigation.issue().createIssue("homosapien", "Bug", "This is a first test issue",
                MapBuilder.<String, String[]>newBuilder().add("description", new String[] { "Hello [~admin]. I created this! Hi [~bob] I just mentioned you!" }).toMap());

        flushMailQueueAndWait(2);

        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(2, mimeMessages.length);

        MimeMessage mimeMessage = getMentionMail(mimeMessages);

        assertEmailSubjectEquals(mimeMessage, "[JIRATEST] Administrator mentioned you (JIRA)");
        assertEmailBodyContains(mimeMessage, "I just mentioned you");
        assertEmailBodyContains(mimeMessage, "Bob Brown");


        navigation.issue().createIssue("homosapien", "Bug", "This is a first test issue",
                MapBuilder.<String, String[]>newBuilder().add("description", new String[] { "Hello [~admin]. I created this!" }).toMap());

        flushMailQueueAndWait(1);

        //now lets try editing
        mailService.removeAllReceivedMessages();
        navigation.issue().gotoEditIssue("HSP-4");
        tester.setFormElement("description", "Hello [~admin]. I created this! Hey [~bob] you are in the description too now!");
        tester.submit();

        //only bob should get an e-mail. Not admin.
        flushMailQueueAndWait(2);
        mimeMessages = mailService.getReceivedMessages();
        assertEquals(2, mimeMessages.length);

        mimeMessage = getMentionMail(mimeMessages);

        assertEmailToEquals(mimeMessage, "bob@example.com");
        assertEmailSubjectEquals(mimeMessage, "[JIRATEST] Administrator mentioned you (JIRA)");
        assertEmailBodyContains(mimeMessage, "Bob Brown");
        assertEmailBodyContains(mimeMessage, "you are in the description too now!");
    }

    /**
     * JRADEV-8147 When user is mentioned in issue comment, notification email contains relative url
     */
    public void testEmailUserLinkWhenMentionedInIssueComment() throws Exception
    {
        navigation.issue().addComment("HSP-1", "This is me mentioning [~bob]");
        final String baseUrl = backdoor.getTestkit().applicationProperties().getString("jira.baseurl");
        flushMailQueueAndWait(2);

        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(2, mimeMessages.length);

        MimeMessage mimeMessage = getMentionMail(mimeMessages);
        assertEmailBodyContains(mimeMessage, String.format("href=\"%s/secure/ViewProfile.jspa?name=bob\"", baseUrl));
    }

    /**
     * JRADEV-8084 User is emailed twice when they are watching and mentioned
     */
    public void testEmailUserOnceWhenWatchingAndMentioned() throws Exception
    {
        navigation.issue().addWatchers("HSP-1", "bob");
        navigation.issue().addComment("HSP-1", "This is me mentioning [~bob]");

        flushMailQueueAndWait(2);

        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(2, mimeMessages.length);
    }

    private MimeMessage getMentionMail(MimeMessage[] mimeMessages) throws MessagingException
    {
        for (MimeMessage mimeMessage : mimeMessages)
        {
            if (mimeMessage.getSubject().contains("mentioned"))
            {
                return mimeMessage;
            }
        }
        return null;
    }

}
