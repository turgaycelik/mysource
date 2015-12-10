package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.ErrorCollection;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;

import java.util.Collection;

/**
 * Tests the RemoveActorsFromProjectRole jelly tag.
 * TODO: retire this because the TestRemoveActors test does this job now
 */
public class TestRemoveActorsFromProjectRole extends AbstractProjectRolesTest
{
    public TestRemoveActorsFromProjectRole(final String s)
    {
        super(s, "remove-actors-projectrole.test.remove-actors.jelly");
    }

    public void testRemoveActorsFromProjectRole() throws Exception
    {
        swapProjectRoleService(new MockProjectRoleService()
        {
            @Override
            public void removeActorsFromProjectRole(final User currentUser, final Collection actors, final ProjectRole projectRole, final Project project, final String actorType, final ErrorCollection errorCollection)
            {
            // do nothing (for now).
            }

            @Override
            public ProjectRole getProjectRole(final User currentUser, final Long id, final ErrorCollection errorCollection)
            {
                return MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
            }

        });

        final Mock mockProjectManager = new Mock(ProjectManager.class);
        final Project mockProject = new ProjectImpl(null);
        mockProjectManager.expectAndReturn("getProjectObjByKey", new Constraint[] { new IsEqual("MKY") }, mockProject);

        ManagerFactory.addService(ProjectManager.class, (ProjectManager) mockProjectManager.proxy());

        // test with no project roles
        runScriptAndAssertTextResultEquals(null);

    }

    public void testRemoveActorsFromProjectRoleWithInvalidParams()
    {
        try
        {
            runScriptBody("<jira:RemoveActorsFromProjectRole />");
            fail("expected exception");
        }
        catch (final JellyServiceException e)
        {
            // we expect an exception to be thrown
        }

    }

}
