package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.DefaultRoleActorsImpl;
import com.atlassian.jira.security.roles.MockUserRoleActor;
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
public class TestGetDefaultRoleActors extends AbstractProjectRolesTest
{
    public TestGetDefaultRoleActors(String s)
    {
        super(s);
    }

    public void testGetDefaultRoleActors() throws Exception
    {
        // set up the returned ProjectRoleActor
        MockUserRoleActor userRoleActor = new MockUserRoleActor(null, null, new MockApplicationUser(u.getName(), u.getDisplayName(), u.getEmailAddress()));
        Set actors = new HashSet();
        actors.add(userRoleActor);
        DefaultRoleActors roleActors = new DefaultRoleActorsImpl(MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getId(), actors);

        Mock projectRoleService  = new Mock(ProjectRoleService.class);
        projectRoleService.expectAndReturn("getDefaultRoleActors",
                                           new Constraint[]{
                                                   new IsEqual(MockProjectRoleManager.PROJECT_ROLE_TYPE_1),
                                                   new IsEqual(new SimpleErrorCollection())}, roleActors);

        projectRoleService.expectAndReturn("getProjectRole", new Constraint [] {new IsEqual(new Long(1)), new IsEqual(new SimpleErrorCollection())}, MockProjectRoleManager.PROJECT_ROLE_TYPE_1);

        swapProjectRoleService((ProjectRoleService) projectRoleService.proxy());

        // We expect on user in the DefaultRoleActors.getUsers call
        runScriptAndAssertTextResultEquals(u.getName(), "get-defaultroleactors.test.getdefaultroleactors.jelly");
        projectRoleService.verify();
    }

    public void testGetProjectRoleActorsWithInvalidParams()
    {
        try
        {
            runScriptBody("<jira:GetDefaultRoleActors />");
            fail("expected exception");
        }
        catch (JellyServiceException e)
        {
            // we expected an error to be thrown
        }
    }

}
