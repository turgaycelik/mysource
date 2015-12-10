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
 * Created: christo 29/06/2006 17:10:21
 */
public class TestDeleteProjectRole extends AbstractProjectRolesTest
{


    public TestDeleteProjectRole(String s)
    {
        super(s, "delete-projectrole.test.delete-projectrole.jelly");
    }

    public void testDeleteProjectRole() throws Exception
    {
        Collection actors = new ArrayList();
        actors.add("fred");
        actors.add("admin");
        Mock projectRoleService  = new Mock(ProjectRoleService.class);

        projectRoleService.expectVoid("deleteProjectRole", new Constraint[]{
                                                                            new IsEqual(MockProjectRoleManager.PROJECT_ROLE_TYPE_1),
                                                                            new IsEqual(new SimpleErrorCollection())});

        projectRoleService.expectAndReturn("getProjectRole", new Constraint [] {new IsEqual(new Long(1)), new IsEqual(new SimpleErrorCollection())}, MockProjectRoleManager.PROJECT_ROLE_TYPE_1);

        swapProjectRoleService((ProjectRoleService) projectRoleService.proxy());

        // test with no project roles
        runScriptAndAssertTextResultEquals(null);
        projectRoleService.verify();
    }

    public void testDeleteProjectRoleWithInvalidParams()
    {
        try
        {
            runScriptBody("<jira:DeleteProjectRole />");
            fail("expected exception");
        }
        catch (JellyServiceException e)
        {
            // we expect an exception to be thrown
        }

    }

    public void testDeleteProjectRoleWithFlagNotSet()
    {
        try
        {
            runScriptBody("<jira:DeleteProjectRole projectroleid=\"1\" />");
            fail("expected exception");
        }
        catch (JellyServiceException e)
        {
            // we expect an exception to be thrown
            assertTrue(e.getMessage().indexOf("To force deletion of this role make the confirm parameter 'true'.") != -1);
        }
    }
}
