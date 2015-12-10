package com.atlassian.jira.notification.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.util.GroupSelectorUtils;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opensymphony.module.propertyset.PropertySet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Test case for group custom field notification type.
 *
 * @see com.atlassian.jira.notification.type.GroupCFValue
 * @since v4.4
 */
@RunWith(MockitoJUnitRunner.class)
public class TestGroupCFValue
{
    @Mock private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock private FieldManager fieldManager;
    @Mock private UserPropertyManager userPropertyManager;
    @Mock private PropertySet propertySet;
    @Mock private Issue issue;

    private MockGroupSelectorUtils groupSelectorUtils;


    @Before
    public void initMocks()
    {
        when(issue.getIssueTypeObject()).thenReturn(new MockIssueType("test type", "test type"));
        when(userPropertyManager.getPropertySet(any(ApplicationUser.class))).thenReturn(propertySet);
        when(propertySet.exists(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).thenReturn(true);
        when(propertySet.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).thenReturn("html");
        new MockComponentWorker().addMock(UserPropertyManager.class, userPropertyManager).init();
        groupSelectorUtils = new MockGroupSelectorUtils();
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
    public void shouldGetRecipientsFromEventParams()
    {
        final GroupCFValue tested = new GroupCFValue(jiraAuthenticationContext, groupSelectorUtils, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
                "customfield_10000", "some people",
                "customfield_10010", 10L,
                "customfield_10020", new MockUser("not really a recipient")
        ));
        groupSelectorUtils.stubGetUsersForRawValue(ImmutableSet.<User>of(new MockUser("one"), new MockUser("two")));
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");

        groupSelectorUtils.assertGetUsersNotCalled();
        groupSelectorUtils.assertGetUsersForRawValueCalled("some people");
        assertEquals(2, recipients.size());
        assertEquals("one", recipients.get(0).getUserRecipient().getName());
        assertEquals("two", recipients.get(1).getUserRecipient().getName());
    }


    @Test
    public void shouldGetRecipientsFromIssueGivenValueInEventParamsIsNull()
    {
        final GroupCFValue tested = new GroupCFValue(jiraAuthenticationContext, groupSelectorUtils, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
                // no customfield_10000
                "customfield_10010", 10L,
                "customfield_10020", new MockUser("not really a recipient")
        ));
        groupSelectorUtils.stubGetUsers(ImmutableSet.<User>of(new MockUser("one"), new MockUser("two")));
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");

        groupSelectorUtils.assertGetUsersForRawValueNotCalled();
        groupSelectorUtils.assertGetUsersCalledFor(issue, "customfield_10000");
        assertEquals(2, recipients.size());
        assertEquals("one", recipients.get(0).getUserRecipient().getName());
        assertEquals("two", recipients.get(1).getUserRecipient().getName());
    }


    @Test
    public void shouldHandleNullEventParams()
    {
        groupSelectorUtils.stubGetUsers(Collections.<User>emptySet());
        final IssueEvent issueEvent = new IssueEvent(issue, null, new MockUser("sender"), 1L, true);
        final GroupCFValue tested = new GroupCFValue(jiraAuthenticationContext, groupSelectorUtils, fieldManager);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");

        assertTrue(recipients.isEmpty());
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

    private static class MockGroupSelectorUtils extends GroupSelectorUtils
    {
        private Set<User> getUsers;
        private Set<User> getUsersForRawValue;

        private Issue getUsersIssueParam;
        private String getUsersCustomFieldIdParam;
        private Object getUsersRawValueParam;

        public MockGroupSelectorUtils()
        {
            super(null, null, null);
        }

        public MockGroupSelectorUtils stubGetUsers(Set<User> returnValue)
        {
            getUsers = ImmutableSet.copyOf(returnValue);
            return this;
        }

        public MockGroupSelectorUtils stubGetUsersForRawValue(Set<User> returnValue)
        {
            getUsersForRawValue = ImmutableSet.copyOf(returnValue);
            return this;
        }

        @Override
        public Set<User> getUsers(Issue issue, String customFieldId)
        {
            getUsersIssueParam = issue;
            getUsersCustomFieldIdParam = customFieldId;
            return getUsers;
        }

        @Override
        public Set<User> getUsers(Object groupCustomFieldRawValue)
        {
            getUsersRawValueParam = groupCustomFieldRawValue;
            return getUsersForRawValue;
        }

        void assertGetUsersNotCalled()
        {
            assertNull(getUsersIssueParam);
            assertNull(getUsersCustomFieldIdParam);
        }

        void assertGetUsersCalledFor(Issue issue, String customFieldId)
        {
            assertEquals(issue, getUsersIssueParam);
            assertEquals(customFieldId, getUsersCustomFieldIdParam);
        }

        void assertGetUsersForRawValueNotCalled()
        {
            assertNull(getUsersRawValueParam);
        }

        void assertGetUsersForRawValueCalled(Object rawValue)
        {
            assertEquals(rawValue, getUsersRawValueParam);
        }
    }
}
