package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;

/**
 * Tests Jelly tag RemoveDefaultActorsFromProjectRole
 */
public class TestRemoveDefaultActorsFromProjectRole extends AbstractProjectRolesTest
{
    public TestRemoveDefaultActorsFromProjectRole(String s)
    {
        super(s, "remove-defaultactors-projectrole.test.remove-actors.jelly");
    }

    public void testRemoveActorsFromDefaultProjectRole() throws Exception
    {
        swapProjectRoleService(new MockProjectRoleService()
        {
            public void removeDefaultActorsFromProjectRole(User currentUser, Collection actors, ProjectRole projectRole, String actorType, ErrorCollection errorCollection)
            {
                // do nothing (for now).
            }

            public ProjectRole getProjectRole(User currentUser, Long id, ErrorCollection errorCollection)
            {
                return MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
            }

        });

        // test with no project roles
        runScriptAndAssertTextResultEquals(null);
    }

    public void testRemoveActorsFromProjectRoleWithInvalidParams()
    {
        try
        {
            runScriptBody("<jira:RemoveDefaultActorsFromProjectRole />");
            fail("expected exception");
        }
        catch (JellyServiceException e)
        {
            // we expect an exception to be thrown
        }

    }

}
