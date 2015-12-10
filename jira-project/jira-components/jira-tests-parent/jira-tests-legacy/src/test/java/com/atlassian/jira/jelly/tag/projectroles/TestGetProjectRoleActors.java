package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.MockUserRoleActor;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleActorsImpl;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright All Rights Reserved.
 * Created: christo 30/06/2006 11:58:43
 */
public class TestGetProjectRoleActors extends AbstractProjectRolesTest
{
    public TestGetProjectRoleActors(final String s)
    {
        super(s);
    }

    public void testGetProjectRoleActors() throws Exception
    {
        final Project mockProject = new ProjectImpl(null);

        // set up the returned ProjectRoleActor
        final MockUserRoleActor userRoleActor = new MockUserRoleActor(null, null, new MockApplicationUser(u.getName(), u.getDisplayName(), u.getEmailAddress()));
        final Set<ProjectRoleActor> actors = new HashSet<ProjectRoleActor>();
        actors.add(userRoleActor);
        final ProjectRoleActors roleActors = new ProjectRoleActorsImpl(mockProject.getId(), MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getId(),
            actors);

        final Mock projectRoleService = new Mock(ProjectRoleService.class);
        projectRoleService.expectAndReturn("getProjectRoleActors", new Constraint[] {new IsEqual(
            MockProjectRoleManager.PROJECT_ROLE_TYPE_1), new IsEqual(mockProject), new IsEqual(new SimpleErrorCollection()) }, roleActors);

        projectRoleService.expectAndReturn("getProjectRole", new Constraint[] {new IsEqual(new Long(1)), new IsEqual(
            new SimpleErrorCollection()) }, MockProjectRoleManager.PROJECT_ROLE_TYPE_1);

        swapProjectRoleService((ProjectRoleService) projectRoleService.proxy());

        final Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.expectAndReturn("getProjectObjByKey", new Constraint[] { new IsEqual("MKY") }, mockProject);
        ManagerFactory.removeService(ProjectManager.class);
        ManagerFactory.addService(ProjectManager.class, (ProjectManager) mockProjectManager.proxy());

        // We expect one user in the ProjectRoleActors.getUsers call
        runScriptAndAssertTextResultEquals(u.getName(), "get-projectroleactors.test.getprojectroleactors.jelly");
        projectRoleService.verify();
    }

    public void testGetProjectRoleActorsWithInvalidParams()
    {
        try
        {
            runScriptBody("<jira:GetProjectRoleActors />");
            fail("expected exception");
        }
        catch (final JellyServiceException e)
        {
            // we expected an error to be thrown
        }
    }

}
