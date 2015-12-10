package com.atlassian.jira.plugin.jql.function;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
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

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.2
 */
public class TestComponentsLeadByUserFunction
{
    private ApplicationUser theUser;
    private QueryCreationContext queryCreationContext;
    private TerminalClause terminalClause;
    private List<ProjectComponent> componentsList1 = new ArrayList<ProjectComponent>(4);
    private List<ProjectComponent> componentsList2 = new ArrayList<ProjectComponent>(4);
    private MockProject project1;
    private MockProject project2;
    private MockProject project3;
    private MockProjectComponent component1;
    private MockProjectComponent component2;
    private MockProjectComponent component3;
    private MockProjectComponent component4;

    @Before
    public void setUp()
    {
        theUser = new MockApplicationUser("Fred");
        queryCreationContext = new QueryCreationContextImpl(theUser);

        component1 = new MockProjectComponent(21l, "c1", 21l);
        component2 = new MockProjectComponent(22l, "c2", 22l);
        component3 = new MockProjectComponent(23l, "c3", 23l);
        component4 = new MockProjectComponent(24l, "c4", 23l);

        project1 = new MockProject(21l, "p1");
        project2 = new MockProject(22l, "p2");
        project3 = new MockProject(23l, "p3");

        componentsList1.add(component1);
        componentsList1.add(component2);
        componentsList1.add(component3);
        componentsList1.add(component4);

        componentsList2.add(component1);
        componentsList2.add(component2);

        project1.setProjectComponents(ImmutableList.<ProjectComponent>of(component1));
        project2.setProjectComponents(ImmutableList.<ProjectComponent>of(component2));
        project3.setProjectComponents(ImmutableList.<ProjectComponent>of(component3, component4));
    }

    @After
    public void tearDown()
    {
        theUser = null;
        queryCreationContext = null;
        terminalClause = null;
        componentsList1 = null;
        componentsList2 = null;
        project1 = null;
        project2 = null;
        project3 = null;
        component1 = null;
        component2 = null;
        component3 = null;
        component4 = null;
    }

    @Test
    public void testDataType() throws Exception
    {
        final UserUtil userUtil = mock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = mock(ProjectComponentManager.class);
        final PermissionManager permissionManager = mock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, userUtil);

        assertEquals(JiraDataTypes.COMPONENT, componentsLeadByUserFunction.getDataType());
    }

    @Test
    public void testValidateWrongArgs() throws Exception
    {
        final UserUtil userUtil = mock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = mock(ProjectComponentManager.class);
        final PermissionManager permissionManager = mock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        MessageSet messageSet = componentsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("componentsLeadByUser", "badArg1", "badArg2"), terminalClause);
        assert1ErrorNoWarnings(messageSet, "Function 'componentsLeadByUser' expected between '0' and '1' arguments but received '2'.");

        messageSet = componentsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("componentsLeadByUser", "badUser"), terminalClause);
        assert1ErrorNoWarnings(messageSet, "Function 'componentsLeadByUser' can not generate a list of components for user 'badUser'; the user does not exist.");
    }

    @Test
    public void testValidateHappyPath() throws Exception
    {
        final UserUtil userUtil = mock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = mock(ProjectComponentManager.class);
        final PermissionManager permissionManager = mock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        // No user name supplied
        MessageSet messageSet = componentsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertThat("hasAnyErrors", messageSet.hasAnyErrors(), is(false));

        // One valid user name supplied
        when(userUtil.getUserByName("fred")).thenReturn(theUser);

        messageSet = componentsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("componentsLeadByUser", "fred"), terminalClause);
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidateAnonymous()
    {
        final UserUtil userUtil = mock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = mock(ProjectComponentManager.class);
        final PermissionManager permissionManager = mock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        final MessageSet messageSet = componentsLeadByUserFunction.validate(null, new FunctionOperand("componentsLeadByUser"), terminalClause);
        assert1ErrorNoWarnings(messageSet, "Function 'componentsLeadByUser' cannot be called as anonymous user.");
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        final UserUtil userUtil = mock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = mock(ProjectComponentManager.class);
        final ProjectManager projectManager = mock(ProjectManager.class);
        final PermissionManager permissionManager = mock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        ApplicationUser bill = new MockApplicationUser("bill");

        when(userUtil.getUserByName("fred")).thenReturn(theUser);
        when(userUtil.getUserByName("bill")).thenReturn(bill);

        when(projectManager.getProjectObj(project1.getId())).thenReturn(project1);
        when(projectManager.getProjectObj(project2.getId())).thenReturn(project2);
        when(projectManager.getProjectObj(project3.getId())).thenReturn(project3);

        when(projectComponentManager.findComponentsByLead("fred")).thenReturn(componentsList1);
        when(projectComponentManager.findComponentsByLead("bill")).thenReturn(componentsList2);

        when(permissionManager.getProjects(Permissions.BROWSE, theUser))
                .thenReturn(ImmutableList.<Project>of(project1, project2, project3))
                .thenReturn(ImmutableList.<Project>of(project1, project2));

        component1.setLead("fred");
        component2.setLead("fred");
        component3.setLead("fred");
        component4.setLead("fred");
        List<QueryLiteral> list = componentsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertEquals(4, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());
        assertEquals(new Long(23), list.get(2).getLongValue());
        assertEquals(new Long(24), list.get(3).getLongValue());

        component1.setLead("bill");
        component2.setLead("bill");
        list = componentsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("componentsLeadByUser", "bill"), terminalClause);
        assertEquals(2, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());

        // No permissions on projects 22 & 23
        when(userUtil.getUserByName("fred")).thenReturn(theUser);
        when(projectComponentManager.findComponentsByLead("fred")).thenReturn(componentsList1);
        when(permissionManager.getProjects(Permissions.BROWSE, theUser)).thenReturn(ImmutableList.<Project>of(project1));

        bill = new MockApplicationUser("bill");
        when(userUtil.getUserByName("bill")).thenReturn(bill);
        when(projectComponentManager.findComponentsByLead("bill")).thenReturn(componentsList2);
        when(projectManager.getProjectObj(component1.getProjectId())).thenReturn(project1);
        when(projectManager.getProjectObj(component2.getProjectId())).thenReturn(project2);
        when(permissionManager.getProjects(Permissions.BROWSE, theUser)).thenReturn(ImmutableList.<Project>of(project1));

        component1.setLead("fred");
        component2.setLead("fred");
        component3.setLead("fred");
        component4.setLead("fred");
        list = componentsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertEquals(1, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());

        component1.setLead("bill");
        component2.setLead("bill");
        list = componentsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("componentsLeadByUser", "bill"), terminalClause);
        assertEquals(1, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
    }

    @Test
    public void testGetValuesAnonymous()
    {
        final UserUtil userUtil = mock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = mock(ProjectComponentManager.class);
        final ProjectManager projectManager = mock(ProjectManager.class);
        final PermissionManager permissionManager = mock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        ApplicationUser bill = new MockApplicationUser("bill");
        when(userUtil.getUserByName("bill")).thenReturn(bill);
        when(projectComponentManager.findComponentsByLead("bill")).thenReturn(componentsList2);
        when(projectManager.getProjectObj(component1.getProjectId())).thenReturn(project1);
        when(projectManager.getProjectObj(component2.getProjectId())).thenReturn(project2);
        when(permissionManager.getProjects(Permissions.BROWSE, theUser)).thenReturn(ImmutableList.<Project>of(project1, project2));
        when(permissionManager.getProjects(Permissions.BROWSE, (ApplicationUser) null)).thenReturn(ImmutableList.<Project>of(project1, project2));

        component1.setLead("fred");
        component2.setLead("fred");
        component3.setLead("fred");
        component4.setLead("fred");
        List<QueryLiteral> list = componentsLeadByUserFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertTrue(list.isEmpty());

        component1.setLead("bill");
        component2.setLead("bill");
        list = componentsLeadByUserFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("componentsLeadByUser", "bill"), terminalClause);
        assertEquals(2, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());

        // No permissions for anonymous user.
        when(userUtil.getUserByName("bill")).thenReturn(bill);
        when(projectComponentManager.findComponentsByLead("bill")).thenReturn(componentsList2);
        when(projectManager.getProjectObj(component1.getProjectId())).thenReturn(project1);
        when(projectManager.getProjectObj(component2.getProjectId())).thenReturn(project2);
        when(permissionManager.getProjects(Permissions.BROWSE, theUser)).thenReturn(ImmutableList.<Project>of());
        when(permissionManager.getProjects(Permissions.BROWSE, (ApplicationUser) null)).thenReturn(ImmutableList.<Project>of());

        list = componentsLeadByUserFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertThat(list, Matchers.<QueryLiteral>empty());

        list = componentsLeadByUserFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("componentsLeadByUser", "bill"), terminalClause);
        assertThat(list, Matchers.<QueryLiteral>empty());
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        final UserUtil userUtil = mock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = mock(ProjectComponentManager.class);
        final PermissionManager permissionManager = mock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        assertEquals(0, componentsLeadByUserFunction.getMinimumNumberOfExpectedArguments());
    }
}
