package com.atlassian.jira.webtests.util.mail;

import com.atlassian.jira.testkit.client.log.FuncTestLoggerImpl;
import com.atlassian.jira.webtests.JIRAServerSetup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.BindException;

/**
 * A little utility originally written by Chris K (in Confluence) - provides a simple UI for starting and
 * stopping a test mail server. Useful for manually testing JIRA notification features.
 *
 * Features:
 * - SMTP / IMAP servers
 * - Test message sender
 * - a default admin mail account
 * - and much more! No, that's about it.
 */
public class SwingMail extends JFrame
{
    private static final String DEFAULT_EMAIL_ADDRESS = "admin@example.com";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    private static final String POP3_PORT_LABEL_PREFIX = "POP3 port: ";
    private static final String SMTP_PORT_LABEL_PREFIX = "SMTP port: ";
    private static final String ACCOUNT_LABEL_PREFIX = "Account: ";

    private MailService mailService;
    private JLabel pop3PortLabel;
    private JLabel smtpPortLabel;
    private AbstractButton startButton;
    private AbstractButton stopButton;
    private AbstractButton testMessageButton;

    public SwingMail(String title) throws HeadlessException
    {
        super(title);

        Container pane = this.getContentPane();
        pane.setLayout(new FlowLayout());

        Container labelPane = new JPanel();
        labelPane.setPreferredSize(new Dimension(315, 60));

        pop3PortLabel = new JLabel();
        smtpPortLabel = new JLabel();
        JLabel accountLabel = new JLabel();
        accountLabel.setText( ACCOUNT_LABEL_PREFIX + DEFAULT_EMAIL_ADDRESS + "/" + DEFAULT_USERNAME + "/" + DEFAULT_PASSWORD);
        labelPane.add(pop3PortLabel);
        labelPane.add(smtpPortLabel);
        labelPane.add(accountLabel);

        startButton = new JButton();
        stopButton = new JButton();
        testMessageButton = new JButton();
        pane.add(startButton);
        pane.add(stopButton);
        pane.add(testMessageButton);

        pane.add(labelPane);
        addHandlers();
        syncUI();
        this.pack();
        this.setVisible(true);
        System.out.println("Ready.");
    }

    private void addHandlers()
    {
        startButton.setAction(new AbstractAction("Start") {
            public void actionPerformed(ActionEvent e)
            {
                startService();
            }
        });
        stopButton.setAction(new AbstractAction("Stop") {
            public void actionPerformed(ActionEvent e)
            {
                stopService();
            }
        });
        testMessageButton.setAction(new AbstractAction("Send Test Message") {
            public void actionPerformed(ActionEvent e)
            {
                sendTestMessage();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
                shutdown();
            }
        });
    }

    private void shutdown()
    {
        stopService();
        System.out.println("Shutting down...");
        System.exit(0);
    }

    private void sendTestMessage()
    {
        if (mailService != null)
        {
            final String subject = "SwingMail Test Message";
            String body = "Hi, I'm a test message from your mail service interface. " +
                            "If you're reading me your mail setup is good.";
            mailService.sendTextMessage(DEFAULT_EMAIL_ADDRESS, DEFAULT_EMAIL_ADDRESS, subject, body);
            System.out.println("Test message sent to " + DEFAULT_EMAIL_ADDRESS + ".");
        }
    }

    private void startService()
    {
        try
        {
            mailService = new MailService(new FuncTestLoggerImpl());
            mailService.configureAndStartGreenMail(JIRAServerSetup.SMTP_POP3);
            mailService.addUser(DEFAULT_EMAIL_ADDRESS, DEFAULT_USERNAME, DEFAULT_PASSWORD);
            syncUI();
            System.out.println("Mail Service Started.");
        }
        catch (BindException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    private void stopService()
    {
        if (mailService != null)
        {
            mailService.stop();
            syncUI();
            System.out.println("Mail Service Stopped.");
        }
    }

    private void syncUI()
    {
        boolean started = mailService != null && mailService.isRunning();

        pop3PortLabel.setText(POP3_PORT_LABEL_PREFIX + (started ? mailService.getPop3Port() : ""));
        smtpPortLabel.setText(SMTP_PORT_LABEL_PREFIX + (started ? mailService.getSmtpPort() : ""));
        startButton.setEnabled(!started);
        stopButton.setEnabled(started);
        testMessageButton.setEnabled(started);
    }

    public static void main(String[] args)
    {
        new SwingMail("mail");
    }
}