/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.notification.DefaultNotificationSchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.DefaultPermissionSchemeManager;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import static org.junit.Assert.assertEquals;

public class TestEditSchemeAction
{
    @Mock private ProjectManager projectManager;
    @Mock private PermissionTypeManager permissionTypeManager;
    @Mock private PermissionContextFactory permissionContextFactory;
    @Mock private SchemeFactory schemeFactory;
    @Mock private NodeAssociationStore nodeAssociationStore;
    @Mock private GroupManager groupManager;
    @Mock private EventPublisher eventPublisher;
    @Mock private NotificationTypeManager notificationTypeManager;
    @Mock private UserPreferencesManager userPreferencesManager;

    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    private MockOfBizDelegator ofBizDelegator;
    private MockComponentWorker mockComponentWorker;

    private PermissionSchemeManager permSchemeManager;
    private NotificationSchemeManager notificationSchemeManager;

    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);
    private MemoryCacheManager cacheManager;

    @Before
    public void setUp() throws Exception
    {
        ofBizDelegator = new MockOfBizDelegator();
        cacheManager = new MemoryCacheManager();

        notificationSchemeManager = new DefaultNotificationSchemeManager(projectManager, permissionTypeManager, permissionContextFactory, ofBizDelegator, schemeFactory, eventPublisher, notificationTypeManager, nodeAssociationStore, groupManager, userPreferencesManager, cacheManager);
        permSchemeManager = new DefaultPermissionSchemeManager(projectManager, permissionTypeManager, permissionContextFactory, ofBizDelegator, schemeFactory, nodeAssociationStore, groupManager, eventPublisher, cacheManager);

        mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.addMock(OfBizDelegator.class, ofBizDelegator);
        mockComponentWorker.addMock(RedirectSanitiser.class, redirectSanitiser);
        mockComponentWorker.addMock(PermissionSchemeManager.class, permSchemeManager);
        mockComponentWorker.addMock(NotificationSchemeManager.class, notificationSchemeManager);

        mockComponentWorker.init();
    }

    @Test
    public void testEditPermissionScheme() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewPermissionSchemes.jspa");

        com.atlassian.jira.web.action.admin.permission.EditScheme editScheme = new com.atlassian.jira.web.action.admin.permission.EditScheme();

        GenericValue scheme = permSchemeManager.createScheme("PScheme", "Test Desc");

        //set the scheme details
        editScheme.setName("New Name");
        editScheme.setDescription("New Description");

        //The scheme manager should be set correctly
        assertEquals(editScheme.getSchemeManager(), permSchemeManager);

        assertEquals(1, permSchemeManager.getSchemes().size());

        editScheme.setSchemeId(scheme.getLong("id"));

        //edit the scheme
        String result = editScheme.execute();

        //the new scheme should be there
        final GenericValue newScheme = permSchemeManager.getScheme(scheme.getLong("id"));
        assertEquals("New Name", newScheme.getString("name"));
        assertEquals("New Description", newScheme.getString("description"));

        //there should be no errors
        assertEquals(0, editScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }

    @Test
    public void testEditNotificationScheme() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewNotificationSchemes.jspa");

        com.atlassian.jira.web.action.admin.notification.EditScheme editScheme = new com.atlassian.jira.web.action.admin.notification.EditScheme();

        GenericValue scheme = notificationSchemeManager.createScheme("NScheme", "Test Desc");

        //set the scheme details
        editScheme.setName("New Name");
        editScheme.setDescription("New Description");

        //The scheme manager should be set correctly
        assertEquals(editScheme.getSchemeManager(), notificationSchemeManager);

        assertEquals(1, notificationSchemeManager.getSchemes().size());

        editScheme.setSchemeId(scheme.getLong("id"));

        //edit the scheme
        String result = editScheme.execute();

        //the new scheme should be there
        final GenericValue newScheme = notificationSchemeManager.getScheme(scheme.getLong("id"));
        assertEquals("New Name", newScheme.getString("name"));
        assertEquals("New Description", newScheme.getString("description"));

        //there should be no errors
        assertEquals(0, editScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }
}
