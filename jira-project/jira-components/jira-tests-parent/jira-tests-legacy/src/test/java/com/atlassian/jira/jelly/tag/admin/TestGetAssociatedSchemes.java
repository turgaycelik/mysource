package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.jelly.tag.projectroles.AbstractProjectRolesTest;
import com.atlassian.jira.jelly.tag.projectroles.MockProjectRoleService;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;

/**
 * Tests GetAssociatedSchemes jelly tag.
 */
public class TestGetAssociatedSchemes extends AbstractJellyTestCase
{
    private User u;
    private Group g;

    protected void setUp() throws Exception
    {
        super.setUp();
        //Create user and place in the action context
        u = createMockUser("logged-in-user");
        g = createMockGroup("admin-group");
        ComponentAccessor.getGroupManager().addUserToGroup(u, g);
        JiraTestUtil.loginUser(u);
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "admin" + FS;
    }

    public TestGetAssociatedSchemes(String s)
    {
        super(s);
    }

    public void testGetAssociatedNotificationSchemes() throws Exception
    {
        AbstractProjectRolesTest.swapProjectRoleService(new MockProjectRoleService(){

            @Override
            public Collection getAssociatedNotificationSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
            {
                return EasyList.build(new Scheme(GetAssociatedSchemes.SCHEME_TYPE_NOTIFICATION, "myNotificationScheme"));
            }

            @Override
            public ProjectRole getProjectRole(User currentUser, Long id, ErrorCollection errorCollection)
            {
                return MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
            }

        });
        runScriptAndAssertTextResultEquals("myNotificationScheme", "get-associated-schemes.test.notification.jelly");
    }

    public void testGetAssociatedPermissionSchemes() throws Exception
    {
        AbstractProjectRolesTest.swapProjectRoleService(new MockProjectRoleService(){

            @Override
            public Collection getAssociatedPermissionSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
            {
                return EasyList.build(new Scheme(GetAssociatedSchemes.SCHEME_TYPE_PERMISSION, "myPermissionScheme"));
            }

            @Override
            public ProjectRole getProjectRole(User currentUser, Long id, ErrorCollection errorCollection)
            {
                return MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
            }

        });
        runScriptAndAssertTextResultEquals("myPermissionScheme", "get-associated-schemes.test.permission.jelly");
    }
}

