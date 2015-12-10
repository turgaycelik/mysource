/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import electric.xml.Document;
import electric.xml.Element;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.List;

public class TestAddUserToGroup extends AbstractJellyTestCase
{
    private GroupManager groupMagager;

    public TestAddUserToGroup(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        //Create user and place in the action context
        User u = createMockUser("logged-in-user");
        Group g = createMockGroup("admin-group");
        addUserToGroup(u, g);

        JiraTestUtil.loginUser(u);

        //Create the administer permission for that group
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, "admin-group");
        groupMagager = ManagerFactory.getGroupManager();
    }

    public void testAddUserToGroup() throws Exception
    {
        User u = createMockUser("new-user");
        Group g = createMockGroup("new-group");

        final Document document = runScript("add-user-to-group.test.add-user-to-group.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());


        final Collection groups = groupMagager.getGroupNamesForUser(u.getName());
        assertEquals(1, groups.size());
        assertEquals("new-group", groups.iterator().next());
    }

    public void testAddUserToGroupFromUser() throws Exception
    {
        Group g = createMockGroup("new-group");

        final Document document = runScript("add-user-to-group.test.add-user-to-group-from-user.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        User u = ManagerFactory.getUserManager().getUser(root.getTextString().trim());
        final Collection groups = groupMagager.getGroupNamesForUser(u.getName());
        assertEquals(1, groups.size());
        assertEquals("new-group", groups.iterator().next());
    }

    public void testAddUserToGroupFromGroup() throws Exception
    {
        User u = createMockUser("new-user");

        final Document document = runScript("add-user-to-group.test.add-user-to-group-from-group.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        final Collection groups = groupMagager.getGroupNamesForUser(u.getName());
        assertEquals(1, groups.size());
        assertEquals("new-group", groups.iterator().next());
    }

    public void testAddUserToGroupFromUserAndGroup() throws Exception
    {
        final Document document = runScript("add-user-to-group.test.add-user-to-group-from-user-and-group.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        User u = ManagerFactory.getUserManager().getUser(root.getTextString().trim());
        final Collection groups = groupMagager.getGroupNamesForUser(u.getName());
        assertEquals(1, groups.size());
        assertEquals("new-group", groups.iterator().next());
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "admin" + FS;
    }
}
