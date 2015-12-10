package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import electric.xml.Document;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Tests {@link com.atlassian.jira.jelly.tag.projectroles.RemoveActorsFromProjectRole}.
 */
public class TestRemoveActors extends AbstractProjectRolesTest
{
    /**
     * Holds the number of times something was called for Mocks.
     */
    private static class CallRecorder
    {
        private int calls = 0;

    }

    public TestRemoveActors(final String s)
    {
        super(s);
    }

    public void testRemoveActorsFromProjectRole() throws Exception
    {
        final Collection<String> actors = new ArrayList<String>();
        actors.add("jira-administrators");
        actors.add("jira-users");

        final Project mockProject = new ProjectImpl(null);

        final Mock projectRoleService = new Mock(ProjectRoleService.class);
        projectRoleService.expectVoid("removeActorsFromProjectRole", new Constraint[] { new IsEqual(u), new IsEqual(actors), new IsEqual(
            MockProjectRoleManager.PROJECT_ROLE_TYPE_1), new IsEqual(mockProject), new IsEqual("atlassian-group-role-actor"), new IsEqual(
            new SimpleErrorCollection()) });

        projectRoleService.expectAndReturn("getProjectRole", new Constraint[] { new IsEqual(new Long(1)), new IsEqual(
            new SimpleErrorCollection()) }, MockProjectRoleManager.PROJECT_ROLE_TYPE_1);

        swapProjectRoleService((ProjectRoleService) projectRoleService.proxy());

        final Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.expectAndReturn("getProjectObjByKey", new Constraint[] { new IsEqual("MKY") }, mockProject);

        ManagerFactory.removeService(ProjectManager.class);
        ManagerFactory.addService(ProjectManager.class, (ProjectManager) mockProjectManager.proxy());

        // test with no project roles
        runScriptAndAssertTextResultEquals(null, "remove-actors-projectrole.test.remove-actors.jelly");
        projectRoleService.verify();

    }

    public void testRemoveAllRoleActorsByNameAndType() throws Exception
    {

        final CallRecorder recorder = new CallRecorder();
        swapProjectRoleService(new MockProjectRoleService()
        {

            @Override
            public void removeAllRoleActorsByNameAndType(final User currentUser, final String name, final String type, final ErrorCollection errorCollection)
            {
                assertEquals(u, currentUser);
                assertEquals("atlassian-group-role-actor", type);
                // check that the two successive calls on this method are for each item in the actors list
                assertTrue(recorder.calls < 2);
                if (recorder.calls == 0)
                {
                    // first actor in the list
                    assertEquals("jira-administrators", name);
                }
                else if (recorder.calls == 1)
                {
                    // second actor in the list
                    assertEquals("jira-users", name);
                }
                recorder.calls++;
            }
        });

        final Document result = runScriptBody("<jira:RemoveActorsFromProjectRole projectroleid=\"1\" actors=\"jira-administrators, jira-users\" actortype=\"atlassian-group-role-actor\" />");
        assertEquals(2, recorder.calls);
        // we expect no output from this script
        assertRootTextEquals(result, null);

    }

    public void testRemoveAllRoleActorsByProject() throws Exception
    {
        final Project mockProject = new ProjectImpl(null);

        final Mock projectRoleService = new Mock(ProjectRoleService.class);
        projectRoleService.expectVoid("removeAllRoleActorsByProject", new Constraint[] { new IsEqual(u), new IsEqual(mockProject), new IsEqual(
            new SimpleErrorCollection()) });

        swapProjectRoleService((ProjectRoleService) projectRoleService.proxy());

        final Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.expectAndReturn("getProjectObjByKey", new Constraint[] { new IsEqual("MKY") }, mockProject);

        ManagerFactory.removeService(ProjectManager.class);
        ManagerFactory.addService(ProjectManager.class, (ProjectManager) mockProjectManager.proxy());

        final Document result = runScriptBody("<jira:RemoveActorsFromProjectRole projectkey=\"MKY\" />");
        // we expect no output from this script
        assertRootTextEquals(result, null);
        projectRoleService.verify();

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
