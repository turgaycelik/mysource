/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.project;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import electric.xml.Document;
import electric.xml.Element;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Iterator;

public class TestAddComponent extends AbstractJellyTestCase
{
    private final String PROJECT_KEY = "ABC";

    public TestAddComponent(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        //Create user and place in the action context
        User u = createMockUser("logged-in-user");
        Group group = createMockGroup("admin-group");
        addUserToGroup(u, group);
        JiraTestUtil.loginUser(u);

        //Create the administer permission for that group
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, "admin-group");
    }

    public void testAddComponent() throws Exception
    {
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "A Project", "key", PROJECT_KEY));

        final Document document = runScript("add-component.test.add-component.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        // Check project has component
        final Collection components = ManagerFactory.getProjectManager().getComponents(project);
        assertFalse(components.isEmpty());
        assertEquals(1, components.size());

        GenericValue component = (GenericValue) components.iterator().next();
        assertEquals("Comp 1", component.getString("name"));
    }

    public void testAddComponentWithDescription() throws Exception
    {
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "A Project", "key", PROJECT_KEY));

        final Document document = runScript("add-component.test.add-component-description.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        // Check project has the 3 components
        final Collection components = ManagerFactory.getProjectManager().getComponents(project);
        assertFalse(components.isEmpty());
        assertEquals(3, components.size());

        //get the components and validate their values
        Iterator iterator = components.iterator();
        GenericValue component1 = (GenericValue) iterator.next();
        GenericValue component2 = (GenericValue) iterator.next();
        GenericValue component3 = (GenericValue) iterator.next();
        //check that the names are correct
        assertEquals("Comp with description", component1.getString("name"));
        assertEquals("Comp with empty description", component2.getString("name"));
        assertEquals("Comp with null description", component3.getString("name"));
        //check the descriptions values are correct
        assertEquals("this is the description", component1.getString("description"));
        assertEquals(null, component2.getString("description")); //empty string should have been stored as null - JRA-12193
        assertEquals(null, component3.getString("description"));
    }

    public void testAddComponentToProject() throws Exception
    {
        UtilsForTests.getTestEntity("IssueTypeScreenScheme", EasyMap.build("name", "Test Scheme", "description", "Scheme For Testing"));

        final Document document = runScript("add-component.test.add-component-to-project.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        // Check project has component
        final ProjectManager projectManager = ManagerFactory.getProjectManager();
        final GenericValue project = projectManager.getProjectByKey(PROJECT_KEY);
        final Collection components = projectManager.getComponents(project);
        assertFalse(components.isEmpty());
        assertEquals(1, components.size());

        GenericValue component = (GenericValue) components.iterator().next();
        assertEquals("Comp 1", component.getString("name"));
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "project" + FS;
    }
}
