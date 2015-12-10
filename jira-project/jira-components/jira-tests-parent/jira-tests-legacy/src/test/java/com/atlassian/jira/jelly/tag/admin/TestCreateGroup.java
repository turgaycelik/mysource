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
import com.atlassian.seraph.auth.DefaultAuthenticator;
import electric.xml.Document;
import electric.xml.Element;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import webwork.action.ActionContext;

public class TestCreateGroup extends AbstractJellyTestCase
{
    public TestCreateGroup(String s)
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

    public void testCreateGroup() throws Exception
    {
        final Document document = runScript("create-group.test.create-group.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        assertEquals("new-group", root.getTextString().trim());
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "admin" + FS;
    }
}
