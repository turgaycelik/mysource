package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.ErrorCollection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Tests the GetProjectRole jelly tag.
 */
public class TestGetProjectRole extends AbstractProjectRolesTest
{

    public TestGetProjectRole(String s)
    {
        super(s, "get-projectrole.test-getprojectroles.jelly");
    }

    public void testGetProjectRoles() throws Exception
    {
        final Collection projectRoles = new ArrayList();
        swapProjectRoleService(new MockProjectRoleService()
        {
            @Override
            public Collection getProjectRoles(ErrorCollection errorCollection)
            {
                return projectRoles;
            }
        });

        // test with no project roles
        runScriptAndAssertTextResultEquals(null);

        // Add one project role only
        ProjectRole project_role_type_1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
        projectRoles.add(project_role_type_1);
        runScriptAndAssertTextResultEquals(project_role_type_1.getName());

        // Add a second project role
        ProjectRole project_role_type_2 = MockProjectRoleManager.PROJECT_ROLE_TYPE_2;
        projectRoles.add(project_role_type_2);
        runScriptAndAssertTextResultEquals(project_role_type_1.getName()+project_role_type_2.getName());

    }

    public void testGetProjectRole() throws Exception
    {
        swapProjectRoleService(new MockProjectRoleService()
        {
            public ProjectRole getProjectRole(Long id, ErrorCollection errorCollection)
            {
                return MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
            }
        });

         // Add one project role only
        ProjectRole project_role_type_1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
        runScriptAndAssertTextResultEquals(project_role_type_1.getName(), "get-projectrole.test-getprojectrole.jelly");
    }

}
