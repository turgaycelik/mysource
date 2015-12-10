package com.atlassian.jira.event.issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link com.atlassian.jira.event.issue.DefaultIssueEventManager}.
 *
 * @since v4.4
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultIssueEventManager
{
    private static final String DEFAULT_BASE_URL = "http://jira.atlassian.com";

    @Mock
    private IssueEventParamsTransformer paramsTransformer;

    private EventPublisherSpy eventPublisher;

    private DefaultIssueEventManagerWithNotificationsSetup issueEventManager;

    @Before
    public void setUp()
    {
        eventPublisher = new EventPublisherSpy();
        this.issueEventManager = new DefaultIssueEventManagerWithNotificationsSetup(paramsTransformer, eventPublisher);
    }

    @Test
    public void shouldCreateAnIssueEventObjectWithTheGivenParamsAndDispatchIt()
    {
        Issue issue = mock(Issue.class);
        User user = mock(User.class);

        Map<String, Object> expectedParamsOnEvent = new HashMap<String, Object>();
        when(paramsTransformer.transformParams(null)).thenReturn(expectedParamsOnEvent);

        issueEventManager.dispatchEvent(1L, issue, user, true);

        assertThat(eventPublisher.publishedEvents.size(), is(1));
        IssueEvent publishedEvent = (IssueEvent) eventPublisher.publishedEvents.get(0);
        assertEquals(1L, publishedEvent.getEventTypeId().longValue());
        assertEquals(issue, publishedEvent.getIssue());
        assertEquals(user, publishedEvent.getUser());
        assertTrue(publishedEvent.isSendMail());
        assertEquals(expectedParamsOnEvent, publishedEvent.getParams());
    }

    @Test
    public void shouldCreateAnIssueEventObjectWithTheGivenParamsAndDispatchItWhenCustomParametersArePassed()
    {
        Issue issue = mock(Issue.class);
        User user = mock(User.class);

        Map<String, Object> parameters = ImmutableMap.<String, Object>of("customParam", "customValue");
        Map<String, Object> expectedParamsOnEvent = new HashMap<String, Object>();
        when(paramsTransformer.transformParams(parameters)).thenReturn(expectedParamsOnEvent);

        issueEventManager.dispatchEvent(1L, issue, parameters, user, false);

        assertThat(eventPublisher.publishedEvents.size(), is(1));
        IssueEvent publishedEvent = (IssueEvent) eventPublisher.publishedEvents.get(0);
        assertEquals(1L, publishedEvent.getEventTypeId().longValue());
        assertEquals(issue, publishedEvent.getIssue());
        assertEquals(user, publishedEvent.getUser());
        assertFalse(publishedEvent.isSendMail());
        assertEquals(expectedParamsOnEvent, publishedEvent.getParams());
    }

    @Test
    public void shouldDispatchAnEventForTheIssueEventBundleAndOnePerEventContainedInTheBundleWhenCallingDispatchWithABundleOfEvents()
    {
        JiraIssueEvent event1 = mock(JiraIssueEvent.class);
        JiraIssueEvent event2 = mock(JiraIssueEvent.class);
        JiraIssueEvent event3 = mock(JiraIssueEvent.class);

        List<JiraIssueEvent> events = Arrays.asList(event1, event2, event3);
        IssueEventBundle issueEventBundle = DefaultIssueEventBundle.create(events);

        issueEventManager.dispatchEvent(issueEventBundle);

        assertThat(eventPublisher.publishedEvents.size(), is(4));
        assertThat(eventPublisher.publishedEvents, containsInAnyOrder(issueEventBundle, event1, event2, event3));
    }

    @Test
    public void shouldNotDispatchAnyEventsWhenNotificationsAreDisabledWhenCallingDispatchWithABundleOfEvents()
    {
        JiraIssueEvent event1 = mock(JiraIssueEvent.class);
        JiraIssueEvent event2 = mock(JiraIssueEvent.class);
        JiraIssueEvent event3 = mock(JiraIssueEvent.class);

        List<JiraIssueEvent> events = Arrays.asList(event1, event2, event3);
        IssueEventBundle issueEventBundle = DefaultIssueEventBundle.create(events);

        issueEventManager.disableNotifications();
        issueEventManager.dispatchEvent(issueEventBundle);

        assertThat(eventPublisher.publishedEvents.size(), is(0));
    }
    
    @Test
    public void publishAsRedundantShouldMarkTheIssueEventAsRedundantBeforePublishingIt()
    {
        IssueEvent event = anyIssueEvent();

        issueEventManager.publishAsRedundant(event);

        assertTrue(eventPublisher.publishedIssueEventWasRedundant);
    }

    private IssueEvent anyIssueEvent()
    {
        return new IssueEvent(null, null, null, null);
    }

    private static class DefaultIssueEventManagerWithNotificationsSetup extends DefaultIssueEventManager
    {
        private boolean notificationsEnabled = true;

        public DefaultIssueEventManagerWithNotificationsSetup(final IssueEventParamsTransformer paramsTransformer, final EventPublisher eventPublisher)
        {
            super(paramsTransformer, eventPublisher);
        }

        public void disableNotifications()
        {
            notificationsEnabled = false;
        }

        @Override
        boolean areNotificationsEnabled()
        {
            return notificationsEnabled;
        }
    }

    private static class EventPublisherSpy implements EventPublisher
    {
        public List<Object> publishedEvents = new ArrayList<Object>();
        public boolean publishedIssueEventWasRedundant = false;

        @Override
        public void publish(final Object event)
        {
            publishedEvents.add(event);
            if (event instanceof IssueEvent)
            {
                publishedIssueEventWasRedundant = ((IssueEvent) event).isRedundant();
            }
        }

        @Override
        public void register(final Object listener)
        {
            // no-op: not relevant for the test

        }

        @Override
        public void unregister(final Object listener)
        {
            // no-op: not relevant for the test
        }

        @Override
        public void unregisterAll()
        {
            // no-op: not relevant for the test
        }
    }
}
