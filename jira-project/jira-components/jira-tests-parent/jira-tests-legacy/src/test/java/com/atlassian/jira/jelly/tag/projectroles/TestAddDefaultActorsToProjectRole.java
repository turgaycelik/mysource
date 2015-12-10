package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.mock.MockProjectRoleManager;
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
public class TestAddDefaultActorsToProjectRole extends AbstractProjectRolesTest
{
    public TestAddDefaultActorsToProjectRole(String s)
    {
        super(s, "add-defaultactors-projectrole.test.add-actors.jelly");
    }

    public void testAddActorsToDefaultProjectRole() throws Exception
    {
        Collection actors = new ArrayList();
        actors.add("fred");
        actors.add("admin");
        Mock projectRoleService  = new Mock(ProjectRoleService.class);
        projectRoleService.expectVoid("addDefaultActorsToProjectRole", new Constraint[]{
                                                                                 new IsEqual(actors),
                                                                                 new IsEqual(MockProjectRoleManager.PROJECT_ROLE_TYPE_1),
                                                                                 new IsEqual("atlassian-user-role-actor"),
                                                                                 new IsEqual(new SimpleErrorCollection())});
        projectRoleService.expectAndReturn("getProjectRole", new Constraint [] {new IsEqual(new Long(1)), new IsEqual(new SimpleErrorCollection())}, MockProjectRoleManager.PROJECT_ROLE_TYPE_1);


        swapProjectRoleService((ProjectRoleService) projectRoleService.proxy());

        // test with no project roles
        runScriptAndAssertTextResultEquals(null);
        projectRoleService.verify();
    }

    public void testAddActorToProjectRoleWithInvalidParams()
    {
        try
        {
            runScriptBody("<jira:AddDefaultActorsToProjectRole />");
            fail("expected exception");
        }
        catch (JellyServiceException e)
        {
            // we expect an exception to be thrown
        }
    }
}
