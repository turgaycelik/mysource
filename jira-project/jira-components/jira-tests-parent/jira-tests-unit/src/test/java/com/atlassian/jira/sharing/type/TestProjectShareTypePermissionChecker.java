package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.type.ShareType.Name;
import com.atlassian.jira.user.MockUser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link ProjectShareTypePermissionChecker}
 * 
 * @since v3.13
 */
public class TestProjectShareTypePermissionChecker
{
    private static final MockProjectRoleManager.MockProjectRole MOCK_ROLE = new MockProjectRoleManager.MockProjectRole(20000, "Role1", "Role1");
    private static final MockProject MOCK_PROJECT = new MockProject(10000, "PROJ", "PROJ");
    private static final SharePermissionImpl PROJECT_PERMISSION = new SharePermissionImpl(ProjectShareType.TYPE, "" + TestProjectShareTypePermissionChecker.MOCK_PROJECT.getId(), null);
    private static final SharePermissionImpl PROJECT_ROLE_PERMISSION = new SharePermissionImpl(ProjectShareType.TYPE, "" + TestProjectShareTypePermissionChecker.MOCK_PROJECT.getId(), "" + TestProjectShareTypePermissionChecker.MOCK_ROLE.getId());

    private static final SharePermission INVALID_PERM = new SharePermissionImpl(new Name("group"), "coolgroup", null);

    protected Mock permissionManagerMock;
    protected Mock projectManagerMock;
    private Mock projectRoleManagerMock;

    private User user;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("test");
        permissionManagerMock = new Mock(PermissionManager.class);
        permissionManagerMock.setStrict(true);

        projectManagerMock = new Mock(ProjectManager.class);
        projectManagerMock.setStrict(true);

        projectRoleManagerMock = new Mock(ProjectRoleManager.class);
        projectRoleManagerMock.setStrict(true);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        permissionManagerMock = null;
        projectManagerMock = null;
        projectRoleManagerMock = null;
    }

    @Test
    public void testHasPermissionNullPermission()
    {
        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(null, null, null);

        try
        {
            checker.hasPermission(user, null);
            fail("permission can not be null");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testHasPermissionWithNullParam1()
    {
        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(null, null, null);

        try
        {
            final SharePermission perm = new SharePermissionImpl(ProjectShareType.TYPE, null, null);
            checker.hasPermission(user, perm);
            fail("permission.param1 can not be null");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testHasPermissionInvalidPermission()
    {
        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(null, null, null);

        try
        {
            checker.hasPermission(user, TestProjectShareTypePermissionChecker.INVALID_PERM);
            fail("permission must be of type: " + ProjectShareType.TYPE);
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testHasPermissionNoProject()
    {
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(TestProjectShareTypePermissionChecker.PROJECT_PERMISSION.getParam1()))), null);
        final ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(projectManager, null, null);
        assertFalse(checker.hasPermission(user, TestProjectShareTypePermissionChecker.PROJECT_PERMISSION));

        projectManagerMock.verify();
    }

    @Test
    public void testHasPermissionProjectNullUserNoPermission()
    {
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(TestProjectShareTypePermissionChecker.PROJECT_PERMISSION.getParam1()))), TestProjectShareTypePermissionChecker.MOCK_PROJECT);
        final ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        permissionManagerMock.expectAndReturn("hasPermission", P.args(P.eq(Permissions.BROWSE), P.eq(TestProjectShareTypePermissionChecker.MOCK_PROJECT), P.IS_NULL), Boolean.FALSE);
        final PermissionManager permissionManager = (PermissionManager) permissionManagerMock.proxy();

        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(projectManager, permissionManager, null);
        assertFalse(checker.hasPermission(null, TestProjectShareTypePermissionChecker.PROJECT_PERMISSION));

        projectManagerMock.verify();
        permissionManagerMock.verify();
    }

    @Test
    public void testHasPermissionProjectNullUserHasPermission()
    {
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(TestProjectShareTypePermissionChecker.PROJECT_PERMISSION.getParam1()))), TestProjectShareTypePermissionChecker.MOCK_PROJECT);
        final ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        permissionManagerMock.expectAndReturn("hasPermission", P.args(P.eq(Permissions.BROWSE), P.eq(TestProjectShareTypePermissionChecker.MOCK_PROJECT), P.IS_NULL), Boolean.TRUE);
        final PermissionManager permissionManager = (PermissionManager) permissionManagerMock.proxy();

        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(projectManager, permissionManager, null);
        assertTrue(checker.hasPermission(null, TestProjectShareTypePermissionChecker.PROJECT_PERMISSION));

        projectManagerMock.verify();
        permissionManagerMock.verify();
    }

    @Test
    public void testHasPermissionProjectUserNoPermission()
    {
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(TestProjectShareTypePermissionChecker.PROJECT_PERMISSION.getParam1()))), TestProjectShareTypePermissionChecker.MOCK_PROJECT);
        final ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        permissionManagerMock.expectAndReturn("hasPermission", P.args(P.eq(Permissions.BROWSE), P.eq(TestProjectShareTypePermissionChecker.MOCK_PROJECT), P.eq(user)), Boolean.FALSE);
        final PermissionManager permissionManager = (PermissionManager) permissionManagerMock.proxy();

        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(projectManager, permissionManager, null);
        assertFalse(checker.hasPermission(user, TestProjectShareTypePermissionChecker.PROJECT_PERMISSION));

        projectManagerMock.verify();
        permissionManagerMock.verify();
    }

    @Test
    public void testHasPermissionProjectUserHasPermission()
    {
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(TestProjectShareTypePermissionChecker.PROJECT_PERMISSION.getParam1()))), TestProjectShareTypePermissionChecker.MOCK_PROJECT);
        final ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        permissionManagerMock.expectAndReturn("hasPermission", P.args(P.eq(Permissions.BROWSE), P.eq(TestProjectShareTypePermissionChecker.MOCK_PROJECT), P.eq(user)), Boolean.TRUE);
        final PermissionManager permissionManager = (PermissionManager) permissionManagerMock.proxy();

        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(projectManager, permissionManager, null);
        assertTrue(checker.hasPermission(user, TestProjectShareTypePermissionChecker.PROJECT_PERMISSION));

        projectManagerMock.verify();
        permissionManagerMock.verify();
    }

    @Test
    public void testHasPermissionProjectRoleUserHasPermission()
    {
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(TestProjectShareTypePermissionChecker.PROJECT_ROLE_PERMISSION.getParam1()))), TestProjectShareTypePermissionChecker.MOCK_PROJECT);
        final ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        final PermissionManager permissionManager = (PermissionManager) permissionManagerMock.proxy();

        projectRoleManagerMock.expectAndReturn("getProjectRole", P.args(P.eq(TestProjectShareTypePermissionChecker.MOCK_ROLE.getId())), TestProjectShareTypePermissionChecker.MOCK_ROLE);
        projectRoleManagerMock.expectAndReturn("isUserInProjectRole", P.args(P.eq(user), P.eq(TestProjectShareTypePermissionChecker.MOCK_ROLE), P.eq(TestProjectShareTypePermissionChecker.MOCK_PROJECT)), Boolean.TRUE);

        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerMock.proxy();

        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(projectManager, permissionManager, projectRoleManager);
        assertTrue(checker.hasPermission(user, TestProjectShareTypePermissionChecker.PROJECT_ROLE_PERMISSION));

        projectManagerMock.verify();
        permissionManagerMock.verify();
        projectRoleManagerMock.verify();
    }

    @Test
    public void testHasPermissionProjectRoleUserHasNoPermission()
    {
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(TestProjectShareTypePermissionChecker.PROJECT_ROLE_PERMISSION.getParam1()))), TestProjectShareTypePermissionChecker.MOCK_PROJECT);
        final ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        final PermissionManager permissionManager = (PermissionManager) permissionManagerMock.proxy();

        projectRoleManagerMock.expectAndReturn("getProjectRole", P.args(P.eq(TestProjectShareTypePermissionChecker.MOCK_ROLE.getId())), TestProjectShareTypePermissionChecker.MOCK_ROLE);
        projectRoleManagerMock.expectAndReturn("isUserInProjectRole", P.args(P.eq(user), P.eq(TestProjectShareTypePermissionChecker.MOCK_ROLE), P.eq(TestProjectShareTypePermissionChecker.MOCK_PROJECT)), Boolean.FALSE);

        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerMock.proxy();

        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(projectManager, permissionManager, projectRoleManager);
        assertFalse(checker.hasPermission(user, TestProjectShareTypePermissionChecker.PROJECT_ROLE_PERMISSION));

        projectManagerMock.verify();
        permissionManagerMock.verify();
        projectRoleManagerMock.verify();
    }

    @Test
    public void testHasPermissionProjectNullRole()
    {
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(TestProjectShareTypePermissionChecker.PROJECT_ROLE_PERMISSION.getParam1()))), TestProjectShareTypePermissionChecker.MOCK_PROJECT);
        final ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        final PermissionManager permissionManager = (PermissionManager) permissionManagerMock.proxy();

        projectRoleManagerMock.expectAndReturn("getProjectRole", P.args(P.eq(TestProjectShareTypePermissionChecker.MOCK_ROLE.getId())), null);

        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerMock.proxy();

        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(projectManager, permissionManager, projectRoleManager);
        assertFalse(checker.hasPermission(user, TestProjectShareTypePermissionChecker.PROJECT_ROLE_PERMISSION));

        projectManagerMock.verify();
        permissionManagerMock.verify();
        projectRoleManagerMock.verify();
    }

    @Test
    public void testHasPermissionProjectRoleNullUserHasNoPermission()
    {
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(TestProjectShareTypePermissionChecker.PROJECT_ROLE_PERMISSION.getParam1()))), TestProjectShareTypePermissionChecker.MOCK_PROJECT);
        final ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        final PermissionManager permissionManager = (PermissionManager) permissionManagerMock.proxy();
        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerMock.proxy();

        final ShareTypePermissionChecker checker = new ProjectShareTypePermissionChecker(projectManager, permissionManager, projectRoleManager);
        assertFalse(checker.hasPermission(null, TestProjectShareTypePermissionChecker.PROJECT_ROLE_PERMISSION));

        projectManagerMock.verify();
        permissionManagerMock.verify();
        projectRoleManagerMock.verify();
    }

}
