package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Copyright All Rights Reserved.
 * Created: christo 29/06/2006 12:05:13
 */
public class TestAddActorsToProjectRole extends AbstractProjectRolesTest
{
    public TestAddActorsToProjectRole(final String s)
    {
        super(s, "add-actors-projectrole.test.add-actors.jelly");
    }

    public void testAddActorsToProjectRole() throws Exception
    {
        final Project mockProject = new ProjectImpl(null);
        final Collection<String> actors = new ArrayList<String>();
        actors.add("fred");
        actors.add("admin");
        final Mock projectRoleService = new Mock(ProjectRoleService.class);
        projectRoleService.expectVoid("addActorsToProjectRole", new Constraint[] {
                new IsEqual(actors),
                new IsEqual( MockProjectRoleManager.PROJECT_ROLE_TYPE_1),
                new IsEqual(mockProject),
                new IsEqual("atlassian-user-role-actor"),
                new IsEqual( new SimpleErrorCollection())
        });
        projectRoleService.expectAndReturn("getProjectRole", new Constraint[] {
                new IsEqual(new Long(1)),
                new IsEqual( new SimpleErrorCollection())
        }, MockProjectRoleManager.PROJECT_ROLE_TYPE_1);

        swapProjectRoleService((ProjectRoleService) projectRoleService.proxy());

        final Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.expectAndReturn("getProjectObjByKey", new Constraint[] { new IsEqual("MKY") }, mockProject);
        ManagerFactory.removeService(ProjectManager.class);
        ManagerFactory.addService(ProjectManager.class, (ProjectManager) mockProjectManager.proxy());

        // test with no project roles
        runScriptAndAssertTextResultEquals(null);
        projectRoleService.verify();
    }

    public void testAddActorToProjectRoleWithInvalidParams()
    {
        try
        {
            runScriptBody("<jira:AddActorsToProjectRole />");
            fail("expected exception");
        }
        catch (final JellyServiceException e)
        {
            // we expect an exception to be thrown
        }
    }
}
