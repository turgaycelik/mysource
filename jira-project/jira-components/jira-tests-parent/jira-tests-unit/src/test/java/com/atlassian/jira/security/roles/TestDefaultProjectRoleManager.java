package com.atlassian.jira.security.roles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.jira.mock.MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
import static com.atlassian.jira.mock.MockProjectRoleManager.PROJECT_ROLE_TYPE_2;
import static com.atlassian.jira.mock.MockProjectRoleManager.PROJECT_ROLE_TYPE_3;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the DefaultProjectRoleManager.
 */
@SuppressWarnings("deprecation")
public class TestDefaultProjectRoleManager
{
    private static final String I_EXIST_DESC = "I exist desc";
    private static final String I_EXIST = "I EXIST";
    
    @Mock private ProjectManager mockProjectManager;
    @Mock private ProjectRoleAndActorStore mockProjectRoleAndActorStore;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init()
                .addMock(CrowdService.class, new MockCrowdService())
                .addMock(ProjectManager.class, mockProjectManager)
                .addMock(UserManager.class, new MockUserManager());
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testCreateProjectIdToProjectRolesMap() throws Exception
    {
        ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(mockProjectRoleAndActorStore);

        ProjectRoleManager.ProjectIdToProjectRoleIdsMap map;

        // null project IDs returns an empty map
        map = projectRoleManager.createProjectIdToProjectRolesMap((ApplicationUser) null, null);
        assertTrue(map.isEmpty());

        // empty collection of project IDs returns an empty map
        map = projectRoleManager.createProjectIdToProjectRolesMap((ApplicationUser) null, new ArrayList<Long>());
        assertTrue(map.isEmpty());

        final List<Long> expectedProjectIds = Arrays.asList(10L, 20L, 150L);

        final MockProject mockProject1 = new MockProject(10L);
        when(mockProjectManager.getProjectObj(10L)).thenReturn(mockProject1);
        final MockProject mockProject2 = new MockProject(20L);
        when(mockProjectManager.getProjectObj(20L)).thenReturn(mockProject2);
        final MockProject mockProject3 = new MockProject(150L);
        when(mockProjectManager.getProjectObj(150L)).thenReturn(mockProject3);

        final List<Long> project1RoleIds = Arrays.asList(11L, 21L, 31L);
        final List<Long> project2RoleIds = Arrays.asList(101L, 201L, 301L);
        final List<Long> project3RoleIds = Arrays.asList(1100L, 2100L, 3100L);

        final AtomicInteger getProjectRolesCalled = new AtomicInteger(0);

        final ApplicationUser testUser = createMockApplicationUser("dushan","","");

        projectRoleManager = new DefaultProjectRoleManager(null)
        {
            @Override
            public Collection<ProjectRole> getProjectRoles(final ApplicationUser user, final Project project)
            {
                getProjectRolesCalled.incrementAndGet();

                // Test that the correct user object was passed
                assertTrue(user == testUser);

                if (project == mockProject1)
                {
                    return getMockProjectRoles(project1RoleIds);
                }
                else if (project == mockProject2)
                {
                    return getMockProjectRoles(project2RoleIds);
                }
                else if (project == mockProject3)
                {
                    return getMockProjectRoles(project3RoleIds);
                }

                throw new IllegalArgumentException("Unexpected project passed.");
            }

            private Collection<ProjectRole> getMockProjectRoles(final Iterable<Long> mockProjectRoledIds)
            {
                final Collection<ProjectRole> mockProjectRoles = new ArrayList<ProjectRole>();
                for (final long roleId : mockProjectRoledIds)
                {
                    mockProjectRoles.add(new MockProjectRoleManager.MockProjectRole(roleId, null, null));
                }

                return mockProjectRoles;
            }
        };

        // Do the right thing
        map = projectRoleManager.createProjectIdToProjectRolesMap(testUser, expectedProjectIds);

        // Ensure the getRoleIds was called 3 times
        assertEquals(3, getProjectRolesCalled.get());

        // Check that the map has correct contebnts
        assertFalse(map.isEmpty());

        int called = 0;
        for (final ProjectRoleManager.ProjectIdToProjectRoleIdsMap.Entry entry : map)
        {
            called++;
            if (entry.getProjectId().equals(10L))
            {
                assertEquals(project1RoleIds, entry.getProjectRoleIds());
            }
            else if (entry.getProjectId().equals(20L))
            {
                assertEquals(project2RoleIds, entry.getProjectRoleIds());
            }
            else if (entry.getProjectId().equals(150L))
            {
                assertEquals(project3RoleIds, entry.getProjectRoleIds());
            }
            else
            {
                fail("Unexpected project id '" + entry.getProjectId() + "'.");
            }
        }

        // Ensure we have only 3 keys in the map
        assertEquals(3, called);
    }

    @Test
    public void testProjectIdToProjectRoleIdsMap()
    {
        final ProjectRoleManager.ProjectIdToProjectRoleIdsMap map = new ProjectRoleManager.ProjectIdToProjectRoleIdsMap();
        assertTrue(map.isEmpty());

        final Long PROJECT_ID_1 = 1L;
        final Long PROJECT_ID_2 = 2L;

        final Long PROJECT_ROLE_ID_1 = 11L;
        final Long PROJECT_ROLE_ID_2 = 22L;
        final Long PROJECT_ROLE_ID_3 = 33L;

        // adding nulls should not modify the map
        map.add(null, null);
        assertTrue(map.isEmpty());

        // null project ID should not affect the map
        map.add(null, PROJECT_ROLE_ID_1);
        assertTrue(map.isEmpty());

        // adding a null project role ID should not affect the map
        map.add(PROJECT_ID_1, null);
        assertTrue(map.isEmpty());

        map.add(PROJECT_ID_1, PROJECT_ROLE_ID_1);
        assertFalse(map.isEmpty());
        ProjectRoleManager.ProjectIdToProjectRoleIdsMap.Entry entry = map.iterator().next();
        assertEquals(PROJECT_ID_1, entry.getProjectId());
        assertTrue(entry.getProjectRoleIds().contains(PROJECT_ROLE_ID_1));

        // adding a null project role ID should not affect the map
        map.add(PROJECT_ID_1, null);
        // same test as above
        assertFalse(map.isEmpty());
        entry = map.iterator().next();
        assertEquals(PROJECT_ID_1, entry.getProjectId());
        assertTrue(entry.getProjectRoleIds().contains(PROJECT_ROLE_ID_1));

        map.add(PROJECT_ID_1, PROJECT_ROLE_ID_2);
        assertFalse(map.isEmpty());
        entry = map.iterator().next();
        assertEquals(PROJECT_ID_1, entry.getProjectId());
        Collection projectRoleIds = entry.getProjectRoleIds();
        assertEquals(2, projectRoleIds.size());
        assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_1));
        assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_2));

        map.add(PROJECT_ID_1, PROJECT_ROLE_ID_3);
        map.add(PROJECT_ID_2, PROJECT_ROLE_ID_3);
        assertFalse(map.isEmpty());
        for (int i = 0; i < 2; i++)
        {
            entry = map.iterator().next();
            if (PROJECT_ID_1.equals(entry.getProjectId()))
            {
                projectRoleIds = entry.getProjectRoleIds();
                assertEquals(3, projectRoleIds.size());
                assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_1));
                assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_2));
                assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_3));
            }
            else if (PROJECT_ID_2.equals(entry.getProjectId()))
            {
                projectRoleIds = entry.getProjectRoleIds();
                assertEquals(1, projectRoleIds.size());
                assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_3));
            }
            else
            {
                fail();
            }
        }
    }

    @Test
    public void testCreateProjectRoleDuplicateNameError()
    {
        when(mockProjectRoleAndActorStore.getProjectRoleByName((String) any()))
                .thenReturn(new ProjectRoleImpl(I_EXIST, I_EXIST_DESC));

        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(mockProjectRoleAndActorStore);
        try
        {
            projectRoleManager.createRole(new ProjectRoleImpl(I_EXIST, I_EXIST_DESC));
            fail();
        }
        catch (final IllegalArgumentException iae)
        {
            assertEquals("A project role with the provided name: " + I_EXIST + ", already exists in the system.", iae.getMessage());
        }
    }

    @Test
    public void testGetDefaultRoleActors()
    {
        // Since we are just testing the illegal args we can use a null store
        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(null);

        try
        {
            projectRoleManager.getDefaultRoleActors(null);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //everything is good
        }
    }

    @Test
    public void testUpdateDefaultRoleActors()
    {
        // Since we are just testing the illegal args we can use a null store
        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(null);
        try
        {
            // testing that the defaultRoleActors cannot be null
            projectRoleManager.updateDefaultRoleActors(null);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //everything is good
        }

        try
        {
            // Testing that the projectRole cannot be null
            final DefaultRoleActors defaultRoleActors = new DefaultRoleActorsImpl(null, Collections.EMPTY_SET);
            projectRoleManager.updateDefaultRoleActors(defaultRoleActors);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //everything is good
        }
    }

    @Test
    public void testApplyDefaultRolesToNullProject()
    {
        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(null);
        try
        {
            // testing that the project cannot be null and exception is thrown
            projectRoleManager.applyDefaultsRolesToProject(null);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //everything is good
        }
    }

    @Test
    public void testIsUserInRole() throws Exception
    {
        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(mockProjectRoleAndActorStore);

        // Prepare the mock object to pass in and return
        final MockProject mockProject = new MockProject();
        mockProject.setId(123L);
        final User user1 = createMockUser("roleactoruser");
        final User user2 = createMockUser("roleactoruser2");
        final Set<User> users = new HashSet<User>();
        users.add(user1);

        final RoleActor actor = new MyRoleActor(users);
        final Set<RoleActor> actors = new HashSet<RoleActor>();
        actors.add(actor);
        final ProjectRoleActors projectRoleActors = new ProjectRoleActorsImpl(mockProject.getId(),
            PROJECT_ROLE_TYPE_1.getId(), actors);

        when(mockProjectRoleAndActorStore.getProjectRoleActors(PROJECT_ROLE_TYPE_1.getId(), mockProject.getId())).thenReturn(projectRoleActors);
        final boolean user1InRole = projectRoleManager.isUserInProjectRole(user1, PROJECT_ROLE_TYPE_1, mockProject);
        final boolean user2InRole = projectRoleManager.isUserInProjectRole(user2, PROJECT_ROLE_TYPE_1, mockProject);

        assertTrue("User 1 should be in role", user1InRole);
        assertFalse("User 2 should not be in role", user2InRole);
    }

    @Test
    public void testNullInputsForIsUserInRole()
    {
        try
        {
            final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(mockProjectRoleAndActorStore);
            projectRoleManager.isUserInProjectRole((ApplicationUser) null, null, null);
            fail("We should have thrown an IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {

        }
    }

    private ApplicationUser createMockApplicationUser(String userName, String name, String email)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        createMockUser(userName, name, email);
        return ApplicationUsers.from(ComponentAccessor.getCrowdService().getUser(userName));
    }

    private User createMockUser(String userName, String name, String email)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        User user = new MockUser(userName, name, email);
        CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addUser(user, "password");
        return user;
    }

    private User createMockUser(String userName) throws Exception
    {
        return createMockUser(userName, userName, "");
    }

    private void addUserToGroup(User user, Group group)
            throws OperationNotPermittedException, InvalidGroupException
    {
        CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addUserToGroup(user, group);
    }

    @Test
    public void testGetProjectRoles() throws Exception
    {
        final MockProject mockProject = new MockProject();
        mockProject.setKey("PRJ");
        final long projectId = 456;
        mockProject.setId(projectId);
        mockProject.setName("Projectini");

        final ApplicationUser userInRole = createMockApplicationUser("userinrole","","");
        final ApplicationUser userInGroupInRole = createMockApplicationUser("useringroupinrole", "", "");
        final Group groupInRole = createMockGroup("groupinrole");
        addUserToGroup(userInGroupInRole.getDirectoryUser(), groupInRole);
        final User userNotInRole = createMockUser("usernotinrole");
        final Group groupNotInRole = createMockGroup("groupnotinrole");
        addUserToGroup(userNotInRole, groupNotInRole);

        final Set<RoleActor> actors = new HashSet<RoleActor>();
        final Long roleId = PROJECT_ROLE_TYPE_1.getId();
        final RoleActor userRoleActor = new MockUserRoleActor(roleId, projectId, userInRole);
        actors.add(userRoleActor);
        final RoleActor groupRoleActor = new MockGroupRoleActor(roleId, projectId, groupInRole);
        actors.add(groupRoleActor);

        final ProjectRoleActors projectRoleActorsWithUsers = new ProjectRoleActorsImpl(projectId, roleId, actors);

        final ProjectRoleAndActorStore mockProjectRoleAndActorStore = mock(ProjectRoleAndActorStore.class);
        when(mockProjectRoleAndActorStore.getAllProjectRoles())
                .thenReturn(Arrays.asList(PROJECT_ROLE_TYPE_1, PROJECT_ROLE_TYPE_2, PROJECT_ROLE_TYPE_3));
        when(mockProjectRoleAndActorStore.getProjectRoleActors(PROJECT_ROLE_TYPE_1.getId(), projectId))
                .thenReturn(projectRoleActorsWithUsers);
        when(mockProjectRoleAndActorStore.getProjectRoleActors(PROJECT_ROLE_TYPE_2.getId(), projectId))
                .thenReturn(new ProjectRoleActorsImpl(projectId, PROJECT_ROLE_TYPE_2.getId(), Collections.EMPTY_SET));
        when(mockProjectRoleAndActorStore.getProjectRoleActors(PROJECT_ROLE_TYPE_3.getId(), projectId))
                .thenReturn(new ProjectRoleActorsImpl(projectId, PROJECT_ROLE_TYPE_3.getId(), Collections.EMPTY_SET));

        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(mockProjectRoleAndActorStore);

        assertContainsOnly(PROJECT_ROLE_TYPE_1, projectRoleManager.getProjectRoles(userInRole, mockProject));
        assertContainsOnly(PROJECT_ROLE_TYPE_1, projectRoleManager.getProjectRoles(userInGroupInRole, mockProject));
        assertTrue(projectRoleManager.getProjectRoles(userNotInRole, mockProject).isEmpty());
    }

    /**
     * Asserts that the given object is the only thing in the given collection.
     */
    private <E> void assertContainsOnly(final E expected, final Collection<E> actual)
    {
        assertEquals(1, actual.size());
        assertEquals(expected, actual.iterator().next());
    }

    private Group createMockGroup(String groupName) throws Exception
    {
        Group group = new MockGroup(groupName);
        CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addGroup(group);
        return group;
    }

    /**
     * Mock that could be promoted to the test source tree for making roles stuff easier to test.
     */
    private static class MyRoleActor implements RoleActor
    {
        private final Set<User> users;

        public MyRoleActor(final Set<User> users)
        {
            this.users = users;
        }

        public String getDescriptor()
        {
            return null;
        }

        public Long getId()
        {
            return null;
        }

        public String getParameter()
        {
            return null;
        }

        public Long getProjectRoleId()
        {
            return null;
        }

        public String getType()
        {
            return null;
        }

        public Set<User> getUsers()
        {
            return users;
        }

        @Override
        public boolean contains(ApplicationUser user)
        {
            return users.contains(user.getDirectoryUser());
        }

        public boolean contains(final User user)
        {
            return users.contains(user);
        }

        @Override
        public boolean isActive()
        {
            return true;
        }

        public void setId(@SuppressWarnings("unused") final Long id) {}
    }
}
