package com.atlassian.jira.sharing.type;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.search.PrivateShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ProjectShareTypeSearchParameter;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link com.atlassian.jira.sharing.type.ProjectShareTypeValidator}
 *
 * @since v3.13
 */
public class TestProjectShareTypeValidator
{
    private static final MockProjectRoleManager.MockProjectRole MOCK_ROLE = new MockProjectRoleManager.MockProjectRole(20000, "Role1", "Role1");
    private static final MockProject MOCK_PROJECT = new MockProject(10000, "PROJ", "PROJ");
    private static final SharePermissionImpl PROJECT_PERMISSION = new SharePermissionImpl(ProjectShareType.TYPE, "" + MOCK_PROJECT.getId(), null);
    private static final SharePermissionImpl PROJECT_ROLE_PERMISSION = new SharePermissionImpl(ProjectShareType.TYPE, "" + MOCK_PROJECT.getId(), "" + MOCK_ROLE.getId());

    private JiraServiceContext context;
    private ApplicationUser user;
    protected MockControl permissionManagerMock;
    protected PermissionManager permissionManager;
    protected Mock projectManagerMock;
    private Mock projectRoleManagerMock;

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init();
        user = new MockApplicationUser("test");
        context = createServiceContext(user);
        permissionManagerMock = MockControl.createStrictControl(PermissionManager.class);
        permissionManager = (PermissionManager) permissionManagerMock.getMock();

        projectManagerMock = new Mock(ProjectManager.class);
        projectManagerMock.setStrict(true);

        projectRoleManagerMock = new Mock(ProjectRoleManager.class);
        projectRoleManagerMock.setStrict(true);
    }

    @After
    public void tearDown() throws Exception
    {
        context = null;
        user = null;
        permissionManagerMock = null;
        permissionManager = null;
        projectManagerMock = null;
        projectRoleManagerMock = null;
    }

    @Test
    public void testCheckSharePermissionNullContext()
    {
        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(null, null, null);
        try
        {
            validator.checkSharePermission(null, PROJECT_PERMISSION);
            fail("Can not pass null context to checkSharePermission");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

    }

    @Test
    public void testCheckSharePermissionNullUserInContext()
    {
        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(null, null, null);
        JiraServiceContext nullUserContext = createServiceContext(null);

        try
        {
            validator.checkSharePermission(nullUserContext, PROJECT_PERMISSION);
            fail("Can not pass null context.user to checkSharePermission");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testCheckSharePermissionNullPermission()
    {
        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(null, null, null);

        try
        {
            validator.checkSharePermission(context, null);
            fail("Can not pass null permission to checkSharePermission");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testCheckSharePermissionInvalidType()
    {
        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(null, null, null);
        final SharePermissionImpl invalidPermission = new SharePermissionImpl(GlobalShareType.TYPE, "coolgroup", null);

        try
        {
            validator.checkSharePermission(context, invalidPermission);
            fail("Permission must of type: " + ProjectShareType.TYPE);
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testCheckSharePermissionNullParam1()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerMock.setReturnValue(true);

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionManager, null, null);

        final SharePermissionImpl nullParam1Permission = new SharePermissionImpl(ProjectShareType.TYPE, null, null);

        permissionManagerMock.replay();

        assertFalse(validator.checkSharePermission(context, nullParam1Permission));
        assertTrue(context.getErrorCollection().hasAnyErrors());

        permissionManagerMock.verify();
    }

    @Test
    public void testCheckSharePermissionInvalidParam1()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerMock.setReturnValue(true);

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionManager, null, null);

        final SharePermissionImpl nullParam1Permission = new SharePermissionImpl(ProjectShareType.TYPE, "abc", null);

        permissionManagerMock.replay();

        assertFalse(validator.checkSharePermission(context, nullParam1Permission));
        assertTrue(context.getErrorCollection().hasAnyErrors());

        permissionManagerMock.verify();
    }

    @Test
    public void testCheckSharePermissionNoPermission()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerMock.setReturnValue(false);

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionManager, null, null);

        permissionManagerMock.replay();

        assertFalse(validator.checkSharePermission(context, PROJECT_PERMISSION));
        assertTrue(context.getErrorCollection().hasAnyErrors());

        permissionManagerMock.verify();
    }

    @Test
    public void testCheckSharePermissionProjectDoesNotExist()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerMock.setReturnValue(true);

        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(PROJECT_PERMISSION.getParam1()))), null);
        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionManager, projectManager, null);

        permissionManagerMock.replay();

        assertFalse(validator.checkSharePermission(context, PROJECT_PERMISSION));
        assertTrue(context.getErrorCollection().hasAnyErrors());

        permissionManagerMock.verify();
        projectManagerMock.verify();
    }

    @Test
    public void testCheckSharePermissionNoBrowsePermission()
    {
        MockControl control = MockControl.createStrictControl(PermissionManager.class);
        PermissionManager permissionsMgr = (PermissionManager) control.getMock();
        permissionsMgr.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        control.setReturnValue(true);
        permissionsMgr.hasPermission(Permissions.BROWSE, MOCK_PROJECT, user);
        control.setReturnValue(false);
        control.replay();

        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(PROJECT_PERMISSION.getParam1()))), MOCK_PROJECT);
        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionsMgr, projectManager, null);

        permissionManagerMock.replay();

        assertFalse(validator.checkSharePermission(context, PROJECT_PERMISSION));
        assertTrue(context.getErrorCollection().hasAnyErrors());

        control.verify();
        projectManagerMock.verify();
    }

    @Test
    public void testCheckSharePermissionWithBrowsePermission()
    {
        MockControl control = MockControl.createStrictControl(PermissionManager.class);
        PermissionManager permissionsMgr = (PermissionManager) control.getMock();
        permissionsMgr.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        control.setReturnValue(true);
        permissionsMgr.hasPermission(Permissions.BROWSE, MOCK_PROJECT, user);
        control.setReturnValue(true);
        control.replay();

        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(PROJECT_PERMISSION.getParam1()))), MOCK_PROJECT);
        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionsMgr, projectManager, null);

        permissionManagerMock.replay();

        assertTrue(validator.checkSharePermission(context, PROJECT_PERMISSION));
        assertFalse(context.getErrorCollection().hasAnyErrors());

        control.verify();
        projectManagerMock.verify();
    }

    @Test
    public void testCheckSharePermissionEmptyPerm2()
    {

        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerMock.setReturnValue(true);

        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(PROJECT_PERMISSION.getParam1()))), MOCK_PROJECT);
        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionManager, projectManager, null);

        final SharePermission perm = new SharePermissionImpl(ProjectShareType.TYPE, "" + MOCK_PROJECT.getId(), "");

        permissionManagerMock.replay();

        assertFalse(validator.checkSharePermission(context, perm));
        assertTrue(context.getErrorCollection().hasAnyErrors());

        permissionManagerMock.verify();
        projectManagerMock.verify();
    }

    @Test
    public void testCheckSharePermissionInvalidPerm2()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerMock.setReturnValue(true);
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(PROJECT_PERMISSION.getParam1()))), MOCK_PROJECT);
        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionManager, projectManager, null);

        final SharePermission perm = new SharePermissionImpl(ProjectShareType.TYPE, "" + MOCK_PROJECT.getId(), "abc");

        permissionManagerMock.replay();

        assertFalse(validator.checkSharePermission(context, perm));
        assertTrue(context.getErrorCollection().hasAnyErrors());

        permissionManagerMock.verify();
        projectManagerMock.verify();
    }

    @Test
    public void testCheckSharePermssionInvalidProjectRole()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerMock.setReturnValue(true);

        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(PROJECT_ROLE_PERMISSION.getParam1()))), MOCK_PROJECT);
        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        Mock projectRoleManagerMock = new Mock(ProjectRoleManager.class);
        projectRoleManagerMock.setStrict(true);
        projectRoleManagerMock.expectAndReturn("getProjectRole", P.args(P.eq(new Long(PROJECT_ROLE_PERMISSION.getParam2()))), null);
        ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerMock.proxy();

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionManager, projectManager, projectRoleManager);

        permissionManagerMock.replay();

        assertFalse(validator.checkSharePermission(context, PROJECT_ROLE_PERMISSION));
        assertTrue(context.getErrorCollection().hasAnyErrors());

        projectManagerMock.verify();
        permissionManagerMock.verify();
        projectRoleManagerMock.verify();
    }

    @Test
    public void testCheckSharePermssionWithProjectRoleNotInProject()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerMock.setReturnValue(true);

        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(PROJECT_ROLE_PERMISSION.getParam1()))), MOCK_PROJECT);
        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        projectRoleManagerMock.expectAndReturn("getProjectRole", P.args(P.eq(new Long(PROJECT_ROLE_PERMISSION.getParam2()))), MOCK_ROLE);

        permissionManager.hasPermission(Permissions.BROWSE, MOCK_PROJECT, user);
        permissionManagerMock.setReturnValue(true);

        projectRoleManagerMock.expectAndReturn("isUserInProjectRole", P.args(P.eq(user), P.eq(MOCK_ROLE), P.eq(MOCK_PROJECT)), Boolean.FALSE);
        ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerMock.proxy();

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionManager, projectManager, projectRoleManager);

        permissionManagerMock.replay();

        assertFalse(validator.checkSharePermission(context, PROJECT_ROLE_PERMISSION));
        assertTrue(context.getErrorCollection().hasAnyErrors());

        projectManagerMock.verify();
        permissionManagerMock.verify();
        projectRoleManagerMock.verify();
    }

    @Test
    public void testCheckSharePermssionWithProjectRoleInProjectNoBrowse()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerMock.setReturnValue(true);

        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(PROJECT_ROLE_PERMISSION.getParam1()))), MOCK_PROJECT);
        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        projectRoleManagerMock.expectAndReturn("getProjectRole", P.args(P.eq(new Long(PROJECT_ROLE_PERMISSION.getParam2()))), MOCK_ROLE);

        permissionManager.hasPermission(Permissions.BROWSE, MOCK_PROJECT, user);
        permissionManagerMock.setReturnValue(false);

        ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerMock.proxy();

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionManager, projectManager, projectRoleManager);

        permissionManagerMock.replay();

        assertFalse(validator.checkSharePermission(context, PROJECT_ROLE_PERMISSION));
        assertTrue(context.getErrorCollection().hasAnyErrors());

        projectManagerMock.verify();
        permissionManagerMock.verify();
        projectRoleManagerMock.verify();
    }

    @Test
    public void testCheckSharePermssionWithProjectRoleInProject()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerMock.setReturnValue(true);

        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(PROJECT_ROLE_PERMISSION.getParam1()))), MOCK_PROJECT);
        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();

        projectRoleManagerMock.expectAndReturn("getProjectRole", P.args(P.eq(new Long(PROJECT_ROLE_PERMISSION.getParam2()))), MOCK_ROLE);

        permissionManager.hasPermission(Permissions.BROWSE, MOCK_PROJECT, user);
        permissionManagerMock.setReturnValue(true);

        projectRoleManagerMock.expectAndReturn("isUserInProjectRole", P.args(P.eq(user), P.eq(MOCK_ROLE), P.eq(MOCK_PROJECT)), Boolean.TRUE);
        ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerMock.proxy();

        ProjectShareTypeValidator validator = new ProjectShareTypeValidator(permissionManager, projectManager, projectRoleManager);

        permissionManagerMock.replay();

        assertTrue(validator.checkSharePermission(context, PROJECT_ROLE_PERMISSION));
        assertFalse(context.getErrorCollection().hasAnyErrors());

        projectManagerMock.verify();
        permissionManagerMock.verify();
        projectRoleManagerMock.verify();
    }

    @Test
    public void testCheckSearchParameterInvalidProject() throws Exception
    {
        final Long projectId = new Long(123);
        final Long roleId = null;

        final MockControl projectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager projectManager = (ProjectManager) projectManagerControl.getMock();

        final MockControl projectRoleManagerControl = MockControl.createStrictControl(ProjectRoleManager.class);
        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerControl.getMock();

        projectManager.getProjectObj(projectId);
        projectManagerControl.setReturnValue(null);

        projectManagerControl.replay();
        projectRoleManagerControl.replay();

        final JiraServiceContext ctx = createServiceContext(user);

        ProjectShareTypeValidator projectShareTypeValidator = new ProjectShareTypeValidator(permissionManager, projectManager, projectRoleManager);

        boolean result = projectShareTypeValidator.checkSearchParameter(ctx, new ProjectShareTypeSearchParameter(projectId, roleId));
        assertFalse(result);

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrors().get(ShareTypeValidator.ERROR_KEY).equals("common.sharing.searching.exception.project.does.not.exist"));

        projectManagerControl.verify();
        projectRoleManagerControl.verify();
    }

    @Test
    public void testCheckSearchParameterInvalidRole() throws Exception
    {
        final Long projectId = new Long(123);
        final Long roleId = new Long(456);
        final MockProject projectObj = new MockProject(projectId);

        final MockControl projectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager projectManager = (ProjectManager) projectManagerControl.getMock();

        final MockControl projectRoleManagerControl = MockControl.createStrictControl(ProjectRoleManager.class);
        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerControl.getMock();

        projectManager.getProjectObj(projectId);
        projectManagerControl.setReturnValue(projectObj);

        projectRoleManager.getProjectRole(roleId);
        projectRoleManagerControl.setReturnValue(null);

        projectManagerControl.replay();
        projectRoleManagerControl.replay();

        final JiraServiceContext ctx = createServiceContext(null);
        ProjectShareTypeValidator projectShareTypeValidator = new ProjectShareTypeValidator(permissionManager, projectManager, projectRoleManager);
        boolean result = projectShareTypeValidator.checkSearchParameter(ctx, new ProjectShareTypeSearchParameter(projectId, roleId));
        assertFalse(result);

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrors().get(ShareTypeValidator.ERROR_KEY).equals("common.sharing.searching.exception.project.role.does.not.exist"));

        projectManagerControl.verify();
        projectRoleManagerControl.verify();
    }

    @Test
    public void testCheckSearchParameterNotInRole() throws Exception
    {
        final Long projectId = new Long(123);
        final Long roleId = new Long(456);
        final MockProject projectObj = new MockProject(projectId);
        final ProjectRole projectRole = new MockProjectRoleManager.MockProjectRole(roleId.longValue(), "name", "desc");

        final MockControl projectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager projectManager = (ProjectManager) projectManagerControl.getMock();

        final MockControl projectRoleManagerControl = MockControl.createStrictControl(ProjectRoleManager.class);
        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerControl.getMock();

        projectManager.getProjectObj(projectId);
        projectManagerControl.setReturnValue(projectObj);

        projectRoleManager.getProjectRole(roleId);
        projectRoleManagerControl.setReturnValue(projectRole);

        projectRoleManager.isUserInProjectRole(user, projectRole, projectObj);
        projectRoleManagerControl.setReturnValue(false);

        projectManagerControl.replay();
        projectRoleManagerControl.replay();

        final JiraServiceContext ctx = createServiceContext(user);
        ProjectShareTypeValidator projectShareTypeValidator = new ProjectShareTypeValidator(permissionManager, projectManager, projectRoleManager);
        boolean result = projectShareTypeValidator.checkSearchParameter(ctx, new ProjectShareTypeSearchParameter(projectId, roleId));
        assertFalse(result);

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrors().get(ShareTypeValidator.ERROR_KEY).equals("common.sharing.searching.exception.user.not.in.project.role"));

        projectManagerControl.verify();
        projectRoleManagerControl.verify();
    }

    @Test
    public void testCheckSearchParameterAllCool() throws Exception
    {
        final Long projectId = new Long(123);
        final Long roleId = new Long(456);
        final MockProject projectObj = new MockProject(projectId);
        final ProjectRole projectRole = new MockProjectRoleManager.MockProjectRole(roleId.longValue(), "name", "desc");

        final MockControl projectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager projectManager = (ProjectManager) projectManagerControl.getMock();

        final MockControl projectRoleManagerControl = MockControl.createStrictControl(ProjectRoleManager.class);
        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerControl.getMock();

        projectManager.getProjectObj(projectId);
        projectManagerControl.setReturnValue(projectObj);

        projectRoleManager.getProjectRole(roleId);
        projectRoleManagerControl.setReturnValue(projectRole);

        projectRoleManager.isUserInProjectRole(user, projectRole, projectObj);
        projectRoleManagerControl.setReturnValue(true);

        projectManagerControl.replay();
        projectRoleManagerControl.replay();

        final JiraServiceContext ctx = createServiceContext(user);
        ProjectShareTypeValidator projectShareTypeValidator = new ProjectShareTypeValidator(permissionManager, projectManager, projectRoleManager);
        boolean result = projectShareTypeValidator.checkSearchParameter(ctx, new ProjectShareTypeSearchParameter(projectId, roleId));
        assertTrue(result);

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertFalse(errorCollection.hasAnyErrors());

        projectManagerControl.verify();
        projectRoleManagerControl.verify();
    }

    @Test
    public void testCheckSearchParameterNullArgs() throws Exception
    {
        final Long projectId = null;
        final Long roleId = null;

        final MockControl projectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager projectManager = (ProjectManager) projectManagerControl.getMock();

        final MockControl projectRoleManagerControl = MockControl.createStrictControl(ProjectRoleManager.class);
        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) projectRoleManagerControl.getMock();

        projectManagerControl.replay();
        projectRoleManagerControl.replay();

        final JiraServiceContext ctx = createServiceContext(user);
        ProjectShareTypeValidator projectShareTypeValidator = new ProjectShareTypeValidator(permissionManager, projectManager, projectRoleManager);
        boolean result = projectShareTypeValidator.checkSearchParameter(ctx, new ProjectShareTypeSearchParameter(projectId, roleId));
        assertTrue(result);

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertFalse(errorCollection.hasAnyErrors());

        projectManagerControl.verify();
        projectRoleManagerControl.verify();
    }

    @Test
    public void testCheckSearchParameterBadArgs() throws Exception
    {

        ProjectShareTypeValidator projectShareTypeValidator = new ProjectShareTypeValidator(permissionManager, null, null);
        final JiraServiceContext ctx = createServiceContext(user);
        try
        {
            boolean result = projectShareTypeValidator.checkSearchParameter(ctx, PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);
            fail("Should not allow this type of parameter");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

    }

    private JiraServiceContext createServiceContext(ApplicationUser user)
    {
        return new JiraServiceContextImpl(user, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean()
                {
                    public String getText(final String key)
                    {
                        return key;
                    }

                    public String getText(final String key, final String value1)
                    {
                        return key;
                    }

                    public String getText(final String key, final String value1, final String value2)
                    {
                        return key;
                    }

                    public String getText(final String key, final String value1, final String value2, final String value3)
                    {
                        return key;
                    }

                    public String getText(final String key, final Object parameters)
                    {
                        return key;
                    }
                };
            }
        };
    }
}
