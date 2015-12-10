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
public class TestProjectsLeadByUserFunction extends MockControllerTestCase
{
    private ApplicationUser theUser;
    private QueryCreationContext queryCreationContext;
    private TerminalClause terminalClause = null;
    private List<Project> projectsList1 = new ArrayList<Project>();
    private List<Project> projectsList2 = new ArrayList<Project>();

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockApplicationUser("fred");
        queryCreationContext = new QueryCreationContextImpl(theUser);

        Project project1 = new MockProject(21l, "c1");
        Project project2 = new MockProject(22l, "c2");
        Project project3 = new MockProject(23l, "c3");
        Project project4 = new MockProject(24l, "c4");

        projectsList1.add(project1);
        projectsList1.add(project2);
        projectsList1.add(project3);
        projectsList1.add(project4);

        projectsList2.add(project1);
        projectsList2.add(project2);
    }

    @Test
    public void testDataType() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsLeadByUserFunction projectsLeadByUserFunction = new ProjectsLeadByUserFunction(permissionManager, projectManager, userUtil);

        assertEquals(JiraDataTypes.PROJECT, projectsLeadByUserFunction.getDataType());
    }

    @Test
    public void testValidateWrongArgs() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsLeadByUserFunction projectsLeadByUserFunction = new ProjectsLeadByUserFunction(permissionManager, projectManager, userUtil);
        projectsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("projectsLeadByUser", true));

        MessageSet messageSet = projectsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("projectsLeadByUser", "badArg1", "badArg2"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsLeadByUser' expected between '0' and '1' arguments but received '2'.", messageSet.getErrorMessages().iterator().next());

        EasyMock.expect(userUtil.getUserByName("badUser")).andReturn(null);
        replay(userUtil);
        messageSet = projectsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("projectsLeadByUser", "badUser"), terminalClause);
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsLeadByUser' can not generate a list of projects for user 'badUser'; the user does not exist.", messageSet.getErrorMessages().iterator().next());

    }

    @Test
    public void testValidateHappyPath() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsLeadByUserFunction projectsLeadByUserFunction = new ProjectsLeadByUserFunction(permissionManager, projectManager, userUtil);
        projectsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("projectsLeadByUser", true));

        // No user name supplied
        MessageSet messageSet = projectsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("projectsLeadByUser"), terminalClause);
        assertFalse(messageSet.hasAnyErrors());

        // One valid user name supplied
        EasyMock.expect(userUtil.getUserByName("fred")).andReturn(theUser);
        replay(userUtil);
        messageSet = projectsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("projectsLeadByUser", "fred"), terminalClause);
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testValidateAnonymous()
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsLeadByUserFunction projectsLeadByUserFunction = new ProjectsLeadByUserFunction(permissionManager, projectManager, userUtil);
        projectsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("projectsLeadByUser", true));

        final MessageSet messageSet = projectsLeadByUserFunction.validate(null, new FunctionOperand("projectsLeadByUser"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsLeadByUser' cannot be called as anonymous user.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsLeadByUserFunction projectsLeadByUserFunction = new ProjectsLeadByUserFunction(permissionManager, projectManager, userUtil);
        projectsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("projectsLeadByUser", true));

        EasyMock.expect(userUtil.getUserByName("fred")).andReturn(theUser);
        EasyMock.expect(projectManager.getProjectsLeadBy(theUser)).andReturn(projectsList1);
        for (Project project : projectsList1)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, theUser)).andReturn(true);
        }
        ApplicationUser bill = new MockApplicationUser("bill");
        EasyMock.expect(userUtil.getUserByName("bill")).andReturn(bill);
        EasyMock.expect(projectManager.getProjectsLeadBy(bill)).andReturn(projectsList2);
        for (Project project : projectsList2)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, theUser)).andReturn(true);
        }
        replay(userUtil, projectManager, permissionManager);

        List<QueryLiteral> list = projectsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("projectsLeadByUser"), terminalClause);
        assertEquals(4, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());
        assertEquals(new Long(23), list.get(2).getLongValue());
        assertEquals(new Long(24), list.get(3).getLongValue());

        list = projectsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("projectsLeadByUser", "bill"), terminalClause);
        assertEquals(2, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());
        verify();

        // No permissions on projects 22 & 23
        reset(userUtil, projectManager, permissionManager);
        EasyMock.expect(userUtil.getUserByName("fred")).andReturn(theUser);
        EasyMock.expect(projectManager.getProjectsLeadBy(theUser)).andReturn(projectsList1);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(0), theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(1), theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(2), theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(3), theUser)).andReturn(true);

        EasyMock.expect(userUtil.getUserByName("bill")).andReturn(bill);
        EasyMock.expect(projectManager.getProjectsLeadBy(bill)).andReturn(projectsList2);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList2.get(0), theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList2.get(1), theUser)).andReturn(false);
        replay(userUtil, projectManager, permissionManager);

        list = projectsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("projectsLeadByUser"), terminalClause);
        assertEquals(2, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(24), list.get(1).getLongValue());

        list = projectsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("projectsLeadByUser", "bill"), terminalClause);
        assertEquals(1, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        verify();

    }

    @Test
    public void testGetValuesAnonymous()
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsLeadByUserFunction projectsLeadByUserFunction = new ProjectsLeadByUserFunction(permissionManager, projectManager, userUtil);
        projectsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("projectsLeadByUser", true));

        ApplicationUser bill = new MockApplicationUser("bill");
        EasyMock.expect(userUtil.getUserByName("bill")).andReturn(bill);
        EasyMock.expect(projectManager.getProjectsLeadBy(bill)).andReturn(projectsList2);
        for (Project project : projectsList2)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, (ApplicationUser) null)).andReturn(true);
        }
        replay(userUtil, projectManager, permissionManager);

        List<QueryLiteral> list = projectsLeadByUserFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("projectsLeadByUser"), terminalClause);
        assertTrue(list.isEmpty());

        list = projectsLeadByUserFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("projectsLeadByUser", "bill"), terminalClause);
        assertEquals(2, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());
        verify();

        // No permissions for anonymous user.
        reset(userUtil, projectManager, permissionManager);
        EasyMock.expect(userUtil.getUserByName("bill")).andReturn(bill);
        EasyMock.expect(projectManager.getProjectsLeadBy(bill)).andReturn(projectsList2);
        for (Project project : projectsList2)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, (ApplicationUser) null)).andReturn(false);
        }
        replay(userUtil, projectManager, permissionManager);

        list = projectsLeadByUserFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("projectsLeadByUser"), terminalClause);
        assertTrue(list.isEmpty());

        list = projectsLeadByUserFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("projectsLeadByUser", "bill"), terminalClause);
        assertTrue(list.isEmpty());
        verify();
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsLeadByUserFunction projectsLeadByUserFunction = new ProjectsLeadByUserFunction(permissionManager, projectManager, userUtil);
        projectsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("projectsLeadByUser", true));

        assertEquals(0, projectsLeadByUserFunction.getMinimumNumberOfExpectedArguments());
    }

}
