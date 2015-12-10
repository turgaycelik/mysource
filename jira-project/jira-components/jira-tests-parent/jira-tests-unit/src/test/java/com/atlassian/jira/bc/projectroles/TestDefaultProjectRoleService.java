package com.atlassian.jira.bc.projectroles;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.mock.workflow.MockXmlLoadableJiraWorkflow;
import com.atlassian.jira.notification.type.ProjectRoleSecurityAndNotificationType;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleActorsImpl;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.CollectionAssert;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.ActionDescriptor;

import org.apache.commons.collections.MultiMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.xml.sax.SAXException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the project role service, the validation of project roles.
 */
public class TestDefaultProjectRoleService
{
    @Mock
    @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;
    @AvailableInContainer
    private MockAuthenticationContext jiraAuthenticationContext = new MockAuthenticationContext(null);

    @Mock
    private EventPublisher eventPublisher;

    @Rule
    public MockitoContainer initMockitoMocks = MockitoMocksInContainer.rule(this);

    private MockProjectRoleManager projectRoleManager = null;
    private DefaultProjectRoleService defaultProjectRoleServicePermFalseFalse = null;
    private DefaultProjectRoleService defaultProjectRoleServicePermFalseTrue = null;

    private static final String PROJECTROLE_WORKFLOW_FILE = "com/atlassian/jira/bc/projectroles/test-projectrole-condition-workflow.xml";
    private static final String FRED = "TestDefaultProjectRoleService_fred";
    private static final String TESTER = "TestDefaultProjectRoleService_tester";

    @Before
    public void setUp() throws Exception
    {
        projectRoleManager = new MockProjectRoleManager();

        defaultProjectRoleServicePermFalseFalse = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(false, false),
                jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(),
                null, null, null, null, null, null, null, null, eventPublisher);
        defaultProjectRoleServicePermFalseTrue = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(false, true),
                jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(),
                null, null, null, null, null, null, null, null, eventPublisher);
    }

    @After
    public void tearDown() throws Exception
    {
        projectRoleManager = null;
        jiraAuthenticationContext = null;
        defaultProjectRoleServicePermFalseFalse = null;
        defaultProjectRoleServicePermFalseTrue = null;
    }

    private PermissionManager getPermissionManager(final boolean projectAdminPermission, final boolean adminPermission)
    {
        return new MockPermissionManager()
        {

            @Override
            public boolean hasPermission(int permissionsId, Project project, ApplicationUser u)
            {
                return projectAdminPermission;
            }

            @Override
            public boolean hasPermission(int permissionsId, ApplicationUser u)
            {
                return adminPermission;
            }
        };
    }

    /**
     * Will return the project role based off the passed in <code>id</code>, and checking the <code>currentUser</code>
     * has the correct permissions to perform the operation. The passed in <code>errorCollection</code> will contain any
     * errors that are generated, such as permission violations.
     */
    @Test
    public void testGetProjectRoleWithNullId()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        ProjectRole projectRole = defaultProjectRoleServicePermFalseFalse.getProjectRole(null, collection);

        assertNull(projectRole);
        assertTrue(collection.hasAnyErrors());
        assertContains(collection.getErrorMessages(), "Can not get a project role for a null id.");
    }

    /**
     * Will return the project role based off the passed in <code>id</code>, and checking the <code>currentUser</code>
     * has the correct permissions to perform the operation. The passed in <code>errorCollection</code> will contain any
     * errors that are generated, such as permission violations.
     */
    @Test
    public void testGetProjectRole()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        ProjectRole projectRole = defaultProjectRoleServicePermFalseFalse.getProjectRole(null, collection);

        assertNull(projectRole);
        assertTrue(collection.hasAnyErrors());
        assertContains(collection.getErrorMessages(), "Can not get a project role for a null id.");
    }

    @Test
    public void testGetProjectRoleByName()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        ProjectRole projectRole = defaultProjectRoleServicePermFalseFalse.getProjectRoleByName(null, collection);

        assertNull(projectRole);
        assertTrue(collection.hasAnyErrors());
        assertContains(collection.getErrorMessages(), "Can not get a project role with a null name.");
    }

    /**
     * Will create the project role based off the passed in <code>name</code>, <code>description</code> and checking
     * the <code>currentUser</code> has the correct permissions to perform the create operation. The passed in
     * <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     */
    @Test
    public void testCreateRoleNullNameAndAdminPermission()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        ProjectRole projectRole = defaultProjectRoleServicePermFalseFalse.createProjectRole(null, new ProjectRoleImpl(null, null), collection);

        assertNull(projectRole);
        assertTrue(collection.hasAnyErrors());
        assertContains(collection.getErrorMessages(), "Can not create a project role with a null name.");
        assertContains(collection.getErrorMessages(), "This user does not have the JIRA Administrator permission to perform this operation.");
    }

    /**
     * Will create the project role based off the passed in <code>name</code>, <code>description</code> and checking
     * the <code>currentUser</code> has the correct permissions to perform the create operation. The passed in
     * <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     */
    @Test
    public void testCreateRoleAdminPermissionFalse()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        ProjectRole projectRole = defaultProjectRoleServicePermFalseFalse.createProjectRole(null, new ProjectRoleImpl("name", null), collection);

        assertNull(projectRole);
        assertTrue(collection.hasAnyErrors());
        assertFalse(collection.getErrorMessages().contains("Can not create a project role with a null name."));
        assertContains(collection.getErrorMessages(), "This user does not have the JIRA Administrator permission to perform this operation.");
    }

    /**
     * Will create the project role based off the passed in <code>name</code>, <code>description</code> and checking
     * the <code>currentUser</code> has the correct permissions to perform the create operation. The passed in
     * <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     */
    @Test
    public void testCreateRoleAdminPermissionTrue()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        ProjectRole projectRole = defaultProjectRoleServicePermFalseTrue.createProjectRole(null, new ProjectRoleImpl("name", null), collection);

        assertNotNull(projectRole);
        assertFalse(collection.hasAnyErrors());
        assertFalse(collection.getErrorMessages().contains("Can not create a project role with a null name."));
        assertFalse(collection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
    }

    /**
     * Will create the project role based off the passed in <code>name</code>, <code>description</code> and checking the
     * <code>currentUser</code> has the correct permissions to perform the create operation. The passed in
     * <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     */
    @Test
    public void testCreateRoleAdminPermissionIllegalargumentException()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        ProjectRole projectRole = defaultProjectRoleServicePermFalseTrue.createProjectRole(null, new ProjectRoleImpl("Developer", null), collection);

        assertNull(projectRole);
        assertTrue(collection.hasAnyErrors());
        assertTrue(collection.getErrors().get("name").equals("A project role with name 'Developer' already exists."));
    }

    public void testIsRoleNameUnique()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();
        assertFalse(defaultProjectRoleServicePermFalseFalse.isProjectRoleNameUnique(null, "Random Name", collection));
        assertTrue(collection.hasAnyErrors());
        assertContains(collection.getErrorMessages(), "This user does not have the JIRA Administrator permission to perform this operation.");
    }

    /**
     * Will delete the project role based off the passed in <code>projectRole</code> and checking the
     * <code>currentUser</code> has the correct permissions to perform the delete operation. This will also delete all
     * ProjectRoleActor associations that it is the parent of. The passed in <code>errorCollection</code> will contain
     * any errors that are generated, such as permission violations.
     */
    @Test
    public void testDeleteRole()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        defaultProjectRoleServicePermFalseFalse.deleteProjectRole(null, null, collection);

        assertTrue(collection.hasAnyErrors());
        assertContains(collection.getErrorMessages(), "Can not delete a project role with a null project role specified.");
        assertContains(collection.getErrorMessages(), "This user does not have the JIRA Administrator permission to perform this operation.");
    }

    /**
     * Will add project role actor associations based off the passed in <code>actors</code> and checking the
     * <code>currentUser</code> has the correct permissions to perform the update operation. The passed in
     * <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     */
    @Test
    public void testAddActorsToProjectRoleNullParams()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        defaultProjectRoleServicePermFalseFalse.addActorsToProjectRole(null, null, null, null, null, collection);

        assertTrue(collection.hasAnyErrors());
        assertContains(collection.getErrorMessages(), "Can not update a null role actor.");
        assertContains(collection.getErrorMessages(), "Can not retrieve a role actor for a null project role.");
        assertContains(collection.getErrorMessages(), "Can not retrieve a project role actor for a null project.");
        assertContains(collection.getErrorMessages(), "The user does not have the JIRA Administrator permission, or is not running Enterprise and does not have the Project admin permission.");
    }

    // This is a REALLY horrible method that relies on some really loosly hung together mocks.
    @Test
    public void testAddActorsToProjectRole()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        Project mockProject = new ProjectImpl(new MockGenericValue("project", ImmutableMap.of("id", 1L)));
        mockProjectManager.addProject(mockProject);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true),
                jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(),
                null, null, null, mockProjectManager, null, null, null, null, eventPublisher);
        SimpleErrorCollection collection = new SimpleErrorCollection();

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an RoleActorDoesNotExistException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.
        projectRoleService.addActorsToProjectRole(null, ImmutableList.of(MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER, "tester"), projectRoleManager.getProjectRole(new Long(1)), mockProject, MockProjectRoleManager.MockRoleActor.TYPE, collection);

        assertContains(collection.getErrorMessages(), "'tester' is already a member of the project role.");
        assertContains(collection.getErrorMessages(), "'" + MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER + "' could not be found");
    }

    /**
     * Slowly works throuh each parameter to ensure the right error messages are produced
     */
    @Test
    public void testSetActorsForNullProjectRole()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        Project mockProject = new ProjectImpl(new MockGenericValue("project", ImmutableMap.of("id", 1L)));
        mockProjectManager.addProject(mockProject);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true),
                jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(),
                null, null, null, mockProjectManager, null, null, null, null, eventPublisher);
        SimpleErrorCollection collection = new SimpleErrorCollection();

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an RoleActorDoesNotExistException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.
        projectRoleService.setActorsForProjectRole(null, null,
                projectRoleManager.getProjectRole(1L), mockProject, collection);

        assertEquals(1, collection.getErrorMessages().size());
        assertContains(collection.getErrorMessages(), "Can not update project actors with a null value.");

        collection = new SimpleErrorCollection();

        projectRoleService.setActorsForProjectRole(null, MapBuilder.<String, Set<String>>build(
                MockProjectRoleManager.MockRoleActor.TYPE,
                Sets.<String>newHashSet(MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER, "tester")
        ), null, mockProject, collection);
        assertEquals(2, collection.getErrorMessages().size());
        assertContains(collection.getErrorMessages(), "Can not retrieve a role actor for a null project role.");
        assertTrue(collection.getErrorMessages().contains("The user does not have the JIRA Administrator permission, "
                + "or is not running Enterprise and does not have the Project admin permission."));

        collection = new SimpleErrorCollection();

        projectRoleService.setActorsForProjectRole(null, MapBuilder.<String, Set<String>>build(
                MockProjectRoleManager.MockRoleActor.TYPE,
                Sets.<String>newHashSet(MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER, "tester")
        ), projectRoleManager.getProjectRole(1L), null, collection);
        assertEquals(2, collection.getErrorMessages().size());
        assertContains(collection.getErrorMessages(), "Can not retrieve a project role actor for a null project.");
        assertTrue(collection.getErrorMessages().contains("The user does not have the JIRA Administrator permission, "
                + "or is not running Enterprise and does not have the Project admin permission."));
    }

    @Test
    public void testSetInvalidActorForProjectRole()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        Project mockProject = new ProjectImpl(new MockGenericValue("project", ImmutableMap.of("id", 1L)));
        mockProjectManager.addProject(mockProject);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true),
                jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(),
                null, null, null, mockProjectManager, null, null, null, null, eventPublisher);
        SimpleErrorCollection collection = new SimpleErrorCollection();

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an RoleActorDoesNotExistException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.
        projectRoleService.setActorsForProjectRole(null, MapBuilder.<String, Set<String>>build(
                MockProjectRoleManager.MockRoleActor.TYPE,
                Sets.<String>newHashSet(MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER, "tester")
        ),
                projectRoleManager.getProjectRole(new Long(1)), mockProject, collection);

        assertContains(collection.getErrorMessages(), "'" + MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER + "' could not be found");
    }

    @Test
    public void testSetActorForProjectRoleDeterminesRightAdditionAndDeletion()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        final Project mockProject = new ProjectImpl(new MockGenericValue("project", ImmutableMap.of("id", 1L)));
        mockProjectManager.addProject(mockProject);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true),
                jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(),
                null, null, null, mockProjectManager, null, null, null, null, eventPublisher)
        {
            @Override
            public void addActorsToProjectRole(com.atlassian.crowd.embedded.api.User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
            {
                assertNull(currentUser);
                CollectionAssert.assertContainsExactly(CollectionBuilder.list("someUser"), actors);
                assertEquals(projectRoleManager.getProjectRole(new Long(1)), projectRole);
                assertEquals(mockProject, project);
                assertEquals(MockProjectRoleManager.MockRoleActor.TYPE, actorType);
            }

            @Override
            public void removeActorsFromProjectRole(com.atlassian.crowd.embedded.api.User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
            {
                assertNull(currentUser);
                CollectionAssert.assertContainsExactly(CollectionBuilder.list("fred", "tester"), actors);
                assertEquals(projectRoleManager.getProjectRole(new Long(1)), projectRole);
                assertEquals(mockProject, project);
                assertEquals(MockProjectRoleManager.MockRoleActor.TYPE, actorType);

            }
        };
        SimpleErrorCollection collection = new SimpleErrorCollection();

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an RoleActorDoesNotExistException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.

        // Als note that the projectRoleManager actually contains two role actors: fred and tester

        projectRoleService.setActorsForProjectRole(null, MapBuilder.<String, Set<String>>build(
                MockProjectRoleManager.MockRoleActor.TYPE,
                Sets.<String>newHashSet("someUser")
        ),
                projectRoleManager.getProjectRole(1L), mockProject, collection);

        assertFalse(collection.hasAnyErrors());
    }

    @Test
    public void testRemoveSelfFromProjectRoleReferencedTwice() throws Exception
    {
        removeActorFromAProjectRole("tester", false, getPermissionManager(true, false), projectRoleManager);
    }

    @Test
    public void testRemoveSelfFromProjectRoleReferencedOnce() throws Exception
    {
        // make sure the
        SimpleErrorCollection errorCollection = removeActorFromAProjectRole("tester", true, getPermissionManager(true, false), new MockProjectRoleManagerWithOneUserReference());
        assertContains(errorCollection.getErrorMessages(), "You can not remove a user/group that will result in completely removing yourself from this role.");
    }

    @Test
    public void testRemoveSomeoneNoPermission() throws Exception
    {
        SimpleErrorCollection errorCollection = removeActorFromAProjectRole("tester", true, getPermissionManager(false, false), projectRoleManager);
        assertContains(errorCollection.getErrorMessages(), "You do not have permission to remove a user/group from this role.");
    }

    @Test
    public void testRemoveSomeoneElseFromProjectRole() throws Exception
    {
        removeActorFromAProjectRole(FRED, false, getPermissionManager(true, false), projectRoleManager);
    }

    @Test
    public void testRemoveAllFromProjectRoleAsGlobalAdmin() throws Exception
    {
        removeActorFromAProjectRole("tester", false, getPermissionManager(false, true), projectRoleManager);
    }

    /**
     * Will update the project role based off the passed in <code>projectRole</code> and checking the
     * <code>currentUser</code> has the correct permissions to perform the update operation. The passed in
     * <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     */
    @Test
    public void testUpdateRole()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        defaultProjectRoleServicePermFalseFalse.updateProjectRole(null, null, collection);

        assertTrue(collection.hasAnyErrors());
        assertContains(collection.getErrorMessages(), "Can not update a project role with a null project role specified.");
        assertContains(collection.getErrorMessages(), "This user does not have the JIRA Administrator permission to perform this operation.");
    }

    @Test
    public void testUpdateRoleNameAlreadyExistsInAnotherRole() throws Exception
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        final ProjectRoleManager mockProjectRoleManager = mock(ProjectRoleManager.class);
        when(mockProjectRoleManager.getProjectRole("Test")).thenReturn(new ProjectRoleImpl(567L, "Test", "blah"));

        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, (ApplicationUser) null)).thenReturn(true);

        final JiraAuthenticationContext mockJiraAuthenticationContext = new MockAuthenticationContext(null);

        DefaultProjectRoleService service = new DefaultProjectRoleService(mockProjectRoleManager, mockPermissionManager,
                mockJiraAuthenticationContext, null, null, null, null, null, null, null, null, null, eventPublisher);

        service.updateProjectRole(null, new ProjectRoleImpl(123L, "Test", "blah"), collection);

        assertTrue(collection.hasAnyErrors());
        assertContains(collection.getErrorMessages(), "A project role with name 'Test' already exists.");
    }

    @Test
    public void testUpdateRoleNameAlreadyExistsInThisRole() throws Exception
    {
        final ProjectRoleImpl passedInRole = new ProjectRoleImpl(123L, "Test", "blah");
        SimpleErrorCollection collection = new SimpleErrorCollection();

        final ProjectRoleManager mockProjectRoleManager = mock(ProjectRoleManager.class);
        when(mockProjectRoleManager.getProjectRole("Test")).thenReturn(new ProjectRoleImpl(123L, "Test", "blah"));

        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, (ApplicationUser) null)).thenReturn(true);

        DefaultProjectRoleService service = new DefaultProjectRoleService(mockProjectRoleManager, mockPermissionManager, null, null, null, null,
                null, null, null, null, null, null, eventPublisher);

        service.updateProjectRole(null, passedInRole, collection);

        assertFalse(collection.hasAnyErrors());
    }

    @Test
    public void testDoesProjectRoleExistForAdministerProjectsPermissionChucksNPEWithNullSchemeFactory()
    {
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, mock(PermissionSchemeManager.class),
                null, null, null, null, null, null, eventPublisher);
        try
        {
            projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(new ProjectImpl(null), projectRoleManager.getProjectRole(new Long(1)));
            fail("should have throw NPE due to null SchemeFactory");
        }
        catch (NullPointerException yay)
        {
            assertContains(yay.getMessage(), SchemeFactory.class.getName());
        }
    }

    @Test
    public void testDoesProjectRoleExistForAdministerProjectsPermissionChucksNPEWithNullPermissionSchemeManager()
    {
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, null,
                null, null, mock(SchemeFactory.class), null, null, null, eventPublisher);
        try
        {
            projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(new ProjectImpl(null), projectRoleManager.getProjectRole(new Long(1)));
            fail("should have throw NPE due to null PermissionSchemeManager");
        }
        catch (NullPointerException yay)
        {
            assertContains(yay.getMessage(), PermissionSchemeManager.class.getName());
        }
    }

    @Test
    public void testDoesProjectRoleExistForAdministerProjectsPermissionChucksNPEWithNullProject()
    {
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null,
                mock(PermissionSchemeManager.class), null, null, mock(SchemeFactory.class), null, null, null, eventPublisher);
        try
        {
            projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(null, projectRoleManager.getProjectRole(new Long(1)));
            fail("should have throw NPE due to null Project");
        }
        catch (NullPointerException yay)
        {
            assertContains(yay.getMessage(), Project.class.getName());
        }
    }

    @Test
    public void testDoesProjectRoleExistForAdministerProjectsPermissionChucksNPEWithNullProjectRole()
    {
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, mock(PermissionSchemeManager.class),
                null, null, mock(SchemeFactory.class), null, null, null, eventPublisher);
        try
        {
            projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(new ProjectImpl(null), null);
            fail("should have throw NPE due to null ProjectRole");
        }
        catch (NullPointerException yay)
        {
            assertContains(yay.getMessage(), ProjectRole.class.getName());
        }
    }

    @Test
    public void testDoesProjectRoleExistForAdministerProjectsPermissionHasPermission() throws GenericEntityException
    {
        PermissionSchemeManager mockPSM = mock(PermissionSchemeManager.class);
        // Return one GenericValue that pretends to be a scheme
        when(mockPSM.getSchemes(any(GenericValue.class))).thenReturn(ImmutableList.<GenericValue>of(new MockGenericValue("Scheme")));

        Collection<SchemeEntity> schemeEntities = ImmutableList.of(new SchemeEntity(ProjectRoleSecurityAndNotificationType.PROJECT_ROLE, "1", new Long(Permissions.PROJECT_ADMIN)));
        Scheme scheme = new Scheme(null, "PermissionScheme", "Default Permission Scheme", schemeEntities);

        SchemeFactory mockSchemeFactory = mock(SchemeFactory.class);
        when(mockSchemeFactory.getSchemeWithEntitiesComparable(any(GenericValue.class))).thenReturn(scheme);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, mockPSM,
                null, null, mockSchemeFactory, null, null, null, eventPublisher);

        assertTrue(projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(new ProjectImpl(null), projectRoleManager.getProjectRole(new Long(1))));
    }

    @Test
    public void testDoesProjectRoleExistForAdministerProjectsPermissionNoPermission() throws GenericEntityException
    {
        PermissionSchemeManager mockPSM = mock(PermissionSchemeManager.class);
        // Return one GenericValue that pretends to be a scheme
        when(mockPSM.getSchemes(any(GenericValue.class))).thenReturn(ImmutableList.<GenericValue>of(new MockGenericValue("Scheme")));

        Collection<SchemeEntity> schemeEntities = ImmutableList.of(new SchemeEntity(ProjectRoleSecurityAndNotificationType.PROJECT_ROLE, "1", new Long(Permissions.EDIT_ISSUE)));
        Scheme scheme = new Scheme(null, "PermissionScheme", "Default Permission Scheme", schemeEntities);

        SchemeFactory mockSchemeFactory = mock(SchemeFactory.class);
        when(mockSchemeFactory.getSchemeWithEntitiesComparable(any(GenericValue.class))).thenReturn(scheme);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, mockPSM,
                null, null, mockSchemeFactory, null, null, null, eventPublisher);

        assertFalse(projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(new ProjectImpl(null), projectRoleManager.getProjectRole(new Long(1))));
    }

    /**
     * Will return the project role actors based off the passed in <code>projectRole</code> and <code>project</code>
     * checking the <code>currentUser</code> has the correct permissions to perform the delete operation.
     */
    @Test
    public void testGetProjectRoleActors()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();
        Project mockProject = new ProjectImpl(new MockGenericValue("Project"));

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an IllegalArgumentException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.
        defaultProjectRoleServicePermFalseFalse.getProjectRoleActors(null, projectRoleManager.getProjectRole(1L), mockProject, collection);

        assertContains(collection.getErrorMessages(), "The user does not have the JIRA Administrator permission, or is not running Enterprise and does not have the Project admin permission.");
    }

    @Test
    public void testGetDefaultRoleActors()
    {
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, false),
                jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, null, null, null,
                null, null, null, null, eventPublisher);
        SimpleErrorCollection collection = new SimpleErrorCollection();

        projectRoleService.getDefaultRoleActors(null, null, collection);

        assertContains(collection.getErrorMessages(), "This user does not have the JIRA Administrator permission to perform this operation.");
        assertContains(collection.getErrorMessages(), "Can not retrieve a role actor for a null project role.");
    }

    @Test
    public void testAddDefaultRoleActors()
    {
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true),
                jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, null, null, null,
                null, null, null, null, eventPublisher);
        SimpleErrorCollection collection = new SimpleErrorCollection();

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an IllegalArgumentException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.
        projectRoleService.addDefaultActorsToProjectRole(null, ImmutableList.of(MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER, "tester"), projectRoleManager.getProjectRole(new Long(1)), MockProjectRoleManager.MockRoleActor.TYPE, collection);

        assertContains(collection.getErrorMessages(), "'tester' is already a member of the project role.");
        assertContains(collection.getErrorMessages(), "'" + MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER + "' could not be found");
    }

    @Test
    public void testAddDefaultRoleActorsNullParams()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        defaultProjectRoleServicePermFalseFalse.addDefaultActorsToProjectRole(null, null, null, null, collection);

        assertTrue(collection.hasAnyErrors());
        assertContains(collection.getErrorMessages(), "Can not update a null role actor.");
        assertContains(collection.getErrorMessages(), "Can not retrieve a role actor for a null project role.");
        assertContains(collection.getErrorMessages(), "This user does not have the JIRA Administrator permission to perform this operation.");
    }

    /**
     * NOTE: This is not tested because this exercises a subset of the validation called by testAddActorsToProjectRole
     */
    @Test
    public void testRemoveDefaultRoleActors()
    {
    }

    @Test
    public void testRemoveAllRoleActosByNameAndType()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        defaultProjectRoleServicePermFalseFalse.removeAllRoleActorsByNameAndType(null, null, null, errorCollection);

        assertContains(errorCollection.getErrorMessages(), "Can not delete role actors without a name specified.");
        assertContains(errorCollection.getErrorMessages(), "Can not delete role actors without a type specified.");
        assertContains(errorCollection.getErrorMessages(), "This user does not have the JIRA Administrator permission to perform this operation.");
    }

    @Test
    public void testRemoveAllRoleActorsByProject()
    {

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        defaultProjectRoleServicePermFalseFalse.removeAllRoleActorsByProject(null, errorCollection);

        assertContains(errorCollection.getErrorMessages(), "Can not delete role actors without a project specified.");
        assertContains(errorCollection.getErrorMessages(), "This user does not have the JIRA Administrator permission to perform this operation.");
    }

    @Test
    public void testGetAssociatedNotificationSchemes()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        defaultProjectRoleServicePermFalseFalse.getAssociatedNotificationSchemes(null, null, errorCollection);
        assertContains(errorCollection.getErrorMessages(), "The project role can not be null.");
    }

    @Test
    public void testGetAssociatedPermissionSchemes()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        defaultProjectRoleServicePermFalseFalse.getAssociatedPermissionSchemes(null, errorCollection);
        assertContains(errorCollection.getErrorMessages(), "The project role can not be null.");
    }

    @Test
    public void testGetAssociatedWorkflows()
            throws InvalidWorkflowDescriptorException, IOException, SAXException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        User user = new MockUser("workflowuser");

        WorkflowManager mockWorkflowManager = mock(WorkflowManager.class);

        //Collection actions = new HashSet();
        MockXmlLoadableJiraWorkflow roleConditionWorkflow = new MockXmlLoadableJiraWorkflow(mockWorkflowManager, PROJECTROLE_WORKFLOW_FILE);
        final String roleConditionWorkflowName = "The default JIRA workflow.";
        roleConditionWorkflow.setName(roleConditionWorkflowName);

        MockXmlLoadableJiraWorkflow anotherRoleConditionWorkflow = new MockXmlLoadableJiraWorkflow(mockWorkflowManager, PROJECTROLE_WORKFLOW_FILE);
        final String anotherWorkflowName = "another " + roleConditionWorkflowName;
        anotherRoleConditionWorkflow.setName(anotherWorkflowName);

        MockXmlLoadableJiraWorkflow unassociatedWorkflow = new MockXmlLoadableJiraWorkflow(mockWorkflowManager, "com/atlassian/jira/upgrade/tasks/upgradetask_build155/simpleworkflow-broken.xml");
        when(mockWorkflowManager.getWorkflows()).thenReturn(ImmutableList.<JiraWorkflow>of(roleConditionWorkflow, unassociatedWorkflow, anotherRoleConditionWorkflow));

        ProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true),
                jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(),
                null, null, mockWorkflowManager, null, null, null, null, null, eventPublisher);

        ProjectRole developerRole = new ProjectRoleImpl(10001L, "Developers", "A role that represents developers in a project");

        MultiMap workflows = projectRoleService.getAssociatedWorkflows(user, developerRole, errorCollection);

        boolean foundRoleConditionWorkflow = false;
        boolean foundAnotherWorkflow = false;
        assertEquals(2, workflows.keySet().size());
        for (Iterator iterator = workflows.entrySet().iterator(); iterator.hasNext(); )
        {
            // check the workflows are right
            Map.Entry e = (Map.Entry) iterator.next();
            final String name = ((JiraWorkflow) e.getKey()).getName();
            if (name.equals(roleConditionWorkflowName))
            {
                foundRoleConditionWorkflow = true;
            }
            else if (name.equals(anotherWorkflowName))
            {
                foundAnotherWorkflow = true;
            }
            Collection actions = (Collection) e.getValue();
            for (Iterator actionsIter = actions.iterator(); actionsIter.hasNext(); )
            {
                // check the action with the condition is the correct one
                ActionDescriptor actionDescriptor = (ActionDescriptor) actionsIter.next();
                assertTrue(actionDescriptor.getName().equals("Start Progress"));
            }
        }
        assertTrue(foundRoleConditionWorkflow);
        assertTrue(foundAnotherWorkflow);
    }

    private SimpleErrorCollection removeActorFromAProjectRole(String userToRemove, boolean errorsExpected, PermissionManager permissionManager, ProjectRoleManager projectRoleManager)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException, GenericEntityException
    {
        ApplicationUser user = getUser("tester", "tester@test.com");
        Project mockProject = new ProjectImpl(new MockGenericValue("Project", ImmutableMap.of("id", 1L)));

        PermissionSchemeManager mockPSM = mock(PermissionSchemeManager.class);
        // Return one GenericValue that pretends to be a scheme
        when(mockPSM.getSchemes(any(GenericValue.class))).thenReturn(ImmutableList.<GenericValue>of(new MockGenericValue("Scheme")));

        Collection<SchemeEntity> schemeEntities = ImmutableList.of(new SchemeEntity(ProjectRoleSecurityAndNotificationType.PROJECT_ROLE, "1", new Long(Permissions.PROJECT_ADMIN)));
        Scheme scheme = new Scheme(null, "PermissionScheme", "Default Permission Scheme", schemeEntities);

        SchemeFactory mockSchemeFactory = mock(SchemeFactory.class);
        when(mockSchemeFactory.getSchemeWithEntitiesComparable(any(GenericValue.class))).thenReturn(scheme);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(mockProject.getGenericValue());
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, permissionManager, jiraAuthenticationContext,
                new MockProjectRoleManager.MockRoleActorFactory(), null, mockPSM, null,
                mockProjectManager, mockSchemeFactory, null, null, null, eventPublisher);

        // tester is automagically included in the ProjectRoleManager mock, that's why it works
        projectRoleService.removeActorsFromProjectRole(user.getDirectoryUser(), ImmutableList.of(userToRemove), projectRoleManager.getProjectRole(1L), mockProject, "mock type", errorCollection);

        assertEquals(errorCollection.getErrorMessages().toString(), errorsExpected, errorCollection.hasAnyErrors());
        return errorCollection;
    }

    private ApplicationUser getUser(String username, String email)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        return new MockApplicationUser(username, username, email);
    }

    private class MockProjectRoleManagerWithOneUserReference extends MockProjectRoleManager
    {

        public ProjectRoleActors getProjectRoleActors(ProjectRole projectRole, Project project)
        {
            if (project == null)
            {
                throw new IllegalArgumentException("Mock bad argument");
            }
            Set<RoleActor> actors = new HashSet<RoleActor>();

            try
            {
                final User tester = new MockUser(TESTER, TESTER, "tester@test.com");
                final User fred = new MockUser(FRED, FRED, "fred@test.com");
                actors.add(new MockRoleActor(1L, projectRole.getId(), project.getId(), ImmutableSet.of(tester), MockRoleActor.TYPE, TESTER));
                actors.add(new MockRoleActor(2L, projectRole.getId(), project.getId(), ImmutableSet.of(fred), MockRoleActor.TYPE, FRED));
            }
            catch (RuntimeException re)
            {
                throw re;
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
            return new ProjectRoleActorsImpl(project.getId(), projectRole.getId(), actors);
        }
    }

    private void assertContains(final String haystack, final String needle)
    {
        assertThat(haystack, containsString(needle));
    }

    private void assertContains(final Collection<String> collection, final String name)
    {
//        assertThat(collection, hasItem(containsString(name)));
        assertThat(collection.toString(), containsString(name));
    }
}
