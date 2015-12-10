package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.util.ErrorCollection;

/**
 * Tests the CreateProjectRole Jelly tag.
 */
public class TestCreateProjectRole extends AbstractProjectRolesTest
{
    public TestCreateProjectRole(String s)
    {
        super(s, "create-projectrole.test-createprojectrole.jelly");
    }

    public void testCreate() throws Exception
    {
        swapProjectRoleService(new LionTamerProjectRoleService());

        runScriptAndAssertTextResultEquals("1 lion-tamer tames the lions");
    }

    public void testCreateWithErrors() throws Exception
    {
        final String message = "ERROR: createProjectRole";
        swapProjectRoleService(new MockProjectRoleService()
        {
            @Override
            public ProjectRole createProjectRole(ProjectRole projectRole, ErrorCollection errorCollection)
            {
                errorCollection.addErrorMessage(message);
                return new ProjectRoleImpl(new Long(1), "lion-tamer", "tames the lions");
            }
        });

        try
        {
            runScriptAndAssertTextResultEquals("some unknown value that we don't care about");
            fail();
        }
        catch(JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf(message) != -1);
        }
    }

    public void testCreateInvalidParams() throws Exception
    {
        try
        {
            runScriptBody("<jira:CreateProjectRole />");
            fail("expected exception");
        }
        catch (JellyServiceException e)
        {
            // we expect an exception to be thrown
        }
    }

    private static class LionTamerProjectRoleService extends MockProjectRoleService
    {
        @Override
        public ProjectRole createProjectRole(ProjectRole projectRole, ErrorCollection errorCollection)
        {
            return new ProjectRoleImpl(new Long(1), "lion-tamer", "tames the lions");
        }
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
}
