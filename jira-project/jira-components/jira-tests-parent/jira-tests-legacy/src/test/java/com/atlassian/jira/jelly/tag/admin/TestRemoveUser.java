package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.security.Permissions;
import electric.xml.Document;
import electric.xml.Element;

public class TestRemoveUser extends AbstractJellyTestCase
{
    public TestRemoveUser(String s)
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

    public void testRemoveAddedUser() throws Exception
    {
        final Document document = runScript("remove-user.test.remove-added-user.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        assertEquals("added-user added\nadded-user removed", root.getTextString().trim());
    }

    public void testRemoveExistingUser() throws Exception
    {
        User u = createMockUser("existing-user");

        final Document document = runScript("remove-user.test.remove-existing-user.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        assertEquals("existing-user removed", root.getTextString().trim());
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "admin" + FS;
    }
}
