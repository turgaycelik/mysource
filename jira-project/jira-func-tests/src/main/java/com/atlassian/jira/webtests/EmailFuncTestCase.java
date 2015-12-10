package com.atlassian.jira.webtests;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.webtests.util.mail.MailService;
import com.google.common.collect.Sets;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.SimpleStoredMessage;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.opensymphony.util.TextUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import static com.atlassian.jira.functest.framework.util.RegexMatchers.regexMatches;
import static com.atlassian.jira.functest.framework.util.RegexMatchers.regexMatchesNot;
import static com.atlassian.jira.webtests.JIRAServerSetup.IMAP;
import static com.atlassian.jira.webtests.JIRAServerSetup.POP3;
import static com.atlassian.jira.webtests.JIRAServerSetup.SMTP;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This class extends FuncTestCase by adding methods to test emails being sent from JIRA.
 */
public class EmailFuncTestCase extends FuncTestCase implements FunctTestConstants
{
    public static final String DEFAULT_FROM_ADDRESS = "jiratest@atlassian.com";
    public static final String DEFAULT_SUBJECT_PREFIX = "[JIRATEST]";
    public static final String newline = "\r\n";
    public static final String HTML_FORMAT_REGEX = "<body class=\"jira\" style=\".*\">\\s*<table id=\"background-table\"";

    protected MailService mailService;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        mailService = new MailService(log);
    }

    public void tearDownTest()
    {
        mailService.stop();
    }

    /**
     * Use this method to start a {@link com.icegreen.greenmail.smtp.SmtpServer}. <p> This will also configure JIRA to
     * use this SMTP server in the admin section. You should call this after your data import. This will override any
     * existing mail servers setup already. </p> <p> A simple SMTP server proxy is started by first attempting to start
     * on a default port number. If this port is already used we try that port number plus one and so on for 10
     * attempts. this allows for multiple tests running in Bamboo concurrently, and also for a particular test machine
     * maybe using that port already. </p> <p> The tearDown() method will close the TCP socket. </p>
     */
    protected void configureAndStartSmtpServer()
    {
        configureAndStartMailServers(DEFAULT_FROM_ADDRESS, DEFAULT_SUBJECT_PREFIX, SMTP);
    }

    protected void configureAndStartSmtpServer(String from, String prefix)
    {
        configureAndStartMailServers(from, prefix, SMTP);
    }

    protected void configureAndStartMailServers(String from, String prefix, JIRAServerSetup... jiraServerSetups)
    {
        assertSendingMailIsEnabled();

        startMailService(jiraServerSetups);

        List<JIRAServerSetup> serverSetupList = Arrays.asList(jiraServerSetups);

        if (serverSetupList.contains(IMAP))
        {
            int imapPort = mailService.getImapPort();
            log("Setting IMAP server to 'localhost:" + imapPort +"'");
            backdoor.mailServers().addPopServer("Local Test Imap Server", "Imap Server for test purposes", "imap",
                     "localhost", imapPort, ADMIN_USERNAME, ADMIN_PASSWORD);
        }
        if (serverSetupList.contains(POP3))
        {
            int popPort = mailService.getPop3Port();
            log("Setting POP3 server to 'localhost:" + popPort +"'");
            backdoor.mailServers().addPopServer("Local Test Pop Server", "Pop Server for test purposes", "pop3",
                    "localhost", popPort, ADMIN_USERNAME, ADMIN_PASSWORD);
        }
        if (serverSetupList.contains(SMTP))
        {
            int smtpPort = mailService.getSmtpPort();
            log("Setting SMTP server to 'localhost:" + smtpPort + "'");
            backdoor.mailServers().addSmtpServer(from, prefix, smtpPort);
        }
    }

    protected void configureAndStartSmtpServerWithNotify()
    {
        configureAndStartSmtpServer();
        // Chances are you if you're testing mail you want this setting enabled from previous default
        navigation.userProfile().changeNotifyMyChanges(true);
    }

    protected void startMailService(JIRAServerSetup... jiraServerSetups)
    {
        try
        {
            mailService.configureAndStartGreenMail(jiraServerSetups);
        }
        catch (BindException e)
        {
            fail("Error: Could not start green mail server. See log for details.");
        }
    }

    /**
     * Given a comma seperated list of email addresses, returns a collection of the email addresses.
     *
     * @param emails comma seperated list of email addresses
     * @return collection of individual email address
     */
    protected Collection<String> parseEmailAddresses(String emails)
    {
        StringTokenizer st = new StringTokenizer(emails, ",");
        Collection<String> emailList = new ArrayList<String>();
        while (st.hasMoreTokens())
        {
            String email = st.nextToken().trim();
            if (TextUtils.stringSet(email))
            {
                emailList.add(email.trim());
            }
        }
        return emailList;
    }

    protected void assertRecipientsHaveMessages(Collection<String> recipients) throws MessagingException
    {
        for (String recipient : recipients)
        {
            assertFalse("Recipient '" + recipient + "' did not receive any messages", getMessagesForRecipient(recipient).isEmpty());
        }
    }

    protected List<MimeMessage> getMessagesForRecipient(String recipient) throws MessagingException
    {
        MimeMessage[] messages = mailService.getReceivedMessages();
        List<MimeMessage> ret = new ArrayList<MimeMessage>();

        for (MimeMessage message : messages)
        {
            if (Arrays.asList(message.getHeader("To")).contains(recipient))
            {
                ret.add(message);
            }
        }

        return ret;
    }

    protected void assertSendingMailIsEnabled()
    {
        navigation.gotoAdmin();
        tester.clickLink("mail_queue");

        try
        {
            final String responseText = tester.getDialog().getResponse().getText();
            if (responseText.contains("Sending mail is disabled"))
            {
                fail("Mail sending is disabled. Please restart your server without -Datlassian.mail.senddisabled=true.");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void setupJiraImapPopServer()
    {
        navigation.gotoAdmin();
        tester.clickLink("incoming_mail");
        tester.clickLinkWithText("Add POP / IMAP mail server");
        tester.setFormElement("name", "Local Test Pop/Imap Server");
        tester.setFormElement("serverName", "localhost");
        tester.setFormElement("username", ADMIN_USERNAME);
        tester.setFormElement("password", ADMIN_USERNAME);
        tester.submit("Add");
    }

    protected void setupPopService()
    {
        setupPopService("project=MKY, issue=1, createusers=true, issuetype=1");
    }

    protected void setupPopService(String handlerParameters)
    {
        navigation.gotoAdmin();
        tester.clickLink("services");
        tester.setFormElement("name", "pop");
        tester.setFormElement("clazz", "com.atlassian.jira.service.services.mail.MailFetcherService");
        tester.setFormElement("delay", "1");
        tester.submit("Add Service");
        tester.setFormElement("handler.params", handlerParameters);
        tester.setFormElement("delay", "1");
        tester.submit("Update");

    }

    protected void setupImapService()
    {
        setupImapService("project=MKY, issue=1, createusers=true, issuetype=1");
    }

    protected void setupImapService(String handlerParameters)
    {
        navigation.gotoAdmin();
        tester.clickLink("services");
        tester.setFormElement("name", "imap");
        tester.setFormElement("clazz", "com.atlassian.jira.service.services.mail.MailFetcherService");
        tester.setFormElement("delay", "1");
        tester.submit("Add Service");
        tester.setFormElement("handler.params", handlerParameters);
        tester.setFormElement("delay", "1");
        tester.submit("Update");
    }

    /**
     * This is useful for writing func tests that test that the correct notifications are being sent. It goest to the
     * admin section mail-queue and flushes the queue and waits till it recieves emailCount number of emails before
     * timeout. If the timeout is reached before the expected number of emails arrives will fail.
     *
     * @param emailCount number of expected emails to wait to receive
     * @throws InterruptedException if interrupted
     */
    protected void flushMailQueueAndWait(int emailCount) throws InterruptedException
    {
        flushMailQueueAndWait(emailCount, 500);
    }

    /**
     * Does the same as {@link #flushMailQueueAndWait(int)} but allows the user to specify the wait period in case a lot
     * of e-mails are being sent.
     *
     * @param emailCount number of expected emails to wait to receive
     * @param waitPeriodMillis The amount of time to wait in millis until the e-mails should have arrived.
     * @throws InterruptedException if interrupted
     */
    protected void flushMailQueueAndWait(int emailCount, int waitPeriodMillis) throws InterruptedException
    {
        flushMailQueue();
        log("Flushed mail queue. Waiting for '" + waitPeriodMillis + "' ms...");
        // Sleep for a small while - just to be sure the mail is received.
        final boolean receivedAllMail = mailService.waitForIncomingMessage(waitPeriodMillis, emailCount);

        if (!receivedAllMail)
        {
            String msg = "Did not recieve all expected emails (" + emailCount + ") within the timeout.";
            MimeMessage[] receivedMessages = mailService.getReceivedMessages();
            if (receivedMessages != null)
            {
                msg += " Only received " + receivedMessages.length + " message(s).";
                if (receivedMessages.length > 0)
                {
                    msg += "\n  Recipients: " + display(receivedMessages);
                }
            }
            else
            {
                msg += " Received zero messages.";
            }
            fail(msg);
        }
    }

    protected void flushMailQueue()
    {
        //flush mail queue
        backdoor.mailServers().flushMailQueue();
    }

    private String display(MimeMessage[] receivedMessages)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < receivedMessages.length; i++)
        {
            if (i > 0)
                sb.append(", ");
            MimeMessage receivedMessage = receivedMessages[i];
            try
            {
                sb.append(receivedMessage.getRecipients(Message.RecipientType.TO)[0]);
            }
            catch (MessagingException e)
            {
                sb.append("???");
            }
        }
        return sb.toString();
    }

    protected void waitForMail(int emailCount) throws InterruptedException
    {
        final int waitPeriodMillis = 500;
        assertTrue("Did not recieve all expected emails within the timeout", mailService.waitForIncomingMessage(waitPeriodMillis, emailCount));
    }

    /**
     * Asserts that the given email's body contains the bodySubString using indexOf.
     *
     * @param email email to extract the content body from
     * @param bodySubString expected substring of the email body
     * @throws MessagingException Message error
     * @throws IOException IO error
     * @see GreenMailUtil#getBody(javax.mail.Part)
     */
    protected void assertEmailBodyContains(MimeMessage email, String bodySubString)
            throws MessagingException, IOException
    {
        final String emailBody = GreenMailUtil.getBody(email);
        assertTrue("The string '" + bodySubString + "' was not found in the e-mail body [" + emailBody + "]",
                emailBody.contains(bodySubString));
    }

    /**
     * Asserts that the given email's body contains a line which matches the given string or pattern.
     * If multiple lines are specified, they must appear in the given order.
     *
     * @param email email to extract the content body from
     * @param linePattern expected line or line pattern
     * @throws MessagingException Message error
     * @throws IOException IO error
     * @see GreenMailUtil#getBody(javax.mail.Part)
     */
    protected void assertEmailBodyContainsLine(MimeMessage email, String... linePattern)
            throws MessagingException, IOException
    {
        // temporary fix for JRADEV-23803
        final String emailBody = decodeQuotedPrintableBody(email);

        String[] lines = emailBody.split("\\n");
        int linePatternIdx = 0;
        for (String line : lines)
        {
            if (line.trim().matches(linePattern[linePatternIdx]))
            {
                linePatternIdx++;
            }

            if (linePatternIdx >= linePattern.length)
            {
                return;     // All lines found - all good!
            }
        }

        fail("The line '" + linePattern[linePatternIdx] + "' was not found in the e-mail body [" + emailBody + "]");
    }

    /**
     * Asserts that the given email's body does not contain the bodySubString using indexOf.
     *
     * @param email email to extract the content body from
     * @param bodySubString string to not occur in body
     * @throws MessagingException Message error
     * @throws IOException IO error
     * @see GreenMailUtil#getBody(javax.mail.Part)
     */
    protected void assertEmailBodyDoesntContain(MimeMessage email, String bodySubString)
            throws MessagingException, IOException
    {
        final String emailBody = GreenMailUtil.getBody(email);
        assertTrue("The string '" + bodySubString + "' was found (shouldn't exist) in the e-mail body [" + emailBody + "]",
                !emailBody.contains(bodySubString));
    }

    /**
     * Assert that the String emailBody contains bodySubString
     *
     * @param emailBody body
     * @param bodySubString expected substring
     * @throws MessagingException message error
     * @throws IOException IO error
     */
    protected void assertEmailBodyContains(String emailBody, String bodySubString)
            throws MessagingException, IOException
    {
        assertTrue("Expected '" + bodySubString + "' to be present in email body '" + emailBody + "'", emailBody.contains(bodySubString));
    }

    protected void assertEmailHasNumberOfParts(MimeMessage email, int expectedNumOfParts)
            throws MessagingException, IOException
    {
        Object emailContent = email.getContent();
        if (emailContent instanceof Multipart)
        {
            Multipart multiPart = (Multipart) emailContent;
            assertEquals(expectedNumOfParts, multiPart.getCount());
        }
        else
        {
            fail("Cannot assert number of parts for email. Email is not a multipart type.");
        }
    }

    /**
     * Assert that the email was addressed to the expectedTo
     *
     * @param email email to assert the value of the to header
     * @param expectedTo the single or comma seperated list of expected email addresses
     * @throws MessagingException meesage error
     * @see #assertEmailToEquals(javax.mail.internet.MimeMessage, java.util.Collection)
     */
    protected void assertEmailToEquals(MimeMessage email, String expectedTo) throws MessagingException
    {
        assertEmailToEquals(email, parseEmailAddresses(expectedTo));
    }

    /**
     * Assert that the email was addressed to each and everyone of the expectedAddresses
     *
     * @param email email to assert the value of the to header
     * @param expectedToAddresses collection of expected email addresses
     * @throws MessagingException meesage error
     */
    protected void assertEmailToEquals(MimeMessage email, Collection expectedToAddresses) throws MessagingException
    {
        String[] toHeader = email.getHeader("to");
        assertEquals(1, toHeader.length);
        Collection actualAddresses = parseEmailAddresses(toHeader[0]);
        assertEmailsEquals(expectedToAddresses, actualAddresses);
    }

    protected void assertEmailCcEquals(MimeMessage email, Collection expectedCcAddresses) throws MessagingException
    {
        String[] ccHeader = email.getHeader("cc");
        if (ccHeader != null)
        {
            assertEquals(1, ccHeader.length);
            Collection actualAddresses = parseEmailAddresses(ccHeader[0]);
            assertEmailsEquals(expectedCcAddresses, actualAddresses);
        }
        else
        {
            //if there is no Cc header, assert that we were not expecting any emails.
            assertTrue("Expected Cc address but was null", expectedCcAddresses.isEmpty());
        }
    }

    private void assertEmailsEquals(Collection expectedAddresses, Collection actualAddresses)
    {
        assertEquals("Expected '" + expectedAddresses.size() + "' email addresses but only found '" + actualAddresses.size() + "'", expectedAddresses.size(), actualAddresses.size());
        assertEquals(expectedAddresses, actualAddresses);
    }

    protected void assertEmailFromEquals(MimeMessage email, String expectedTo) throws MessagingException
    {
        String[] addresses = email.getHeader("from");
        assertEquals(1, addresses.length);
        assertEquals(expectedTo, addresses[0]);
    }

    protected void assertEmailSubjectEquals(MimeMessage email, String subject) throws MessagingException
    {
        assertEquals(subject, email.getSubject());
    }

    protected void assertEmailSent(String recipient, String subject, String issueComment)
            throws MessagingException, IOException
    {
        final List emails = getMessagesForRecipient(recipient);
        assertEquals("Incorrect number of e-mails received for '" + recipient + "'", 1, emails.size());
        final MimeMessage emailMessage = (MimeMessage) emails.get(0);
        assertEmailBodyContains(emailMessage, issueComment);
        assertEmailSubjectEquals(emailMessage, subject);
    }

    protected void assertCorrectNumberEmailsSent(int numOfMessages)
            throws MessagingException
    {
        final MimeMessage[] messages = mailService.getReceivedMessages();
        if (messages.length != numOfMessages)
        {
            for (MimeMessage message : messages)
            {
                log("Mail sent to '" + message.getHeader("to")[0] + "' with SUBJECT '" + message.getSubject() + "'");
            }
            fail("Invalid number of e-mails received.  Was " + messages.length + " but should have been " + numOfMessages + ".");
        }
    }

    protected final MailBox getMailBox(String email) throws FolderException
    {
        return new MailBox(mailService.getUserInbox(email), email);
    }

    /**
     * Temporary helper method for JRADEV-23803
     * @param message
     * @return
     */
    private final String decodeQuotedPrintableBody(MimeMessage message)
    {
        String body = GreenMailUtil.getBody(message);

        try
        {
            final String[] headersArray = message.getHeader("Content-Transfer-Encoding");
            final Set<String> headers = Sets.newHashSet(headersArray == null ? new String[0] : headersArray);
            if (headers.contains("quoted-printable"))
            {
                InputStream inputStream = MimeUtility.decode(new ByteArrayInputStream(body.getBytes()), "quoted-printable");
                BufferedReader reader =  new BufferedReader(new InputStreamReader(inputStream));
                body = IOUtils.toString(reader);
            }
        }
        catch (MessagingException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return body;
    }

    protected final void assertMessageAndType(MimeMessage message, String expectedComment, boolean html)
    {
        // temporary fix for JRADEV-23803
        final String body = decodeQuotedPrintableBody(message);

        if (html)
        {
            assertThat(body, regexMatches(HTML_FORMAT_REGEX));
        }
        else
        {
            assertThat(body, regexMatchesNot(HTML_FORMAT_REGEX));
        }

        text.assertTextSequence(body, expectedComment);
    }

    protected final void assertNotMessageAndType(MimeMessage message, String expectedComment, boolean html)
    {
        final String body = GreenMailUtil.getBody(message);

        if (html)
        {
            assertThat(body, regexMatches(HTML_FORMAT_REGEX));
        }
        else
        {
            assertThat(body, regexMatchesNot(HTML_FORMAT_REGEX));
        }

        text.assertTextNotPresent(body, expectedComment);
    }

    protected final static class MailBox
    {
        private final MailFolder folder;
        private final String userEmail;
        private int pos;

        public MailBox(final MailFolder folder, final String userEmail)
        {
            this.folder = folder;
            this.userEmail = userEmail;
        }

        public MimeMessage nextMessage()
        {
            List<SimpleStoredMessage> messages = getMessages();
            if (pos >= messages.size())
            {
                return null;
            }
            else
            {
                return messages.get(pos++).getMimeMessage();
            }
        }

        private List<SimpleStoredMessage> getMessages()
        {
            return this.folder.getMessages();
        }

        public int size()
        {
            return this.folder.getMessageCount();
        }

        public void clear()
        {
            folder.deleteAllMessages();
            pos = 0;
        }

        public MimeMessage awaitMessage()
        {
            return awaitMessage(1000);
        }

        public MimeMessage awaitMessage(long timeout)
        {
            MimeMessage message = nextMessage();
            final long startTime = System.currentTimeMillis();
            if (message == null)
            {
                final long timeoutTime = timeout + startTime;
                long currentTime = startTime;
                while (currentTime < timeoutTime && message == null)
                {
                    //I hate this, but greenmail does not have any way to wait for a particular message. Arrrh.
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        break;
                    }

                    message = nextMessage();
                    currentTime = System.currentTimeMillis();
                }
            }

            if (message == null)
            {
                fail("Waited '" + (System.currentTimeMillis() - startTime) + "' ms for e-mail to '" + userEmail + "' but got nothing.");
            }
            return message;
        }
    }
}
