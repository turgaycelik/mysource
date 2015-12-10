package com.atlassian.jira.event.listeners.mail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.event.issue.DefaultIssueEventBundle;
import com.atlassian.jira.event.issue.DelegatingJiraIssueEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.JiraIssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeEntity;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestIssueEventBundleMailHandlerImpl
{
    @Mock
    private NotificationSchemeManager notificationSchemeManager;
    @Mock
    private IssueEventMailNotifier mailNotifier;

    private IssueEventBundleMailHandler eventBundleMailHandler;

    @Before
    public void setUp()
    {
        eventBundleMailHandler = new IssueEventBundleMailHandlerImpl(notificationSchemeManager, mailNotifier);
    }

    @Test
    public void noEmailsAreSentForEventBundlesThatShouldBeIgnored()
    {
        IssueEventBundle eventBundle = DefaultIssueEventBundle.createDisallowingEmailNotifications(someEvents());

        eventBundleMailHandler.handle(eventBundle);

        verify(mailNotifier, never()).generateNotifications(anyListOf(SchemeEntity.class), any(IssueEvent.class), anySetOf(NotificationRecipient.class));
    }

    @Test
    public void noEmailsAreSentWhenTheEventBundleHasNoEventsOnIt()
    {
        IssueEventBundle eventBundle = DefaultIssueEventBundle.create(Collections.<JiraIssueEvent>emptyList());

        eventBundleMailHandler.handle(eventBundle);

        verify(mailNotifier, never()).generateNotifications(anyListOf(SchemeEntity.class), any(IssueEvent.class), anySetOf(NotificationRecipient.class));
    }
    
    @Test
    public void noEmailsAreSentWhenTheEventBundleContainsAnIssueEventThatShouldNotBeSentByEmail() throws Exception
    {
        JiraIssueEvent event = eventNotAllowedToBeSendByEmail();
        IssueEventBundle eventBundle = DefaultIssueEventBundle.create(Arrays.asList(event));

        when(notificationSchemeManager.getNotificationSchemeEntities(any(Project.class), anyLong())).thenReturn(someSchemeEntities());

        eventBundleMailHandler.handle(eventBundle);

        verify(mailNotifier, never()).generateNotifications(anyListOf(SchemeEntity.class), any(IssueEvent.class), anySetOf(NotificationRecipient.class));
    }

    @Test
    public void emailsAreCorrectlyGeneratedWhenTheBundleHasJustOneEventOnIt() throws Exception
    {
        Project project = mock(Project.class);
        Long eventTypeId = EventType.ISSUE_ASSIGNED_ID;

        DelegatingJiraIssueEvent event = eventWith(project, eventTypeId);
        IssueEventBundle eventBundle = DefaultIssueEventBundle.create(Arrays.asList(event));

        List<SchemeEntity> schemeEntities = someSchemeEntities();
        when(notificationSchemeManager.getNotificationSchemeEntities(project, eventTypeId)).thenReturn(schemeEntities);

        eventBundleMailHandler.handle(eventBundle);

        verify(mailNotifier).generateNotifications(schemeEntities, event.asIssueEvent(), Collections.<NotificationRecipient>emptySet());
    }
    
    @Test
    public void emailsAreCorrectlyGeneratedForAllTheEventsInTheBundleAndTheListOfUsersThatHaveAlreadyReceivedAnEmailIsKeptUpToDate() throws Exception
    {
        Project project = mock(Project.class);
        when(notificationSchemeManager.getNotificationSchemeEntities(any(Project.class), anyLong())).thenReturn(someSchemeEntities());

        DelegatingJiraIssueEvent event1 = eventWith(project, EventType.ISSUE_ASSIGNED_ID);
        DelegatingJiraIssueEvent event2 = eventWith(project, EventType.ISSUE_COMMENTED_ID);
        DelegatingJiraIssueEvent event3 = eventWith(project, EventType.ISSUE_UPDATED_ID);
        IssueEventBundle eventBundle = DefaultIssueEventBundle.create(Arrays.asList(event1, event2, event3));

        IssueEventMailNotifierSpy mailNotifier = new IssueEventMailNotifierSpy();
        NotificationRecipient recipient1 = new NotificationRecipient("1");
        NotificationRecipient recipient2 = new NotificationRecipient("2");
        NotificationRecipient recipient3 = new NotificationRecipient("3");
        NotificationRecipient recipient4 = new NotificationRecipient("4");
        NotificationRecipient recipient5 = new NotificationRecipient("5");
        NotificationRecipient recipient6 = new NotificationRecipient("6");

        mailNotifier.setRecipientsForEvent(event1.asIssueEvent(), recipient1, recipient2);
        mailNotifier.setRecipientsForEvent(event2.asIssueEvent(), recipient3, recipient4);
        mailNotifier.setRecipientsForEvent(event3.asIssueEvent(), recipient5, recipient6);

        eventBundleMailHandler = new IssueEventBundleMailHandlerImpl(notificationSchemeManager, mailNotifier);
        eventBundleMailHandler.handle(eventBundle);

        // no recipients should be skipped for the first event processed
        assertThat(mailNotifier.getRecipientsSkippedForEvent(event1.asIssueEvent()).size(), is(0));
        // the recipients of the mails for the first event must be skipped when generating the emails for the second email
        assertThat(mailNotifier.getRecipientsSkippedForEvent(event2.asIssueEvent()), containsInAnyOrder(recipient1, recipient2));
        // the recipients of the mails for the first and second events must be skipped when generating the emails for the third email
        assertThat(mailNotifier.getRecipientsSkippedForEvent(event3.asIssueEvent()), containsInAnyOrder(recipient1, recipient2, recipient3, recipient4));
    }

    private List<JiraIssueEvent> someEvents()
    {
        return Arrays.asList(mock(JiraIssueEvent.class));
    }

    private List<SchemeEntity> someSchemeEntities()
    {
        return Arrays.asList(mock(SchemeEntity.class));
    }

    private DelegatingJiraIssueEvent eventWith(Project project, Long eventTypeId)
    {
        Issue issue = mock(Issue.class);
        when(issue.getProjectObject()).thenReturn(project);
        IssueEvent issueEvent = new IssueEvent(issue, null, null, eventTypeId);
        DelegatingJiraIssueEvent event = mock(DelegatingJiraIssueEvent.class);
        when(event.asIssueEvent()).thenReturn(issueEvent);

        return event;
    }

    private JiraIssueEvent eventNotAllowedToBeSendByEmail()
    {
        IssueEvent issueEventNotAllowedToBeSentByEmail = new IssueEvent(mock(Issue.class), null, null, EventType.ISSUE_ASSIGNED_ID, false);
        DelegatingJiraIssueEvent event = mock(DelegatingJiraIssueEvent.class);
        when(event.asIssueEvent()).thenReturn(issueEventNotAllowedToBeSentByEmail);
        return event;
    }

    private static class IssueEventMailNotifierSpy implements IssueEventMailNotifier
    {
        private Map<IssueEvent, Set<NotificationRecipient>> recipientsForEvents = new HashMap<IssueEvent, Set<NotificationRecipient>>();
        private Map<IssueEvent, Set<NotificationRecipient>> recipientsSkippedForEvents = new HashMap<IssueEvent, Set<NotificationRecipient>>();

        public void setRecipientsForEvent(IssueEvent issueEvent, NotificationRecipient... recipients)
        {
            recipientsForEvents.put(issueEvent, ImmutableSet.copyOf(Arrays.asList(recipients)));
        }

        public Set<NotificationRecipient> getRecipientsSkippedForEvent(IssueEvent issueEvent)
        {
            return this.recipientsSkippedForEvents.get(issueEvent);
        }

        @Override
        @Nonnull
        public Set<NotificationRecipient> generateNotifications(@Nonnull final List<SchemeEntity> schemeEntities, @Nonnull final IssueEvent issueEvent, @Nonnull final Set<NotificationRecipient> recipientsToSkip)
        {
            recipientsSkippedForEvents.put(issueEvent, ImmutableSet.copyOf(recipientsToSkip));
            return recipientsForEvents.get(issueEvent);
        }
    }
}
