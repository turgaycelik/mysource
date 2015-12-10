package com.atlassian.jira.notification.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
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
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test case for user custom field notification type
 *
 * @see UserCFValue
 * @since v4.4
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUserCFValue
{
    @Mock private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock private FieldManager fieldManager;
    @Mock private UserPropertyManager userPropertyManager;
    @Mock private PropertySet propertySet;
    @Mock private Issue issue;

    @Before
    public void setUp()
    {
        when(issue.getIssueTypeObject()).thenReturn(new MockIssueType("test type", "test type"));
        when(userPropertyManager.getPropertySet(any(ApplicationUser.class))).thenReturn(propertySet);
        when(propertySet.exists(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).thenReturn(true);
        when(propertySet.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).thenReturn("html");

        new MockComponentWorker()
                .addMock(UserPropertyManager.class, userPropertyManager)
                .init();
    }

    @After
    public void tearDown()
    {
        jiraAuthenticationContext = null;
        fieldManager = null;
        userPropertyManager = null;
        propertySet = null;
        issue = null;
    }

    @Test
    public void shouldGetSingleRecipientFromEventParams()
    {
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
            "customfield_10000", new MockApplicationUser("a recipient"),
            "customfield_10010", 10L,
            "customfield_10020", new MockApplicationUser("not really a recipient")
        ));
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertEquals(1, recipients.size());
        assertEquals("a recipient", recipients.get(0).getUser().getUsername());
    }

    @Test
    public void shouldGetMultipleRecipientsFromEventParams()
    {
        final ApplicationUser user1 = new MockApplicationUser("A Recipient");
        final ApplicationUser user2 = new MockApplicationUser("Another Recipient");
        final ApplicationUser user3 = new MockApplicationUser("Even More Recipients");
        final ApplicationUser user4 = new MockApplicationUser("Not A Recipient");

        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(
                IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
                        "customfield_10000", ImmutableList.of(user1, user2, user3),
                        "customfield_10010", 10L,
                        "customfield_10020", user4 ));
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients, user1, user2, user3);
    }

    @Test
    public void shouldReturnEmptyRecipientsListGivenValueInEventParamsIsNotRecognized()
    {
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
            "customfield_10000", "what the hell is this?",
            "customfield_10010", 10L,
            "customfield_10020", new MockApplicationUser("not really a recipient")
        ));
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients);
    }

    @Test
    public void shouldGetSingleRecipientFromIssueGivenValueInEventParamsIsNull()
    {
        final ApplicationUser user1 = new MockApplicationUser("A Recipient");
        CustomField userCustomField = mock(CustomField.class);
        when(userCustomField.isInScope(any(Project.class), anyListOf(String.class))).thenReturn(true);
        when(userCustomField.getValue(issue)).thenReturn(user1);
        when(fieldManager.getCustomField("customfield_10000")).thenReturn(userCustomField);

        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
            // no customfield_10000
            "customfield_10010", 10L,
            "customfield_10020", new MockApplicationUser("not really a recipient")
        ));
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients, user1);
    }

    @Test
    public void shouldGetMultipleRecipientsFromIssueGivenValueInEventParamsIsNull()
    {
        final ApplicationUser user1 = new MockApplicationUser("A Recipient");
        final ApplicationUser user2 = new MockApplicationUser("Another Recipient");
        final ApplicationUser user3 = new MockApplicationUser("Even More Recipients");

        CustomField userCustomField = mock(CustomField.class);
        when(userCustomField.isInScope(any(Project.class), anyListOf(String.class))).thenReturn(true);
        when(userCustomField.getValue(issue)).thenReturn(ImmutableList.of(user1, user2, user3));
        when(fieldManager.getCustomField("customfield_10000")).thenReturn(userCustomField);

        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
            // no customfield_10000
            "customfield_10010", 10L,
            "customfield_10020", new MockApplicationUser("not really a recipient")
        ));
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients, user1, user2, user3);
    }

    @Test
    public void shouldGetSingleRecipientFromIssueGivenNoEventParam()
    {
        final ApplicationUser user1 = new MockApplicationUser("A Recipient");

        CustomField userCustomField = mock(CustomField.class);
        when(userCustomField.isInScope(any(Project.class), anyListOf(String.class))).thenReturn(true);
        when(userCustomField.getValue(issue)).thenReturn(user1);
        when(fieldManager.getCustomField("customfield_10000")).thenReturn(userCustomField);

        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients, user1);
    }


    @Test
    public void shouldGetMultipleRecipientsFromIssueGivenNoEventParam()
    {
        final ApplicationUser user1 = new MockApplicationUser("A Recipient");
        final ApplicationUser user2 = new MockApplicationUser("Another Recipient");
        final ApplicationUser user3 = new MockApplicationUser("Even More Recipients");

        CustomField userCustomField = mock(CustomField.class);
        when(userCustomField.isInScope(any(Project.class), anyListOf(String.class))).thenReturn(true);
        when(userCustomField.getValue(issue)).thenReturn(ImmutableList.of(user1, user2, user3));
        when(fieldManager.getCustomField("customfield_10000")).thenReturn(userCustomField);

        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients, user1, user2, user3);
    }

    @Test
    public void shouldReturnEmptyRecipientsListGivenNoEventParamAndValueInIssueNotRecognized()
    {
        CustomField userCustomField = mock(CustomField.class);
        when(userCustomField.isInScope(any(Project.class), anyListOf(String.class))).thenReturn(true);
        when(userCustomField.getValue(issue)).thenReturn("WOOOOT?");
        when(fieldManager.getCustomField("customfield_10000")).thenReturn(userCustomField);

        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients);
    }

    @Test
    public void shouldReturnEmptyRecipientsListGivenNoEventParamAndValueInIssueIsNull()
    {
        CustomField userCustomField = mock(CustomField.class);
        when(userCustomField.isInScope(any(Project.class), anyListOf(String.class))).thenReturn(true);
        when(userCustomField.getValue(issue)).thenReturn(null);
        when(fieldManager.getCustomField("customfield_10000")).thenReturn(userCustomField);

        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients);
    }

    @Test
    public void shouldReturnEmptyRecipientsListGivenNoEventParamAndFieldIsNotInScope()
    {
        CustomField userCustomField = mock(CustomField.class);
        when(userCustomField.isInScope(any(Project.class), anyListOf(String.class))).thenReturn(false);
        when(userCustomField.getValue(issue)).thenReturn(new MockApplicationUser("a recipient"));
        when(fieldManager.getCustomField("customfield_10000")).thenReturn(userCustomField);

        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients);
    }


    @Test
    public void shouldReturnEmptyRecipientsListGivenNoEventParamAndNoCorrespondingField()
    {
        when(fieldManager.getCustomField("customfield_10000")).thenReturn(null);

        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients);
    }

    @Test
    public void shouldHandleNullEventParams()
    {
        when(fieldManager.getCustomField("customfield_10000")).thenReturn(null);

        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        final IssueEvent issueEvent = new IssueEvent(issue, null, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        checkRecipients(recipients);
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
