/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestAllWatchers
{
    @Mock private IssueManager mockIssueManager;
    @Mock private Issue mockIssue;
    @Mock private UserPropertyManager userPropertyManager;
    @Mock private PropertySet propertySet;

    @Before
    public void setUp()
    {
        when(userPropertyManager.getPropertySet(any(ApplicationUser.class))).thenReturn(propertySet);
        when(propertySet.exists(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).thenReturn(true);
        when(propertySet.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).thenReturn("html");
        new MockComponentWorker().addMock(UserPropertyManager.class, userPropertyManager).init();
    }

    @After
    public void tearDown()
    {
        mockIssueManager = null;
        mockIssue = null;
        userPropertyManager = null;
        propertySet = null;
    }

    @Test
    public void testGetDisplayName()
    {
        assertEquals("All Watchers", createTested().getDisplayName());
    }

    @Test
    public void shouldGetRecipientsEventParams()
    {
        final ApplicationUser one = new MockApplicationUser("One");
        final ApplicationUser two = new MockApplicationUser("Two");
        when(mockIssueManager.getWatchersFor(mockIssue)).thenReturn(ImmutableList.of(one, two));

        IssueEvent event = new IssueEvent(mockIssue, null, new MockUser("sender"), 1L);
        List<NotificationRecipient> recipients = createTested().getRecipients(event, null);
        checkRecipients(recipients, one, two);
    }

    @Test
    public void shouldGetRecipientsFromIssueGivenNoEventParams()
    {
        final ApplicationUser one = new MockApplicationUser("One");
        final ApplicationUser two = new MockApplicationUser("Two");
        final Map<String,Object> params = ImmutableMap.<String,Object>of(
                IssueEvent.WATCHERS_PARAM_NAME,
                ImmutableList.<User>of(one.getDirectoryUser(), two.getDirectoryUser()) );
        final IssueEvent event = new IssueEvent(mockIssue, params, new MockUser("sender"), 1L);

        final List<NotificationRecipient> recipients = createTested().getRecipients(event, null);
        checkRecipients(recipients, one, two);
    }



    private AllWatchers createTested()
    {
        return new AllWatchers(new MockSimpleAuthenticationContext(null, Locale.ENGLISH), mockIssueManager);
    }

    private ImmutableMap<String, Object> defaultParams()
    {
        return ImmutableMap.<String,Object>of(IssueEvent.WATCHERS_PARAM_NAME,
                ImmutableList.<User>of(new MockUser("one"), new MockUser("two")));
    }

    private static void checkRecipients(List<NotificationRecipient> actualRecipients, ApplicationUser... expectedUsers)
    {
        final List<NotificationRecipient> expectedRecipients = new ArrayList<NotificationRecipient>(expectedUsers.length);
        for (ApplicationUser user : expectedUsers)
        {
            expectedRecipients.add(new NotificationRecipient(user));
        }
        assertEquals(expectedRecipients, actualRecipients);
    }
}
