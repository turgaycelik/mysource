package com.atlassian.jira.event.listeners.mail;

import java.lang.reflect.Method;
import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.issue.DefaultIssueEventBundle;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.JiraIssueEvent;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mail.UserMailQueueItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtilImpl;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.mail.queue.MailQueue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TestMailListener
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock
    private MailQueue mailQueue;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private IssueEventBundleMailHandler issueEventBundleMailHandler;

    @Mock
    private UserManager userManager;

    @Mock
    private IssueEventBundleFactory issueEventBundleFactory;

    private final MockApplicationUser user = new MockApplicationUser("user1");

    private MailListener mailListener;

    @Before
    public void setUp() throws Exception
    {
        mailListener = new MailListener(userManager, issueEventBundleMailHandler, mailQueue, issueEventBundleFactory);
    }

    @Test
    public void isInternal()
    {
        assertTrue(mailListener.isInternal());
    }

    @Test
    public void isUnique()
    {
        assertTrue(mailListener.isUnique());
    }

    @Test
    public void anEmailNotificationShouldBeEnqueuedGivenAUserHasSignedUpForAJiraAccount() throws Exception
    {
        final UserEvent userEvent = new UserEvent(user.getDirectoryUser(), UserEventType.USER_SIGNUP);
        userEvent.getParams().put(UserUtilImpl.SEND_EMAIL, true);

        mailListener.userSignup(userEvent);

        verifyAUserMailQueueItemHasBeenAdded(mailQueue, "Account signup", "template.user.signup.subject");
    }

    @Test
    public void anEmailIsSentWhenAUserRequestsANewPassword() throws Exception
    {
        final UserEvent userEvent = new UserEvent(user.getDirectoryUser(), UserEventType.USER_SIGNUP);

        mailListener.userForgotPassword(userEvent);

        verifyAUserMailQueueItemHasBeenAdded(mailQueue, "Account password", "template.user.forgotpassword.subject");
    }

    @Test
    public void anEmailIsSentWhenToTheUserWhenAnAccountHasBeenCreatedForItGivenThatTheUserIsAllowedToChangeThePassword()
            throws Exception
    {
        final UserEvent userEvent = new UserEvent(user.getDirectoryUser(), UserEventType.USER_SIGNUP);
        userEvent.getParams().put(UserUtilImpl.SEND_EMAIL, true);
        when(userManager.canUpdateUserPassword(user.getDirectoryUser())).thenReturn(true);

        mailListener.userCreated(userEvent);

        verifyAUserMailQueueItemHasBeenAdded(mailQueue, "Account created", "template.user.created.subject");
    }

    @Test
    public void anEmailIsSentWhenToTheUserWhenAnAccountHasBeenCreatedForItGivenThatTheUserIsNotAllowedToChangeThePassword()
            throws Exception
    {
        final UserEvent userEvent = new UserEvent(user.getDirectoryUser(), UserEventType.USER_SIGNUP);
        userEvent.getParams().put(UserUtilImpl.SEND_EMAIL, true);
        when(userManager.canUpdateUserPassword(user.getDirectoryUser())).thenReturn(false);

        mailListener.userCreated(userEvent);

        verifyAUserMailQueueItemHasBeenAdded(mailQueue, "Account created", "template.user.created.subject");
    }

    @Test
    public void handleIssueEventBundleDelegatesToTheAppropriateHandler()
    {
        IssueEventBundle bundle = DefaultIssueEventBundle.create(Collections.<JiraIssueEvent>emptyList());

        mailListener.handleIssueEventBundle(bundle);

        verify(issueEventBundleMailHandler).handle(bundle);
    }

    @Test
    public void testIfUserCreatedNotificationIsNotSentWhenNotificationsAreDisabled() {
        boolean enabled = ImportUtils.isEnableNotifications();
        try {
            ImportUtils.setEnableNotifications(false);

            UserEvent userEvent = new UserEvent(mock(User.class), UserEventType.USER_CREATED);
            userEvent.getParams().put(UserUtilImpl.SEND_EMAIL, true);
            mailListener.userCreated(userEvent);

            verifyZeroInteractions(mailQueue);
        } finally {
            ImportUtils.setEnableNotifications(enabled);
        }
    }

    @Test
    public void testIfUserCreatedNotificationIsSentWhenNotificationsAreEnabled() {
        boolean enabled = ImportUtils.isEnableNotifications();
        try {
            ImportUtils.setEnableNotifications(true);

            UserEvent userEvent = new UserEvent(mock(User.class), UserEventType.USER_CREATED);
            userEvent.getParams().put(UserUtilImpl.SEND_EMAIL, true);
            mailListener.userCreated(userEvent);

            verify(mailQueue).addItem(any(UserMailQueueItem.class));
        } finally {
            ImportUtils.setEnableNotifications(enabled);
        }
    }
    
    @Test
    public void handleDefaultIssueEventAdaptsIssueEventIntoBundleAndDelegatesToIssueEventBundleMailHandler()
    {
        IssueEvent event = issueEvent();

        IssueEventBundle bundle = mock(IssueEventBundle.class);
        when(issueEventBundleFactory.wrapInBundle(event)).thenReturn(bundle);

        mailListener.handleDefaultIssueEvent(event);

        verify(issueEventBundleMailHandler).handle(bundle);
    }

    @Test
    public void handleDefaultIssueEventIgnoresEventIfItIsRedundant() throws Exception
    {
        IssueEvent event = redundantIssueEvent();

        mailListener.handleDefaultIssueEvent(event);

        verify(issueEventBundleMailHandler, never()).handle(any(IssueEventBundle.class));
    }

    private IssueEvent issueEvent()
    {
        return new IssueEvent(null, null, null, null);
    }

    private IssueEvent redundantIssueEvent() throws Exception
    {
        IssueEvent event = issueEvent();
        Method method = IssueEvent.class.getDeclaredMethod("makeRedundant");
        method.setAccessible(true);
        method.invoke(event);
        return event;
    }

    private void verifyAUserMailQueueItemHasBeenAdded(MailQueue mailQueue, String subject, String subjectKey)
    {
        ArgumentCaptor<UserMailQueueItem> argumentCaptor = ArgumentCaptor.forClass(UserMailQueueItem.class);
        verify(mailQueue).addItem(argumentCaptor.capture());

        assertEquals(subject, argumentCaptor.getValue().getSubject());
        assertEquals(subjectKey, argumentCaptor.getValue().getSubjectKey());
    }
}
