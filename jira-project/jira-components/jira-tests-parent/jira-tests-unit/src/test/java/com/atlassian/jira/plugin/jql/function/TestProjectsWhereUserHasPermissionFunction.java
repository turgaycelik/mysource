package com.atlassian.jira.plugin.jql.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.permission.MockProjectPermission;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import com.google.common.collect.Lists;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestProjectsWhereUserHasPermissionFunction extends MockControllerTestCase
{
    private ApplicationUser theUser;
    private QueryCreationContext queryCreationContext;
    private TerminalClause terminalClause = null;
    private List<Project> projectsList1 = new ArrayList<Project>();
    private List<Project> projectsList2 = new ArrayList<Project>();

    @Before
    public void setUp() throws Exception
    {
        componentAccessorWorker.addMock(I18nHelper.BeanFactory.class, new MockI18nBean.MockI18nBeanFactory());

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
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        final I18nHelper.BeanFactory i18nHelperFactory = EasyMock.createMock(I18nHelper.BeanFactory.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, userUtil, i18nHelperFactory);

        assertEquals(JiraDataTypes.PROJECT, projectsWhereUserHasPermissionFunction.getDataType());
    }

    @Test
    public void testValidateWrongArgs() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        final I18nHelper.BeanFactory i18nHelperFactory = EasyMock.createMock(I18nHelper.BeanFactory.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, userUtil, i18nHelperFactory);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        MessageSet messageSet = projectsWhereUserHasPermissionFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasPermission"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasPermission' expected '1' arguments but received '0'.", messageSet.getErrorMessages().iterator().next());

        messageSet = projectsWhereUserHasPermissionFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasPermission", "badArg1", "badArg2", "badArg3"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasPermission' expected '1' arguments but received '3'.", messageSet.getErrorMessages().iterator().next());

        EasyMock.expect(permissionManager.getAllProjectPermissions()).andReturn(Collections.<ProjectPermission>emptyList());

        replay(userUtil, permissionManager);

        messageSet = projectsWhereUserHasPermissionFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasPermission", "BadPermission"), terminalClause);
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasPermission' can not generate a list of projects for permission 'BadPermission'; the permission does not exist.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateHappyPath() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        final I18nHelper.BeanFactory i18nHelperFactory = EasyMock.createMock(I18nHelper.BeanFactory.class);
        final I18nHelper i18nHelper = EasyMock.createMock(I18nHelper.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, userUtil, i18nHelperFactory);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        EasyMock.expect(permissionManager.getAllProjectPermissions()).andReturn(Lists.<ProjectPermission>newArrayList(new MockProjectPermission("key", "nameKey", null, null)));
        EasyMock.expect(i18nHelperFactory.getInstance(ENGLISH)).andReturn(i18nHelper);
        EasyMock.expect(i18nHelper.getText("nameKey")).andReturn("View Development Tools");

        replay(permissionManager, i18nHelperFactory, i18nHelper);

        // No user name supplied
        MessageSet messageSet = projectsWhereUserHasPermissionFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasPermission", "View Development Tools"), terminalClause);
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testValidateAnonymous()
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        final I18nHelper.BeanFactory i18nHelperFactory = EasyMock.createMock(I18nHelper.BeanFactory.class);
        final I18nHelper i18nHelper = EasyMock.createMock(I18nHelper.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, userUtil, i18nHelperFactory);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        EasyMock.expect(permissionManager.getAllProjectPermissions()).andReturn(Lists.<ProjectPermission>newArrayList(new MockProjectPermission("key", "nameKey", null, null)));
        EasyMock.expect(i18nHelperFactory.getInstance(ENGLISH)).andReturn(i18nHelper);
        EasyMock.expect(i18nHelper.getText("nameKey")).andReturn("View Development Tools");

        replay(permissionManager, i18nHelperFactory, i18nHelper);

        final MessageSet messageSet = projectsWhereUserHasPermissionFunction.validate(null, new FunctionOperand("projectsWhereUserHasPermission", "View Development Tools"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasPermission' cannot be called as anonymous user.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        final I18nHelper.BeanFactory i18nHelperFactory = EasyMock.createMock(I18nHelper.BeanFactory.class);
        final I18nHelper i18nHelper = EasyMock.createMock(I18nHelper.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, userUtil, i18nHelperFactory);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        EasyMock.expect(permissionManager.getAllProjectPermissions()).andReturn(Lists.<ProjectPermission>newArrayList(new MockProjectPermission("key", "nameKey", null, null)));
        EasyMock.expect(i18nHelperFactory.getInstance(ENGLISH)).andReturn(i18nHelper).anyTimes();
        EasyMock.expect(i18nHelper.getText("nameKey")).andReturn("View Development Tools").anyTimes();

        EasyMock.expect(userUtil.getUserByName("fred")).andReturn(theUser);
        EasyMock.expect(permissionManager.getProjects(new ProjectPermissionKey("key"), theUser)).andReturn(projectsList1);
        for (Project project : projectsList1)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, theUser)).andReturn(true);
        }
        replay(userUtil, permissionManager, i18nHelperFactory, i18nHelper);

        List<QueryLiteral> list = projectsWhereUserHasPermissionFunction.getValues(queryCreationContext, new FunctionOperand("projectsWhereUserHasPermission", "View Development Tools"), terminalClause);
        assertEquals(4, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());
        assertEquals(new Long(23), list.get(2).getLongValue());
        assertEquals(new Long(24), list.get(3).getLongValue());
        verify();

        // No permissions on projects 22 & 23
        reset(userUtil, permissionManager);
        EasyMock.expect(permissionManager.getAllProjectPermissions()).andReturn(Lists.<ProjectPermission>newArrayList(new MockProjectPermission("key", "nameKey", null, null))).anyTimes();
        EasyMock.expect(userUtil.getUserByName("fred")).andReturn(theUser);
        EasyMock.expect(permissionManager.getProjects(new ProjectPermissionKey("key"), theUser)).andReturn(projectsList1);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(0), theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(1), theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(2), theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(3), theUser)).andReturn(true);
        replay(userUtil, permissionManager);

        list = projectsWhereUserHasPermissionFunction.getValues(queryCreationContext, new FunctionOperand("projectsWhereUserHasPermission", "View Development Tools"), terminalClause);
        assertEquals(2, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(24), list.get(1).getLongValue());
        verify();
    }

    @Test
    public void testGetValuesAnonymous()
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        final I18nHelper.BeanFactory i18nHelperFactory = EasyMock.createMock(I18nHelper.BeanFactory.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, userUtil, i18nHelperFactory);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        for (Project project : projectsList2)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, (ApplicationUser) null)).andReturn(true);
        }
        replay(userUtil, permissionManager);

        List<QueryLiteral> list = projectsWhereUserHasPermissionFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("projectsWhereUserHasPermission", "View Development Tools"), terminalClause);
        assertTrue(list.isEmpty());

        verify();

        // No permission for anonymous user
        reset(userUtil, permissionManager);
        for (Project project : projectsList2)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, (ApplicationUser) null)).andReturn(false);
        }
        replay(userUtil, permissionManager);

        list = projectsWhereUserHasPermissionFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("projectsWhereUserHasPermission", "View Development Tools"), terminalClause);
        assertTrue(list.isEmpty());
        verify();
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);
        final I18nHelper.BeanFactory i18nHelperFactory = EasyMock.createMock(I18nHelper.BeanFactory.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, userUtil, i18nHelperFactory);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        assertEquals(1, projectsWhereUserHasPermissionFunction.getMinimumNumberOfExpectedArguments());
    }

}
