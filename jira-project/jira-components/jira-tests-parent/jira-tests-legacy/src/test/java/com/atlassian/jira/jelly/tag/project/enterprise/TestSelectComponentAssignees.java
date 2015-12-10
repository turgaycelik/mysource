package com.atlassian.jira.jelly.tag.project.enterprise;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.project.ComponentAssigneeTypes;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import electric.xml.Document;
import electric.xml.Element;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class TestSelectComponentAssignees extends AbstractJellyTestCase
{
    private User user;
    GenericValue project;

    public TestSelectComponentAssignees(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        user = createMockUser("logged-in-user");
        Group adminGroup = createMockGroup("admin-group");
        addUserToGroup(user, adminGroup);
        Group group = createMockGroup(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS);
        addUserToGroup(user, group);
        JiraTestUtil.loginUser(user);
        // Grant admin permission to that group
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, "admin-group");
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "name", "A Project", "counter", new Long(1), "lead", user.getName()));
        GenericValue scheme = JiraTestUtil.setupAndAssociateDefaultPermissionScheme(project);
        ComponentAccessor.getPermissionManager().addPermission(Permissions.ASSIGNABLE_USER, scheme, createMockGroup(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS).getName(), GroupDropdown.DESC);
    }

    public void testSelectProjectDefault() throws Exception
    {
        _testSelectComponentAssignee("select-component-assignees.test.select-assigneetype-projectDefault.jelly", new Long(ComponentAssigneeTypes.PROJECT_DEFAULT));
    }

    public void testSelectValidProjectLead() throws Exception
    {
        _testSelectComponentAssignee("select-component-assignees.test.select-assigneetype-projectLead.jelly", new Long(ComponentAssigneeTypes.PROJECT_LEAD));
    }

    public void testSelectInvalidProjectLead()
    {
        try
        {
            project.remove();
            project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "name", "A Project", "counter", new Long(1)));
            _testSelectComponentAssignee("select-component-assignees.test.select-assigneetype-projectLead.jelly", new Long(ComponentAssigneeTypes.PROJECT_LEAD));
        }
        catch (Exception e)
        {//should fail since there is no project lead set in the project
        }
    }

    public void testSelectValidComponentLead() throws Exception
    {
        assertTrue(ComponentAccessor.getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, project, user));
        _testSelectComponentAssignee("select-component-assignees.test.select-assigneetype-componentLead.jelly", new Long(ComponentAssigneeTypes.COMPONENT_LEAD));
    }

    public void testSelectInvalidComponentLead()
    {
        try
        {
            ComponentAccessor.getPermissionManager().removeGroupPermissions(createMockGroup(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS).getName());
            assertTrue(!ComponentAccessor.getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, project, user));
            _testSelectComponentAssignee("select-component-assignees.test.select-assigneetype-componentLead.jelly", new Long(ComponentAssigneeTypes.COMPONENT_LEAD));
        }
        catch (Exception e)
        {//should fail since user is no longer assignable
        }
    }

    public void testSelectValidUnassigned() throws Exception
    {
        ComponentAccessor.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);
        _testSelectComponentAssignee("select-component-assignees.test.select-assigneetype-unassigned.jelly", new Long(ComponentAssigneeTypes.UNASSIGNED));
    }

    public void testSelectInvalidUnassigned()
    {
        try
        {
            ComponentAccessor.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);
            _testSelectComponentAssignee("select-component-assignees.test.select-assigneetype-unassigned.jelly", new Long(ComponentAssigneeTypes.UNASSIGNED));
        }
        catch (Exception e)
        {//should fail since there unassigned issues are not allowed
        }
    }

    /**
     * runs the selectComponentAssigneetTypeScript and checks if the assigneetype has changed or not depending on
     * the value of the boolean same
     */
    public void _testSelectComponentAssignee(String selectComponentAssigneetTypeScript, Long expectedAssigneeType) throws Exception
    {
        final String addValidComponentScript = "select-component-assignees.test.create-valid-component-and-project.jelly";
        Document document = runScript(addValidComponentScript);
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        //Check to see if project and the component was created
        findAndAssertSingleComponent();

        document = runScript(selectComponentAssigneetTypeScript);

        final Element root2 = document.getRoot();
        assertEquals(0, root2.getElements().size());

        Long newCompAssigneeType = findAndAssertSingleComponent().getLong("assigneetype");

        //compare originalCompAssigneeType and newCompAssigneeType
        assertTrue(newCompAssigneeType.equals(expectedAssigneeType));
    }

    private GenericValue findAndAssertSingleComponent()
            throws GenericEntityException
    {
        final Collection components = CoreFactory.getGenericDelegator().findAll("Component");
        assertFalse(components.isEmpty());
        assertEquals(1, components.size());

        //keep track of the component
        Object o = components.iterator().next();
        GenericValue compnent = (GenericValue) o;
        return compnent;
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "project" + FS + "enterprise" + FS;
    }
}
