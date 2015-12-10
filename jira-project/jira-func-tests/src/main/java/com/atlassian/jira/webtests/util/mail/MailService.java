package com.atlassian.jira.webtests.util.mail;

import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.webtests.JIRAGreenMailSetup;
import com.atlassian.jira.webtests.JIRAServerSetup;
import com.icegreen.greenmail.AbstractServer;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.BindException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * This class provides mail services like POP3 and SMTP for the
 * acceptance tests. It will try to find free TCP/IP ports for the
 * services within a given range.
 */
public class MailService
{
    private GreenMail greenMail;
    private HashSet<GreenMailUser> greenMailUsers = new HashSet<GreenMailUser>();


    private FuncTestLogger log;

    public MailService(FuncTestLogger log)
    {
        this.log = log;
    }

    public GreenMail configureAndStartGreenMail(JIRAServerSetup... serverSetups) throws BindException
    {
        log.log("Configuring and starting JIRA green mail server");

        JIRAGreenMailSetup jiraGreenMailSetup = new JIRAGreenMailSetup(serverSetups);

        for (int setupRetries = 0; setupRetries < 10; setupRetries++)
        {
            greenMail = tryStartingGreenMail(jiraGreenMailSetup.getServerSetups());
            if (greenMail != null)
            {
                log.log("Successfully started green mail server");
                return greenMail;
            }

            log.log("Some servers did not start properly. Incrementing ports and trying again... '" + setupRetries + "'");
            jiraGreenMailSetup.incrementPorts();
        }

        throw new BindException("Unable to start GreenMail server due to port conflicts.");
    }

    private GreenMail tryStartingGreenMail(ServerSetup[] serverSetups)
    {
        // 1. Start up a new GreenMail instance
        GreenMail greenMail = new GreenMail(serverSetups);
        greenMail.start();

        try
        {
            //wait for the servers to start up
            Thread.sleep(100);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        // 2. For each mail server added, check if any has failed to start.
        List<AbstractServer> servers = new ArrayList<AbstractServer>();
        servers.add(greenMail.getSmtp());
        servers.add(greenMail.getPop3());
        servers.add(greenMail.getImap());

        for (AbstractServer server : servers)
        {
            if (server != null && !isServerRunning(server))
            {
                greenMail.stop();
                return null;
            }
        }

        return greenMail;
    }

    /**
     * Checks if the server is not null that it is alive and running.
     *
     * @param server abstract server
     * @return true if the server thread is alive, false otherwise
     */
    private boolean isServerRunning(AbstractServer server)
    {
        if (!server.isAlive())
        {
            log.log("Error trying to start '" + server.getProtocol() + "' server on port '" + server.getPort() + "'.");
            return false;
        }

        log.log("Running '" + server.getProtocol() + "' server on port '" + server.getPort() + "'.");
        return true;
    }

    /**
     * Stops the SMTP and POP3 server.
     */
    public void stop()
    {
        if (isRunning())
        {
            greenMail.stop();
            greenMail = null;
        }

    }

    /**
     * Returns true if the mail servers are running.
     */
    public boolean isRunning()
    {
        return greenMail != null;
    }

    /**
     * Adds a user to the mail service. The SMTP server will then accept incoming mails for email and you can
     * fetch these mails via POP3 using login/password for authentication.
     *
     * @param email email of the user to be created
     * @param login login of the user to be created
     * @param password password of the user to be created
     */
    public void addUser(String email, String login, String password)
    {
        try
        {
            greenMailUsers.add(greenMail.getManagers().getUserManager().createUser(email, login, password));
        }
        catch (UserException e)
        {
            throw new RuntimeException("Unable to create user with email '" + email + "'", e);
        }
    }

    /**
     * @return the port the POP3 server is bound to
     */
    public int getPop3Port()
    {
        return greenMail.getPop3().getPort();
    }

    /**
     * @return the port the SMTP server is bound to
     */
    public int getSmtpPort()
    {
        return greenMail.getSmtp().getPort();
    }

    /**
     * @return the port the IMAP server is bound to
     */
    public int getImapPort()
    {
        return greenMail.getImap().getPort();
    }

    /**
     * Sends an email with the content type "text/plain" and the given contents using the test SMTP server.
     *
     * @param to email address this mail should be sent to
     * @param from email address this mail should appear to be sent from
     * @param subject subject of the email
     * @param body body of the email
     */
    public void sendTextMessage(String to, String from, String subject,
            String body)
    {
        sendTextEmail(to, from, subject, body, greenMail.getSmtp().getServerSetup());

        try
        {
            greenMail.waitForIncomingEmail(1);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Interrupted while waiting for the sent mail to arrive", e);
        }
    }

    /**
     * Returns an array of all messages received until now.
     *
     * @return all received messages
     */
    public MimeMessage[] getReceivedMessages()
    {
        return greenMail.getReceivedMessages();
    }

    /**
     * Returns an array of all messages received until now, clearing the list on the mail server.
     *
     * @return all received messages
     * @throws com.icegreen.greenmail.store.FolderException if there's a problem clearing the list
     */
    public MimeMessage[] getReceivedMessagesAndClear() throws FolderException
    {
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        removeAllReceivedMessages();
        return receivedMessages;
    }

    /**
     * Get the first message of all messages received until now.
     *
     * @return the first message received as MimeMessage or null if there was no message received
     */
    public MimeMessage getReceivedMessage()
    {
        MimeMessage[] receivedMail = getReceivedMessages();
        if (receivedMail.length == 0)
        {
            return null;
        }
        return receivedMail[0];
    }

    /**
     * Does the same as {@link #getReceivedMessage()} but waits until a message is received or until a timeout is reached.
     *
     * @return the first message received as MimeMessage or null if there was no message received
     * @throws InterruptedException
     */
    public MimeMessage waitAndGetReceivedMessage() throws InterruptedException
    {
        //Increased to 10 seconds as it would intermittently not return result with parallel builds
        greenMail.waitForIncomingEmail(10000, 1);
        return getReceivedMessage();
    }

    /**
     * Get the {@link com.icegreen.greenmail.imap.ImapConstants#INBOX_NAME inbox} for the user with email userEmail.
     * NOTE: if the user does not exist, one will be created.
     *
     * @param userEmail the email address of the user
     * @return MailFolder of the user with the given email address
     * @throws FolderException if the user doesn't have an inbox on this server
     */
    public MailFolder getUserInbox(String userEmail) throws FolderException
    {
        GreenMailUser mailUser = greenMail.setUser(userEmail, "password");
        return greenMail.getManagers().getImapHostManager().getInbox(mailUser);
    }

    /**
     * Removes all received mails.
     *
     * @throws FolderException
     */
    public void removeAllReceivedMessages() throws FolderException
    {
        for (GreenMailUser user : greenMailUsers)
        {
            greenMail.getManagers().getImapHostManager().getInbox(user).deleteAllMessages();
        }
    }

    /**
     * Wait until the specified number of mails is received or until 10 seconds have passed.
     *
     * @throws InterruptedException
     */
    public boolean waitForIncomingMessage(int numberOfMessages) throws InterruptedException
    {
        return waitForIncomingMessage(10000, numberOfMessages);
    }

    /**
     * Wait until the specified number of mails is received or until a timeout is reached.
     *
     * @throws InterruptedException
     */
    public boolean waitForIncomingMessage(long timeout, int numberOfMessages) throws InterruptedException
    {
        return greenMail.waitForIncomingEmail(timeout, numberOfMessages);
    }

    /* FROM GREENMAIL */

    private static void sendTextEmail(String to, String from, String subject, String msg, final ServerSetup setup) {
        try {
            Session session = getSession(setup);

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setSubject(subject);
            mimeMessage.setFrom(new InternetAddress(from));

            mimeMessage.setText(msg);
            Transport.send(mimeMessage, new InternetAddress[]{new InternetAddress(to)});
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static Session getSession(final ServerSetup setup) {
        Properties props = new Properties();
        props.put("mail.smtps.starttls.enable", Boolean.TRUE);
        if (setup.isSecure()) {
            props.setProperty("mail.smtp.socketFactory.class", DummySSLSocketFactory.class.getName());
        }
        props.setProperty("mail.transport.protocol", setup.getProtocol());
        props.setProperty("mail.smtp.host", String.valueOf(setup.getBindAddress()));
        props.setProperty("mail.smtp.port", String.valueOf(setup.getPort()));
        props.setProperty("mail.smtps.port", String.valueOf(setup.getPort()));
        return Session.getInstance(props, null);
    }
}