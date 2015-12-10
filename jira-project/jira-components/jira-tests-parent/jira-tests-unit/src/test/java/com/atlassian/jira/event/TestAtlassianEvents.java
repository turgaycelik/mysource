package com.atlassian.jira.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.internal.AsynchronousAbleEventDispatcher;
import com.atlassian.event.internal.DirectEventExecutorFactory;
import com.atlassian.event.internal.EventPublisherImpl;
import com.atlassian.event.internal.EventThreadPoolConfigurationImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventListener;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.jira.event.user.UserEventType.USER_CREATED;
import static com.atlassian.jira.event.user.UserEventType.USER_FORGOTPASSWORD;
import static com.atlassian.jira.event.user.UserEventType.USER_FORGOTUSERNAME;
import static com.atlassian.jira.event.user.UserEventType.USER_SIGNUP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestAtlassianEvents
{
    private EventPublisher eventPublisher;
    @Mock private JiraAuthenticationContext mockJiraAuthenticationContext;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init().addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext);
        eventPublisher = new EventPublisherImpl(
                new AsynchronousAbleEventDispatcher(
                        new DirectEventExecutorFactory(
                                new EventThreadPoolConfigurationImpl())),
                new JiraListenerHandlerConfigurationImpl());
    }

    @After
    public void tearDown() throws Exception
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testIssueEventCorrectlyInvoked() throws Exception
    {
        // Set up
        final IssueEvent issueEvent = new IssueEvent(null, null, null, 1L);
        final IssueEventListener listener = mock(IssueEventListener.class);
        eventPublisher.register(listener);

        // Invoke
        eventPublisher.publish(issueEvent);

        // Check
        verify(listener).workflowEvent(issueEvent);
    }

    @Test
    public void testUserSignupEventCorrectlyInvoked() throws Exception
    {
        // Set up
        final UserEvent userEvent = new UserEvent(null, USER_SIGNUP);
        final UserEventListener listener = mock(UserEventListener.class);
        eventPublisher.register(listener);

        // Invoke
        eventPublisher.publish(userEvent);

        // Check
        verify(listener).userSignup(userEvent);
    }

    @Test
    public void testUserCreatedEventCorrectlyInvoked() throws Exception
    {
        // Set up
        final UserEvent userEvent = new UserEvent(null, USER_CREATED);
        final UserEventListener listener = mock(UserEventListener.class);
        eventPublisher.register(listener);

        // Invoke
        eventPublisher.publish(userEvent);

        // Check
        verify(listener).userCreated(userEvent);
    }

    @Test
    public void testUserForgetPasswordEventCorrectlyInvoked() throws Exception
    {
        // Set up
        final UserEvent userEvent = new UserEvent(null, USER_FORGOTPASSWORD);
        final UserEventListener listener = mock(UserEventListener.class);
        eventPublisher.register(listener);

        // Invoke
        eventPublisher.publish(userEvent);

        // Check
        verify(listener).userForgotPassword(userEvent);
    }

    @Test
    public void testUserForgotUsernameEventCorrectlyInvoked() throws Exception
    {
        // Set up
        final UserEvent userEvent = new UserEvent(null, USER_FORGOTUSERNAME);
        final UserEventListener listener = mock(UserEventListener.class);
        eventPublisher.register(listener);

        // Invoke
        eventPublisher.publish(userEvent);

        // Check
        verify(listener).userForgotUsername(userEvent);
    }
}
