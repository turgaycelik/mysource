package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.jelly.AbstractJellyTestCase;

/**
 * Copyright All Rights Reserved.
 * Created: christo 28/06/2006 16:15:07
 */
public abstract class AbstractProjectRolesTest extends AbstractJellyTestCase
{
    User u;
    Group g;

    public AbstractProjectRolesTest(String s)
    {
        super(s);
    }

    protected AbstractProjectRolesTest(String s, String scriptFilename)
    {
        super(s, scriptFilename);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        //Create user and place in the action context
        u = createMockUser("logged-in-user");
        g = createMockGroup("admin-group");
        addUserToGroup(u, g);
        JiraTestUtil.loginUser(u);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Adds the given ProjectRoleService to the ComponentManager container in
     * place of any previous one.
     * @param service The ProjectRoleService to install.
     */
    public static void swapProjectRoleService(ProjectRoleService service)
    {
        ManagerFactory.removeService(ProjectRoleService.class);
        ManagerFactory.addService(ProjectRoleService.class, service);
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "projectroles" + FS;
    }

}
