/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.security.Permissions;

import electric.xml.Document;
import electric.xml.Element;

public class TestAddNewUser extends AbstractJellyTestCase
{
    public TestAddNewUser(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        //Create user and place in the action context
        User u = createMockUser("logged-in-user");
        JiraTestUtil.loginUser(u);

        //Create the administer permission for all users
        ManagerFactory.getGroupManager().addUserToGroup(u, ManagerFactory.getGroupManager().createGroup("admins"));
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, "admins");
    }

    public void testAddNewUser() throws Exception
    {
        final Document document = runScript("add-new-user.test.add-new-user.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        assertEquals("new-user", root.getTextString().trim());
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "admin" + FS;
    }
}
