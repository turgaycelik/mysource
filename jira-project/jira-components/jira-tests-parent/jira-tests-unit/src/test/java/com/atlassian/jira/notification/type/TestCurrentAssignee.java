/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import java.util.Arrays;
import java.util.List;


import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import junit.framework.Assert;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the CurrentAssignee notification type
 * <p/>
 * See JRA-6344 for more details on how things changed
 */
public class TestCurrentAssignee
{

    public static final String NEW_ASSIGNEE_EMAIL = "new@assignee.com";
    private ApplicationUser previousAssignee;

    private CurrentAssignee currentAssigneeFunc;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    @AvailableInContainer
    private UserManager userManager;
    @Mock
    @AvailableInContainer
    private UserPreferencesManager userPreferencesManager;

    @Rule
    public RuleChain mocks = MockitoMocksInContainer.forTest(this);


    private MockIssue issueObject;
    private MockApplicationUser newAssignee;

    /**
     * Helper class for providing a previous assignee
     */
    private class PreviousAssigneeAwareCurrentAssignee extends CurrentAssignee
    {
        public PreviousAssigneeAwareCurrentAssignee(final JiraAuthenticationContext jiraAuthenticationContext)
        {
            super(jiraAuthenticationContext);
        }

        @Override
        protected ApplicationUser getPreviousAssignee(final IssueEvent event)
        {
            return previousAssignee;
        }
    }

    @Before
    public void setup()
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper());

        newAssignee = new MockApplicationUser("ID2134", "newassignee", "New Assignee", NEW_ASSIGNEE_EMAIL);
        when(userManager.getUserByKey("ID2134")).thenReturn(newAssignee);

        previousAssignee = new MockApplicationUser("fred", "freddy", "Old Fred", "fred@example.com");
        when(userManager.getUserByKey(previousAssignee.getKey())).thenReturn(previousAssignee);

        issueObject = new MockIssue(123, "ISS-454");
        issueObject.setAssignee(newAssignee.getDirectoryUser());

        ExtendedPreferences assigneePrefs = Mockito.mock(ExtendedPreferences.class);
        when(assigneePrefs.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).thenReturn("html");

        when(userPreferencesManager.getExtendedPreferences(newAssignee)).thenReturn(assigneePrefs);
        when(userPreferencesManager.getExtendedPreferences(previousAssignee)).thenReturn(assigneePrefs);

        currentAssigneeFunc = new PreviousAssigneeAwareCurrentAssignee(jiraAuthenticationContext);

    }

    @Test
    public void testGetDisplayName()
    {
        Assert.assertEquals("admin.notification.types.current.assignee", currentAssigneeFunc.getDisplayName());
    }

    /**
     * This is very unlikely to happen really but lets test it in isolation anyways
     */
    @Test
    public void testGetRecipients_NullIssue()
    {
        IssueEvent event = new IssueEvent(null, null, null, null);


        final List<NotificationRecipient> actualList = currentAssigneeFunc.getRecipients(event, null);
        assertThat(actualList, Matchers.<NotificationRecipient>empty());
    }

    /**
     * Tests the new behaviour when the issue event IS an assigned event
     */
    @Test
    public void testGetRecipientsIssueAssigned()
    {
        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_ASSIGNED_ID);

        final List<NotificationRecipient> actualList = currentAssigneeFunc.getRecipients(event, null);
        assertThat(actualList, hasRecipients(previousAssignee, newAssignee));
    }

    /**
     * Tests the new behaviour when the issue event IS an assigned event BUT there is not previous assignee
     */
    @Test
    public void testGetRecipientsIssueAssigned_NullPreviousAssignee()
    {

        previousAssignee = null;

        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_ASSIGNED_ID);

        final List<NotificationRecipient> actualList = currentAssigneeFunc.getRecipients(event, null);
        assertThat(actualList, hasRecipients(newAssignee));
    }


    /**
     * Tests the new behaviour when the issue event is NOT an assigned event.  Should have both parties
     */
    @Test
    public void testGetRecipientsIssueNotAssigned()
    {
        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_UPDATED_ID);


        final List<NotificationRecipient> actualList = currentAssigneeFunc.getRecipients(event, null);
        assertThat(actualList, hasRecipients(previousAssignee, newAssignee));
    }

    /**
     * Tests the new behaviour when the issue event is NOT an assigned event BUt the previous assignee was null
     */
    @Test
    public void testGetRecipientsIssueNotAssigned_NullPreviousAssignee()
    {
        previousAssignee = null;

        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_UPDATED_ID);

        final List<NotificationRecipient> actualList = currentAssigneeFunc.getRecipients(event, null);
        assertThat(actualList, hasRecipients(newAssignee));
    }


    /**
     * Tests the new behaviour when the issue event is NOT an assigned event BUt the new assignee was null
     */
    @Test
    public void testGetRecipientsIssueNotAssigned_NullNewAssignee()
    {

        issueObject.setAssignee(null);

        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_UPDATED_ID);

        final List<NotificationRecipient> actualList = currentAssigneeFunc.getRecipients(event, null);
        assertThat(actualList, hasRecipients(previousAssignee));
    }

    /**
     * Tests the new behaviour when the issue event is NOT an assigned event BUT the previous and new assignee are null
     */
    @Test
    public void testGetRecipientsIssueNotAssigned_NullParties()
    {
        previousAssignee = null;
        issueObject.setAssignee(null);

        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_UPDATED_ID);

        final List<NotificationRecipient> actualList = currentAssigneeFunc.getRecipients(event, null);
        assertThat(actualList, Matchers.<NotificationRecipient>empty());
    }

    private static Matcher<Iterable<NotificationRecipient>> hasRecipients(ApplicationUser... applicationUser)
    {

        Iterable<NotificationRecipient> rcps = Iterables.transform(Arrays.asList(applicationUser), new Function<ApplicationUser, NotificationRecipient>()
        {
            @Override
            public NotificationRecipient apply(final ApplicationUser input)
            {
                return new NotificationRecipient(input);
            }
        });
        NotificationRecipient[] rcpsArray = Iterables.toArray(rcps, NotificationRecipient.class);
        return containsInAnyOrder(rcpsArray);
    }
}
