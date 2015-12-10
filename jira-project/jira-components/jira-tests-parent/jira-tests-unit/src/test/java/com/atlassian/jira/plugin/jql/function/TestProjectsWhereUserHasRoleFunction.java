package com.atlassian.jira.plugin.jql.function;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestProjectsWhereUserHasRoleFunction extends MockControllerTestCase
{
    private ApplicationUser theUser;
    private QueryCreationContext queryCreationContext;
    private TerminalClause terminalClause = null;
    private List<Project> projectsList1 = new ArrayList<Project>();
    private ProjectRole projectRole1 = new ProjectRoleImpl(Long.valueOf(1), "Role1", "Role desc 1");
    private ProjectRole projectRole2 = new ProjectRoleImpl(Long.valueOf(2), "Role2", "Role desc 2");
    private Project project1 = new MockProject(21l, "c1");
    private Project project2 = new MockProject(22l, "c2");
    private Project project3 = new MockProject(23l, "c3");
    private Project project4 = new MockProject(24l, "c4");

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockApplicationUser("fred");
        queryCreationContext = new QueryCreationContextImpl(theUser);


        projectsList1.add(project1);
        projectsList1.add(project2);
        projectsList1.add(project3);
        projectsList1.add(project4);

    }

    @Test
    public void testDataType() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectRoleManager projectRoleManager = EasyMock.createMock(ProjectRoleManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasRoleFunction projectsWhereUserHasRoleFunction = new ProjectsWhereUserHasRoleFunction(permissionManager, projectRoleManager, projectManager, userUtil);

        assertEquals(JiraDataTypes.PROJECT, projectsWhereUserHasRoleFunction.getDataType());
    }

    @Test
    public void testValidateWrongArgs() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectRoleManager projectRoleManager = EasyMock.createMock(ProjectRoleManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasRoleFunction projectsWhereUserHasRoleFunction = new ProjectsWhereUserHasRoleFunction(permissionManager, projectRoleManager, projectManager, userUtil);
        projectsWhereUserHasRoleFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasRole", true));

        MessageSet messageSet = projectsWhereUserHasRoleFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasRole"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasRole' expected '1' arguments but received '0'.", messageSet.getErrorMessages().iterator().next());

        messageSet = projectsWhereUserHasRoleFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasRole", "badArg1", "badArg2", "badArg3"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasRole' expected '1' arguments but received '3'.", messageSet.getErrorMessages().iterator().next());

        EasyMock.expect(projectRoleManager.getProjectRole("BadRole")).andReturn(null);
        replay(userUtil, projectRoleManager);

        messageSet = projectsWhereUserHasRoleFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasRole", "BadRole"), terminalClause);
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasRole' can not generate a list of projects for role 'BadRole'; the role does not exist.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateHappyPath() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectRoleManager projectRoleManager = EasyMock.createMock(ProjectRoleManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasRoleFunction projectsWhereUserHasRoleFunction = new ProjectsWhereUserHasRoleFunction(permissionManager, projectRoleManager, projectManager, userUtil);
        projectsWhereUserHasRoleFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasRole", true));

        // No user name supplied
        EasyMock.expect(projectRoleManager.getProjectRole("Role1")).andReturn(projectRole1);
        // User name supplied
        EasyMock.expect(userUtil.getUserByName("fred")).andReturn(theUser);
        EasyMock.expect(projectRoleManager.getProjectRole("Role1")).andReturn(projectRole1);
        replay(userUtil, projectRoleManager);

        MessageSet messageSet = projectsWhereUserHasRoleFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasRole", "Role1"), terminalClause);
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testValidateAnonymous()
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectRoleManager projectRoleManager = EasyMock.createMock(ProjectRoleManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasRoleFunction projectsWhereUserHasRoleFunction = new ProjectsWhereUserHasRoleFunction(permissionManager, projectRoleManager, projectManager, userUtil);
        projectsWhereUserHasRoleFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasRole", true));

        // No user name supplied
        EasyMock.expect(projectRoleManager.getProjectRole("Role1")).andReturn(projectRole1);
        replay(projectRoleManager);

        final MessageSet messageSet = projectsWhereUserHasRoleFunction.validate(null, new FunctionOperand("projectsWhereUserHasRole", "Role1"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasRole' cannot be called as anonymous user.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectRoleManager projectRoleManager = EasyMock.createMock(ProjectRoleManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasRoleFunction projectsWhereUserHasRoleFunction = new ProjectsWhereUserHasRoleFunction(permissionManager, projectRoleManager, projectManager, userUtil);
        projectsWhereUserHasRoleFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasRole", true));

        // No user name supplied
        EasyMock.expect(userUtil.getUserByName("fred")).andReturn(theUser);
        EasyMock.expect(projectRoleManager.getProjectRole("Role1")).andReturn(projectRole1);
        EasyMock.expect(projectManager.getProjectObjects()).andReturn(projectsList1);
        EasyMock.expect(projectRoleManager.isUserInProjectRole(theUser, projectRole1, project1)).andReturn(true);
        EasyMock.expect(projectRoleManager.isUserInProjectRole(theUser, projectRole1, project2)).andReturn(true);
        EasyMock.expect(projectRoleManager.isUserInProjectRole(theUser, projectRole1, project3)).andReturn(true);
        EasyMock.expect(projectRoleManager.isUserInProjectRole(theUser, projectRole1, project4)).andReturn(false);
        for (Project project : projectsList1)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, theUser)).andReturn(true);
        }

        replay(userUtil, projectRoleManager, projectManager, permissionManager);

        List<QueryLiteral> list = projectsWhereUserHasRoleFunction.getValues(queryCreationContext, new FunctionOperand("projectsWhereUserHasRole", "Role1"), terminalClause);
        assertEquals(3, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());
        assertEquals(new Long(23), list.get(2).getLongValue());
        verify();

        reset(userUtil, projectRoleManager, projectManager, permissionManager);
        // No permissions on projects 22 & 23
        // No user name supplied
        EasyMock.expect(userUtil.getUserByName("fred")).andReturn(theUser);
        EasyMock.expect(projectRoleManager.getProjectRole("Role1")).andReturn(projectRole1);
        EasyMock.expect(projectManager.getProjectObjects()).andReturn(projectsList1);
        EasyMock.expect(projectRoleManager.isUserInProjectRole(theUser, projectRole1, project1)).andReturn(true);
        EasyMock.expect(projectRoleManager.isUserInProjectRole(theUser, projectRole1, project2)).andReturn(true);
        EasyMock.expect(projectRoleManager.isUserInProjectRole(theUser, projectRole1, project3)).andReturn(true);
        EasyMock.expect(projectRoleManager.isUserInProjectRole(theUser, projectRole1, project4)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(0), theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(1), theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(2), theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(3), theUser)).andReturn(true);

        replay(userUtil, projectRoleManager, projectManager, permissionManager);

        list = projectsWhereUserHasRoleFunction.getValues(queryCreationContext, new FunctionOperand("projectsWhereUserHasRole", "Role1"), terminalClause);
        assertEquals(1, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        verify();
    }

    @Test
    public void testGetValuesAnonymous()
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectRoleManager projectRoleManager = EasyMock.createMock(ProjectRoleManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasRoleFunction projectsWhereUserHasRoleFunction = new ProjectsWhereUserHasRoleFunction(permissionManager, projectRoleManager, projectManager, userUtil);
        projectsWhereUserHasRoleFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasRole", true));

        for (Project project : projectsList1)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, (ApplicationUser) null)).andReturn(true);
        }

        replay(userUtil, projectRoleManager, projectManager, permissionManager);

        List<QueryLiteral> list = projectsWhereUserHasRoleFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("projectsWhereUserHasRole", "Role1"), terminalClause);
        assertTrue(list.isEmpty());
        verify();

        // No permission for anonymous user
        reset(userUtil, projectRoleManager, projectManager, permissionManager);
        for (Project project : projectsList1)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, (ApplicationUser) null)).andReturn(false);
        }

        replay(userUtil, projectRoleManager, projectManager, permissionManager);

        list = projectsWhereUserHasRoleFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("projectsWhereUserHasRole", "Role1"), terminalClause);
        assertTrue(list.isEmpty());
        verify();
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectRoleManager projectRoleManager = EasyMock.createMock(ProjectRoleManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasRoleFunction projectsWhereUserHasRoleFunction = new ProjectsWhereUserHasRoleFunction(permissionManager, projectRoleManager, projectManager, userUtil);
        projectsWhereUserHasRoleFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasRole", true));

        assertEquals(1, projectsWhereUserHasRoleFunction.getMinimumNumberOfExpectedArguments());
    }

}
