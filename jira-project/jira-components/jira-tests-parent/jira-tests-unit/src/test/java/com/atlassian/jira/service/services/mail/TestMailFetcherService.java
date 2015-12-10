package com.atlassian.jira.service.services.mail;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.mail.settings.MailSettings;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.atlassian.jira.service.util.handler.MessageHandlerFactory;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;

import com.google.common.base.Optional;
import com.opensymphony.module.propertyset.PropertySet;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

public class TestMailFetcherService
{
    private static final String MESSAGE_ID = "contentId-18877260-9924014896@localhost";
    public static final String HOSTNAME = "hostname";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    private static final int PORT = 143;

    @Mock
    PropertySet props;

    @Mock
    MailServerManager mailServerManager;

    @Mock
    MessageHandlerContext context;

    @Mock
    MessageHandler handler;

    @Mock
    Message problematicMessage;

    @Mock
    Message newMessage;

    @Mock
    Folder folder;

    @Mock
    Store store;

    @AvailableInContainer
    @Mock
    MailThreadManager mailThreadManager;

    @Mock
    MessageHandlerExecutionMonitor monitor;

    @AvailableInContainer
    @Mock
    MailSettings mailSettings;

    @Mock
    MailSettings.Fetch fetch;

    @AvailableInContainer
    @Mock
    MailLoggingManager loggingManager;

    @AvailableInContainer
    @Mock
    MessageHandlerFactory messageHandlerFactory;

    @Mock
    Logger logger;

    @Mock
    MailServer mailServer;

    @Mock
    SMTPMailServer smtpMailServer;

    @AvailableInContainer
    private I18nHelper.BeanFactory beanFactory = new NoopI18nFactory();

    @Mock
    @AvailableInContainer
    JiraApplicationContext jiraApplicationContext;

    @Mock (answer = Answers.RETURNS_MOCKS)
    @AvailableInContainer
    VelocityTemplatingEngine velocityTemplatingEngine;

    @Rule
    public RuleChain mockItAll = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        when(context.getMonitor()).thenReturn(monitor);
        when(mailSettings.fetch()).thenReturn(fetch);

        when(loggingManager.getIncomingMailChildLogger(anyString())).thenReturn(logger);

        when(fetch.isDisabled()).thenReturn(false);

        when(props.getString("handler")).thenReturn("handler");
        when(messageHandlerFactory.getHandler("handler")).thenReturn(handler);

        when(props.exists("popserver")).thenReturn(true);
        when(props.getString("popserver")).thenReturn("1");

        when(mailServerManager.getMailServer(1L)).thenReturn(mailServer);
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        when(mailServer.getHostname()).thenReturn(HOSTNAME);
        when(mailServer.getUsername()).thenReturn(USERNAME);
        when(mailServer.getPassword()).thenReturn(PASSWORD);

        when(mailServer.getMailProtocol()).thenReturn(MailProtocol.IMAP);
        when(mailServer.getPort()).thenReturn(Integer.toString(PORT));

        when(context.isRealRun()).thenReturn(true);

        when(store.getFolder("INBOX")).thenReturn(folder);
        when(folder.search(any(FlagTerm.class))).thenReturn(new Message[] { problematicMessage });
        when(problematicMessage.getHeader(argThat(equalToIgnoringCase("Message-id")))).thenReturn(new String[] { MESSAGE_ID });
        when(newMessage.getHeader(argThat(equalToIgnoringCase("Message-id")))).thenReturn(new String[] { "abc" });

    }

    /**
     * This cover scenario when a mail with a large attachment is processed. CreateIssueHandler will create an issue
     * first and then it will proceed with downloading attachments during which it will fail with a
     * FolderClosedIOException. At the end the handler returns true indicating that the problematicMessage has been
     * processed and can be deleted but when we attempt to do it a FolderClosedException is thrown (due to previous
     * error in attachment processing). This problematic email will be left in the mailbox and the next time we process
     * email a duplicate issue will be created and we don't want that.
     */
    @Test
    public void problematicMailSkippedInSubsequentRuns() throws Exception
    {
        when(handler.handleMessage(any(Message.class), any(MessageHandlerContext.class))).thenReturn(true);
        doThrow(new FolderClosedException(folder)).doNothing().when(problematicMessage).setFlag(Flags.Flag.DELETED, true);
        Issue mockIssue = mock(Issue.class);
        when(mockIssue.getKey()).thenReturn("TST-101");
        when(mailThreadManager.findIssueFromMessageId(MESSAGE_ID)).thenReturn(null).thenReturn(mockIssue).thenReturn(null);

        MailFetcherService service = createMailFetcherService();
        service.init(props);
        service.runImpl(context);

        // assume a new message has arrived after run 1 has completed
        when(folder.search(any(FlagTerm.class))).thenReturn(new Message[] { problematicMessage, newMessage });

        service.runImpl(context);

        InOrder inOrder = inOrder(store, handler, problematicMessage, newMessage);
        // 1st run - problematicMessage is processed and we attempt to delete it but this fails with a FolderClosedException
        inOrder.verify(store).connect(HOSTNAME, PORT, USERNAME, PASSWORD);
        inOrder.verify(handler).handleMessage(eq(problematicMessage), any(MessageHandlerContext.class));
        inOrder.verify(problematicMessage).setFlag(eq(Flags.Flag.DELETED), eq(true));
        // 2nd run - problematicMessage is skipped and message is deleted
        inOrder.verify(store).connect(HOSTNAME, PORT, USERNAME, PASSWORD);
        inOrder.verify(handler, never()).handleMessage(eq(problematicMessage), any(MessageHandlerContext.class));
        inOrder.verify(problematicMessage).setFlag(eq(Flags.Flag.DELETED), eq(true));
        // but the new message is processed as usual
        inOrder.verify(handler).handleMessage(eq(newMessage), any(MessageHandlerContext.class));
        inOrder.verify(newMessage).setFlag(eq(Flags.Flag.DELETED), eq(true));
    }

    /**
     * When mail handler encounters a failure while processing attachments (typically a FolderClosedIOException) the whole communication
     * to mail server is lost. An attempt to forward this email (if configured) will also fail but this should not prevent
     * the message from being deleted if the handler has used {@link MessageHandlerExecutionMonitor#markMessageForDeletion(String)}
     */
    @Test
    public void markMessageForDeletionRespectedOnForwardEmailFailure() throws Exception
    {
        doThrow(new MailException()).when(smtpMailServer).send(any(Email.class));
        when(handler.handleMessage(eq(problematicMessage), any(MessageHandlerContext.class))).thenAnswer(new Answer<Boolean>()
        {
            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable
            {
                Object[] args = invocation.getArguments();
                MessageHandlerContext context = (MessageHandlerContext) args[1];
                // return false but mark the message for deletion
                context.getMonitor().markMessageForDeletion("Some reason");
                return false;
            }
        });
        when(mailThreadManager.findIssueFromMessageId(MESSAGE_ID)).thenReturn(null);
        when(props.getString("forwardEmail")).thenReturn("dev@null");

        MailFetcherService service = createMailFetcherService();
        service.init(props);
        service.runImpl(context);

        InOrder inOrder = inOrder(store, handler, problematicMessage, monitor);
        inOrder.verify(store).connect(HOSTNAME, PORT, USERNAME, PASSWORD);
        inOrder.verify(handler).handleMessage(eq(problematicMessage), any(MessageHandlerContext.class));
        inOrder.verify(monitor).markMessageForDeletion(anyString());
        inOrder.verify(problematicMessage).setFlag(eq(Flags.Flag.DELETED), eq(true));
    }

    private MailFetcherService createMailFetcherService()
    {
        return new MailFetcherService()
        {
            @Override
            MailServerManager getMailServerManager()
            {
                return mailServerManager;
            }

            @Override
            Optional<Store> getStore(final MailServer mailServer, final String protocol, final MessageHandlerExecutionMonitor monitor)
            {
                return Optional.of(store);
            }
        };
    }

}
