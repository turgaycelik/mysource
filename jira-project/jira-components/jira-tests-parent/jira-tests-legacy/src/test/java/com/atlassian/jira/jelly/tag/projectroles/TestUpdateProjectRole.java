package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.ErrorCollection;

/**
 * Copyright All Rights Reserved.
 * Created: christo 30/06/2006 14:57:16
 */
public class TestUpdateProjectRole extends AbstractProjectRolesTest
{

    public TestUpdateProjectRole(String s)
    {
        super(s, "update-projectrole.test-update.jelly");
    }

    private static class CallVerifier {
        private boolean wasCalled = false;
    }

    public void testUpdate() throws Exception
    {
        final CallVerifier updateProjectRole = new CallVerifier();

        swapProjectRoleService(new MockProjectRoleService()
        {
            @Override
            public void updateProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
            {
                updateProjectRole.wasCalled = true;
                assertEquals(new Long(123), projectRole.getId());
                assertEquals("unique name", projectRole.getName());
                assertEquals("my project role is nice", projectRole.getDescription());
            }
        });

        runScriptAndAssertTextResultEquals(null);
        assertTrue("ProjectRoleService.updateProjectRole() should have been called", updateProjectRole.wasCalled);
    }
}
