/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.project;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.Permissions;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import electric.xml.Document;
import electric.xml.Element;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import webwork.action.ActionContext;

import java.util.Collection;

public class TestAddVersion extends AbstractJellyTestCase
{
    private final String PROJECT_KEY = "ABC";
    private User u;

    public TestAddVersion(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        //Create user and place in the action context
        u = createMockUser("logged-in-user");
        Group group = createMockGroup("admin-group");
        addUserToGroup(u, group);
        JiraTestUtil.loginUser(u);

        //Create the administer permission for that group
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, "admin-group");
    }

    public void testAddVersion() throws Exception
    {
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "A Project", "key", PROJECT_KEY));

        final Document document = runScript("add-version.test.add-version.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());
        String text = root.getText().toString().trim();
        assertTrue(text.matches("myVersion_\\d+_"));
        // Check project has version
        final Collection versions = ComponentAccessor.getVersionManager().getVersions(project);
        assertFalse(versions.isEmpty());
        assertEquals(1, versions.size());

        Version version = (Version) versions.iterator().next();
        assertEquals("Ver 1", version.getString("name"));
    }

    public void testAddVersionToProject() throws Exception
    {
        UtilsForTests.getTestEntity("IssueTypeScreenScheme", EasyMap.build("name", "Test Scheme"));
        final Document document = runScript("add-version.test.add-version-to-project.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        // Check project has version
        final ProjectManager projectManager = ManagerFactory.getProjectManager();
        final GenericValue project = projectManager.getProjectByKey(PROJECT_KEY);
        final Collection versions = ComponentAccessor.getVersionManager().getVersions(project);
        assertFalse(versions.isEmpty());
        assertEquals(1, versions.size());

        Version version = (Version) versions.iterator().next();
        assertEquals("Ver 1", version.getString("name"));
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "project" + FS;
    }
}
