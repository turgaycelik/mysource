/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.notification.type.CurrentAssignee;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableMap;

public class TestNotificationSchemeManager
{

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @AvailableInContainer
    private final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    @Mock
    private NotificationTypeManager notificationTypeManager;

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    @Mock
    private UserPreferencesManager userPreferencesManager;

    @Mock
    @AvailableInContainer
    private UserPropertyManager userPropertyManager;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    
    @Mock
    private SchemeFactory schemeFactory;

    private NotificationSchemeManager notificationSchemeManager;

    private final ApplicationProperties applicationProperties = new MockApplicationProperties();

    @Before
    public void onTestUp()
    {
        notificationSchemeManager = new DefaultNotificationSchemeManager(mock(ProjectManager.class), mock(PermissionTypeManager.class),
                mock(PermissionContextFactory.class), ofBizDelegator, schemeFactory, mock(EventPublisher.class),
                notificationTypeManager, mock(NodeAssociationStore.class), mock(GroupManager.class), userPreferencesManager,
                new MemoryCacheManager());
        
        when(schemeFactory.getScheme(Mockito.<GenericValue>any())).thenAnswer(new Answer<Scheme>()
        {

            @Override
            public Scheme answer(final InvocationOnMock invocation) throws Throwable
            {
                final GenericValue genericValue = (GenericValue) invocation.getArguments()[0];
                return new Scheme(genericValue.getLong("id"), genericValue.getEntityName(), genericValue.getString("name"),
                        Collections.<SchemeEntity> emptyList());
            }
        });
    }

    @Test
    public void testGetSchemeId() throws Exception
    {
        Scheme scheme = notificationSchemeManager.getSchemeObject(new Long(1));
        assertNull(scheme);

        final String name = "This Name";
        final GenericValue createdScheme = UtilsForTests.getTestEntity("NotificationScheme", ImmutableMap.<String, Object> of("name", name));
        scheme = notificationSchemeManager.getSchemeObject(createdScheme.getLong("id"));

        assertNotNull(scheme);
        assertEquals(name, scheme.getName());
    }

    @Test
    public void testGetSchemeName() throws GenericEntityException
    {
        Scheme scheme = notificationSchemeManager.getSchemeObject("This Name");
        assertNull(scheme);

        final String name = "This Name";
        UtilsForTests.getTestEntity("NotificationScheme", ImmutableMap.<String, Object> of("name", name));
        scheme = notificationSchemeManager.getSchemeObject("This Name");

        assertNotNull(scheme);
        assertEquals(name, scheme.getName());
    }

    @Test
    public void testSchemeExists() throws GenericEntityException
    {
        assertFalse(notificationSchemeManager.schemeExists("This Name"));
        final String name = "This Name";
        UtilsForTests.getTestEntity("NotificationScheme", ImmutableMap.<String, Object> of("name", name));
        assertTrue(notificationSchemeManager.schemeExists(name));
    }

    @Test
    public void testCreateScheme() throws GenericEntityException
    {
        Scheme scheme = notificationSchemeManager.createSchemeObject("This Name", "Description");
        assertNotNull(scheme);
        scheme = notificationSchemeManager.getSchemeObject("This Name");
        assertNotNull(scheme);
        
        expectedException.expect(DataAccessException.class);
        expectedException.expectMessage("Could not create Notification Scheme with name:This Name as it already exists.");
        notificationSchemeManager.createSchemeObject("This Name", "");
    }

    @Test
    public void testUpdateScheme() throws GenericEntityException
    {
        final String nameInitial = "This Name";
        final String nameUpdated = "That Name";

        Scheme scheme = notificationSchemeManager.createSchemeObject(nameInitial, "");
        assertNotNull(scheme);
        assertEquals(nameInitial, scheme.getName());

        scheme = new Scheme(scheme.getId(), scheme.getType(), nameUpdated, scheme.getDescription(), scheme.getEntities());
        notificationSchemeManager.updateScheme(scheme);

        assertNull(notificationSchemeManager.getSchemeObject(nameInitial));
        final Scheme updatedScheme = notificationSchemeManager.getSchemeObject(nameUpdated);
        assertEquals(scheme.getId(), updatedScheme.getId());
        assertEquals(scheme.getName(), updatedScheme.getName());
        assertEquals(scheme.getDescription(), updatedScheme.getDescription());
    }

    @Test
    public void testDeleteScheme() throws GenericEntityException
    {
        final String schemeName = "This Name";
        final Scheme scheme = notificationSchemeManager.createSchemeObject(schemeName, "");
        assertNotNull(scheme);
        notificationSchemeManager.deleteScheme(scheme.getId());
        assertNull(notificationSchemeManager.getSchemeObject(schemeName));
    }

    @Test
    public void testRemoveEntities() throws Exception
    {
        final GenericValue gvAllWatchers = ofBizDelegator.createValue("Notification", ImmutableMap.<String, Object> of("id",
                new Long(10000), "eventTypeId", new Long(1), "type", "All_Watchers", "parameter", "customfield_10030", "scheme", new Long(
                        10000)));
        ofBizDelegator.createValue("Notification", ImmutableMap.<String, Object> of("id", new Long(10001), "eventTypeId", new Long(1),
                "type", "User_Custom_Field_Value", "parameter", "customfield_10030", "scheme", new Long(10000)));
        final GenericValue gvCustomField_10031 = ofBizDelegator.createValue("Notification", ImmutableMap.<String, Object> of("id",
                new Long(10002), "eventTypeId", new Long(1), "type", "User_Custom_Field_Value", "parameter", "customfield_10031", "scheme",
                new Long(10000)));

        notificationSchemeManager.removeEntities("User_Custom_Field_Value", "customfield_10030");
        final List<GenericValue> notifications = ofBizDelegator.findAll("Notification");
        Assert.assertThat(notifications, Matchers.containsInAnyOrder(gvAllWatchers, gvCustomField_10031));
    }

    @Test
    public void testRemoveSchemeEntitiesForField() throws Exception
    {
        // Create some rows in Notification table.
        final GenericValue gvAllWatchers = ofBizDelegator.createValue("Notification", ImmutableMap.<String, Object> of("id",
                new Long(10000), "eventTypeId", new Long(1), "type", "All_Watchers", "parameter", "customfield_10030", "scheme", new Long(
                        10000)));
        final GenericValue gvUserCustomField_10031 = ofBizDelegator.createValue("Notification", ImmutableMap.<String, Object> of("id",
                new Long(10002), "eventTypeId", new Long(1), "type", "User_Custom_Field_Value", "parameter", "customfield_10031", "scheme",
                new Long(10000)));
        final GenericValue gvGroupCustomField_10031 = ofBizDelegator.createValue("Notification", ImmutableMap.<String, Object> of("id",
                new Long(10004), "eventTypeId", new Long(1), "type", "Group_Custom_Field_Value", "parameter", "customfield_10031",
                "scheme", new Long(10000)));
        ofBizDelegator.createValue("Notification", ImmutableMap.<String, Object> of("id", new Long(10001), "eventTypeId", new Long(1),
                "type", "User_Custom_Field_Value", "parameter", "customfield_10030", "scheme", new Long(10000)));
        ofBizDelegator.createValue("Notification", ImmutableMap.<String, Object> of("id", new Long(10003), "eventTypeId", new Long(1),
                "type", "Group_Custom_Field_Value", "parameter", "customfield_10030", "scheme", new Long(10000)));

        notificationSchemeManager.removeSchemeEntitiesForField("customfield_10030");
        final List<GenericValue> notifications = ofBizDelegator.findAll("Notification");
        Assert.assertThat(notifications, Matchers.containsInAnyOrder(gvAllWatchers, gvGroupCustomField_10031, gvUserCustomField_10031));
    }

    /**
     * Test to check that getRecipients gets the right list of recipients
     */
    @Test
    public void testGetRecipients() throws Exception
    {
        final User user = new MockUser("bill", "bill", "bill@atlassian.com");
        final ApplicationUser applicationUser = ApplicationUsers.from(user);
        final ExtendedPreferences extendedPreferences = mock(ExtendedPreferences.class);
        final Issue issue = mock(Issue.class);
        final IssueEvent event = new IssueEvent(issue, null, user, null);
        final SchemeEntity notificationSchemeEntity = mock(SchemeEntity.class);

        when(notificationSchemeEntity.getType()).thenReturn("Current_Assignee");
        when(notificationTypeManager.getNotificationType(notificationSchemeEntity.getType())).thenReturn(
                new CurrentAssignee(jiraAuthenticationContext));
        when(issue.getAssigneeId()).thenReturn(user.getName());
        when(userManager.getUserByKey(user.getName())).thenReturn(applicationUser);
        when(userPreferencesManager.getExtendedPreferences(applicationUser)).thenReturn(extendedPreferences);

        Set<NotificationRecipient> recipients;

        when(extendedPreferences.getBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES)).thenReturn(Boolean.TRUE);
        recipients = notificationSchemeManager.getRecipients(event, notificationSchemeEntity);
        assertEquals(Collections.singleton(new NotificationRecipient(ApplicationUsers.from(user))), recipients);

        when(extendedPreferences.getBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES)).thenReturn(Boolean.FALSE);
        recipients = notificationSchemeManager.getRecipients(event, notificationSchemeEntity);
        assertEquals(Collections.emptySet(), recipients);
    }

    /**
     * Test to check that getRecipients gets the right list of recipients
     */
    @Test
    public void testGetRecipientsInactiveUser() throws Exception
    {
        final MockUser user = new MockUser("bill", "bill", "bill@atlassian.com");
        final ApplicationUser applicationUser = ApplicationUsers.from(user);
        final ExtendedPreferences extendedPreferences = mock(ExtendedPreferences.class);
        final Issue issue = mock(Issue.class);
        final IssueEvent event = new IssueEvent(issue, null, user, null);
        final SchemeEntity notificationSchemeEntity = mock(SchemeEntity.class);

        when(notificationSchemeEntity.getType()).thenReturn("Current_Assignee");
        when(notificationTypeManager.getNotificationType(notificationSchemeEntity.getType())).thenReturn(
                new CurrentAssignee(jiraAuthenticationContext));
        when(issue.getAssigneeId()).thenReturn(user.getName());
        when(userManager.getUserByKey(user.getName())).thenReturn(applicationUser);
        when(userPreferencesManager.getExtendedPreferences(applicationUser)).thenReturn(extendedPreferences);
        when(extendedPreferences.getBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES)).thenReturn(Boolean.TRUE);

        Set<NotificationRecipient> recipients;

        user.setActive(true);
        recipients = notificationSchemeManager.getRecipients(event, notificationSchemeEntity);
        assertEquals(Collections.singleton(new NotificationRecipient(ApplicationUsers.from(user))), recipients);

        user.setActive(false);
        recipients = notificationSchemeManager.getRecipients(event, notificationSchemeEntity);
        assertEquals(Collections.emptySet(), recipients);
    }

}
