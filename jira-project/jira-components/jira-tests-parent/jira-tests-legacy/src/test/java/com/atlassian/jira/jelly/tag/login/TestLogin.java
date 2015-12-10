/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.login;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import electric.xml.Document;
import electric.xml.Element;
import webwork.action.ActionContext;

public class TestLogin extends AbstractJellyTestCase
{

    JiraAuthenticationContext authCtx;
    User oldUser;

    public TestLogin(String s)
    {
        super(s);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        authCtx = ComponentAccessor.getJiraAuthenticationContext();
        oldUser = authCtx.getLoggedInUser();

        User testUser = createMockUser("misc-user");
        Group testGroup = createMockGroup("group1");
        addUserToGroup(testUser, testGroup);
        // Allow the user to login
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.USE, "group1");
    }

    protected void tearDown() throws Exception
    {
        ManagerFactory.getGlobalPermissionManager().removePermission(Permissions.USE, null);
        authCtx.setLoggedInUser(oldUser);
        authCtx = null;

        super.tearDown();
    }

    public void testLoginWithUsernameAndPassword() throws Exception
    {
        authCtx.setLoggedInUser(createMockUser("prev-user"));

        final String scriptFilename = "login.test.log-user-in.jelly";
        Document document = runScript(scriptFilename);
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());
        assertEquals("misc-user", root.getTextString().trim());

        final User expectedUser = authCtx.getLoggedInUser();
        assertNotNull(expectedUser);
        assertEquals("prev-user", expectedUser.getName());
    }

    public void testLoginWithUsernameOnly() throws Exception
    {
        authCtx.setLoggedInUser(createMockUser("prev-user"));


        final String scriptFilename = "login.test.log-user-in-name-only.jelly";
        Document document = runScript(scriptFilename);
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());
        assertEquals("misc-user", root.getTextString().trim());

        final User expectedUser = authCtx.getLoggedInUser();
        assertNotNull(expectedUser);
        assertEquals("prev-user", expectedUser.getName());
    }

    public void testLoggedIn() throws Exception
    {
        User u = createMockUser("logged-in-user");
        JiraTestUtil.loginUser(u);

        final String scriptFilename = "login.test.logged-in-user.jelly";

        Document document = runScript(scriptFilename);
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());
        assertEquals("logged-in-user", root.getTextString().trim());
    }

    public void testInvalidUser()
    {
        try
        {
            final String scriptFilename = "login.test.invalid-user.jelly";
            Document document = runScript(scriptFilename);
            final Element root = document.getRoot();
            Element errorElement = root.getElement("Error");
            assertEquals("User: invalid-user does not exist", errorElement.getAttribute("action"));
        }
        catch (Exception e)
        {
            fail("Exception was throw:" + e.getMessage());
        }
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "login" + FS;
    }
}
