/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import com.google.common.collect.Lists;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestProjectActionSupport 
{
    @Rule
    public RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    private OfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();
    @Mock @AvailableInContainer private PermissionManager mockPermissionManager;
    @Mock @AvailableInContainer private PermissionSchemeManager mockPermissionSchemeManager;
    @Mock @AvailableInContainer private JiraAuthenticationContext mockJiraAuthenticationContext;
    @Mock @AvailableInContainer private ProjectManager mockJiraProjectManager;

    @Test
    public void testGetProjectManager()
    {
        ProjectActionSupport pas = new ProjectActionSupport();
        assertTrue(pas.getProjectManager() instanceof ProjectManager);
    }

    @Test
    public void testBrowseableProjects() throws Exception
    {
        GenericValue scheme = mockOfBizDelegator.createValue("PermissionScheme", FieldMap.build("id", new Long(10), "name", "Test Scheme"));
        GenericValue project1 = mockOfBizDelegator.createValue("Project", FieldMap.build("id", new Long(4)));
        ComponentAccessor.getPermissionSchemeManager().addSchemeToProject(project1, scheme);

        GenericValue project2 = mockOfBizDelegator.createValue("Project", FieldMap.build("id", new Long(5)));

        ProjectActionSupport pas = new ProjectActionSupport();

        assertEquals(0, pas.getBrowseableProjects().size());

        pas = new ProjectActionSupport();
        when(mockJiraProjectManager.getProjects()).thenReturn(Lists.newArrayList(project1, project2));
        when(mockPermissionManager.getProjects(Permissions.BROWSE,(User) null)).thenReturn(Collections.singletonList(project1));
        assertEquals(1, pas.getBrowseableProjects().size());
        assertTrue(pas.getBrowseableProjects().contains(project1));

        pas = new ProjectActionSupport();

        when(mockPermissionManager.getProjects(Permissions.BROWSE, (User) null)).thenReturn(Lists.newArrayList(project1, project2));
        assertEquals(2, pas.getBrowseableProjects().size());
        assertTrue(pas.getBrowseableProjects().contains(project1));
        assertTrue(pas.getBrowseableProjects().contains(project2));
    }
}
