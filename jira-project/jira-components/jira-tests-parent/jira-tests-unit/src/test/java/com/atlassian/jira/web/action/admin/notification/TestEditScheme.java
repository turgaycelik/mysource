/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.notification.type.CurrentAssignee;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestEditScheme
{
    private final Long TEST_EVENT_TYPE_ID = new Long(1);
    private GenericValue scheme;
    private GenericValue notification1;

    @Mock @AvailableInContainer(interfaceClass=NotificationTypeManager.class)
    private NotificationTypeManager notificationTypeManager;
    @Mock @AvailableInContainer
    private NotificationSchemeManager notificationSchemeManager;
    @Mock @AvailableInContainer
    private EventTypeManager eventTypeManager;
    @Mock @AvailableInContainer
    private JiraAuthenticationContext authContext;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        when(authContext.getI18nHelper()).thenReturn(new MockI18nBean());

        scheme = new MockGenericValue("NotificationScheme", FieldMap.build("id", Long.valueOf(1), "scheme", "group", "TEST_TYPE_1", TEST_EVENT_TYPE_ID));
        when(notificationSchemeManager.getScheme(Long.valueOf(1))).thenReturn(scheme);
        notification1 = new MockGenericValue("NotificationSchemeEntity", FieldMap.build("id", Long.valueOf(1)));
        final List<GenericValue> schemeEntities = Collections.<GenericValue>singletonList(notification1);
        when(notificationSchemeManager.getEntities(scheme, TEST_EVENT_TYPE_ID)).thenReturn(schemeEntities);
        when(notificationTypeManager.getNotificationType("Current_Assignee")).thenReturn(new CurrentAssignee(authContext));
    }

    @Test
    public void testGetEvents()
    {
        EditNotifications es = new EditNotifications(null);
        Map events = es.getEvents();
        assertNotNull(events);
    }

    @Test
    public void testGetNotifications() throws GenericEntityException
    {
        EditNotifications es = new EditNotifications(null);
        es.setSchemeId(scheme.getLong("id"));

        List notifications = es.getNotifications(TEST_EVENT_TYPE_ID);
        assertTrue(!notifications.isEmpty());
        assertTrue(notifications.contains(notification1));
    }

    @Test
    public void testGetNotification()
    {
        EditNotifications es = new EditNotifications(null);
        es.setSchemeId(scheme.getLong("id"));

        NotificationType type = es.getType("Current_Assignee");
        assertNotNull(type);
        assertEquals("Current Assignee", type.getDisplayName());
    }
}
