package com.atlassian.jira.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.plugin.GlobalPermissionTypesManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.plugin.ProjectPermissionOverride;
import com.atlassian.jira.security.plugin.ProjectPermissionOverrideModuleDescriptor;
import com.atlassian.jira.security.plugin.ProjectPermissionTypesManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.permission.ProjectPermissions.ASSIGN_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultPermissionManager
{
    protected MockUser bob;
    protected Project project;
    protected Project project2;
    protected GenericValue scheme;
    protected MockIssue issue;

    @Mock @AvailableInContainer
    private PermissionSchemeManager permissionSchemeManager;
    @Mock @AvailableInContainer
    private ProjectManager projectManager;
    @Mock @AvailableInContainer
    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    @Mock @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;
    @Mock
    private GroupManager groupManager;
    @Mock
    @AvailableInContainer
    private UserManager userManager;
    @AvailableInContainer
    private GlobalPermissionManager globalPermissionManager = MockGlobalPermissionManager.withSystemGlobalPermissions();
    @Mock
    private GlobalPermissionTypesManager globalPermissionTypesManager;
    @Mock
    private UserUtil userUtil;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private ProjectPermissionTypesManager projectPermissionTypesManager;

    @Rule
    public MockitoContainer initMockitoMocks = MockitoMocksInContainer.rule(this);
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private DefaultPermissionManager permissionManager;

    @Before
    public void setUp() throws Exception
    {
        when(globalPermissionTypesManager.getAll()).thenReturn(Lists.<GlobalPermissionType>newArrayList());
        permissionManager = new DefaultPermissionManager(projectPermissionTypesManager);

        bob = new MockUser("bob");

        scheme = new MockGenericValue("PermissionScheme", ImmutableMap.of("id", 10L, "name", "Test Scheme", "description", "test"));

        project = new MockProject(new MockGenericValue("Project", ImmutableMap.of("id", 2L, "lead", "paul")));
        project2 = new MockProject(new MockGenericValue("Project", ImmutableMap.of("id", 3L)));

        ApplicationUser bobApplicationUser = mock(ApplicationUser.class);
        when(userManager.getUserByKey("bob")).thenReturn(bobApplicationUser);
        when(bobApplicationUser.getDirectoryUser()).thenReturn(bob);

        issue = new MockIssue(new MockGenericValue("Issue", ImmutableMap.of("key", "ABC-1", "project", 2L, "reporter", "bob", "assignee", "bob", "security", 15L)))
        {
            @Override
            public Project getProjectObject()
            {
                return project;
            }

            @Override
            public Long getId()
            {
                return 1l;
            }
        };
    }

    /**
     * Cannot call hasPermission (project) for a global permission
     */
    @Test
    public void testHasPermissionForProjectFailGlobalPermission()
    {
        DefaultPermissionManager permissionManager = new DefaultPermissionManager(projectPermissionTypesManager)
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return true;
            }
        };

        try
        {
            permissionManager.hasPermission(0, (Project) null, (User) null, false);
            fail("Should throw IllegalArgument exception due to global permission id");
        }
        catch (IllegalArgumentException e)
        {
            //expected
            assertEquals(e.getMessage(), "PermissionType passed to this function must NOT be a global permission, 0 is global");
        }
    }

    /**
     * Cannot check permissions for null project
     */
    @Test
    public void testHasPermissionForProjectFailNullProject()
    {
        DefaultPermissionManager permissionManager = new DefaultPermissionManager(projectPermissionTypesManager)
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }
        };

        try
        {
            permissionManager.hasPermission(0, (Project) null, (User) null, false);
            fail("Should throw IllegalArgument exception due to null Project");
        }
        catch (IllegalArgumentException e)
        {
            //expected
            assertEquals(e.getMessage(), "The Project argument and its backing generic value must not be null");
        }
    }

    /**
     * A project with a null backing GV is a serious state problem for the domain object - also it's required by
     * underlying methods
     */
    @Test
    public void testHasPermissionForProjectFailNullProjectBackingGV()
    {
        DefaultPermissionManager permissionManager = new DefaultPermissionManager(projectPermissionTypesManager)
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }
        };

        Project project = new MockProject()
        {
            public GenericValue getGenericValue()
            {
                return null;
            }
        };

        try
        {
            permissionManager.hasPermission(0, project, (User) null, false);
            fail("Should throw IllegalArgument exception due to null backing GV for Project");
        }
        catch (IllegalArgumentException e)
        {
            //expected
            assertEquals(e.getMessage(), "The Project argument and its backing generic value must not be null");
        }
    }

    /**
     * If issue object's GV is null, need to defer to the project object for permission check.
     */
    @Test
    public void testHasPermissionForIssueCreation()
    {
        final int testPermission = Permissions.CLOSE_ISSUE;

        DefaultPermissionManager permissionManager = new DefaultPermissionManager(projectPermissionTypesManager)
        {
            public boolean hasPermission(final int permissionsId, final Issue entity, final User user)
            {
                // verify arguments are called through correctly
                assertEquals(testPermission, permissionsId);
                assertEquals(issue, entity);
                assertNull(user);
                return true;
            }

            public boolean hasPermission(int permissionsId, Project project, User user, boolean issueCreation)
            {
                // verify arguments are called through correctly
                assertEquals(testPermission, permissionsId);
                assertEquals(project, project);
                assertNull(user);
                assertEquals(true, issueCreation);
                return true;
            }
        };

        assertTrue(permissionManager.hasPermission(testPermission, issue, (User) null));
        assertTrue(permissionManager.hasPermission(testPermission, issue, (User) null));
    }

    @Test
    public void testProtectedMethodHasProjectPermissionAnonymous()
    {
        MockGenericValue projectGv = new MockGenericValue("Project", 1l);
        MockProject project = new MockProject(projectGv);

        when(permissionSchemeManager.hasSchemeAuthority(ASSIGN_ISSUES, projectGv)).thenReturn(true);
        when(projectPermissionTypesManager.exists(ASSIGN_ISSUES)).thenReturn(true);

        assertTrue(permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, project, (User) null));
        assertTrue(permissionManager.hasPermission(ASSIGN_ISSUES, project, (User) null));
    }

    @Test
    public void protectedMethodHasProjectPermissionSpecifiedUser()
    {
        when(permissionSchemeManager.hasSchemeAuthority(ASSIGN_ISSUES, project.getGenericValue(), bob, false)).thenReturn(false);

        assertFalse(permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, project, bob, false));
        assertFalse(permissionManager.hasPermission(ASSIGN_ISSUES, project, bob, false));
    }

    @Test
    public void testGetProjectsNoProjectsExist() throws Exception
    {
        DefaultPermissionManager permissionManager = new DefaultPermissionManager(projectPermissionTypesManager)
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }
        };

        assertFalse(permissionManager.hasProjects(0, bob));
    }

    @Test
    public void testGetProjectsUserHasNoVisibleProjects() throws Exception
    {
        DefaultPermissionManager permissionManager = new DefaultPermissionManager(projectPermissionTypesManager)
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }

            public boolean hasPermission(int permissionsId, GenericValue projectGV, User user)
            {
                return false;
            }
        };
        when(projectManager.getProjects()).thenReturn(ImmutableList.<GenericValue>of(new MockGenericValue("Project"), new MockGenericValue("Project")));
        assertFalse(permissionManager.hasProjects(0, bob));
    }

    @Test
    public void testGetProjectObjectsNoProjectsExist() throws Exception
    {
        DefaultPermissionManager permissionManager = new DefaultPermissionManager(projectPermissionTypesManager)
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }

            @Override
            public boolean hasPermission(final int permissionsId, final Project project, final User user)
            {
                return true;
            }

        };

        assertTrue(permissionManager.getProjectObjects(Permissions.BROWSE, bob).isEmpty());

    }

    @Test
    public void testGetProjectObjectsProjectsExist() throws Exception
    {
        MockGenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("name", "proj", "key", "key", "id", 1000L));
        final Project project = new MockProject(mockProjectGV);
        final List<Project> projects = Collections.singletonList(project);

        when(projectManager.getProjectObjects()).thenReturn(projects);
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        DefaultPermissionManager permissionManager = new DefaultPermissionManager(projectPermissionTypesManager)
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }

            @Override
            public boolean hasPermission(final ProjectPermissionKey permissionsKey, final Project project, final User user)
            {
                return true;
            }

        };

        assertEquals(projects, permissionManager.getProjectObjects(Permissions.BROWSE, bob));
        assertEquals(projects, permissionManager.getProjectObjects(BROWSE_PROJECTS, bob));
    }

    @Test
    public void shouldNotAllowToCheckPermissionAgainstEntityWhenItsGlobal()
    {
        exception.expect(IllegalArgumentException.class);
        permissionManager.hasPermission(Permissions.ADMINISTER, (GenericValue) null, bob, false);
    }

    @Test
    public void shouldCheckPermissionsAgainstProjectWhenProjectGivenLegacy()
    {
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        permissionManager.hasPermission(Permissions.BROWSE, project, bob, true);

        verify(permissionSchemeManager).hasSchemeAuthority(BROWSE_PROJECTS, project.getGenericValue(), bob, true);
    }

    @Test
    public void shouldCheckPermissionsAgainstProjectWhenProjectGiven()
    {
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        permissionManager.hasPermission(BROWSE_PROJECTS, project, bob, true);

        verify(permissionSchemeManager).hasSchemeAuthority(BROWSE_PROJECTS, project.getGenericValue(), bob, true);
    }

    @Test
    public void shouldCheckPermissionAgainstProjectsIssueWhenIssueGivenLegacy()
    {
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        permissionManager.hasPermission(Permissions.BROWSE, issue, bob);

        verify(permissionSchemeManager).hasSchemeAuthority(BROWSE_PROJECTS, project.getGenericValue(), bob, false);
        verify(permissionSchemeManager, never()).hasSchemeAuthority(BROWSE_PROJECTS, issue.getGenericValue(), bob, false);
    }

    @Test
    public void shouldCheckPermissionAgainstProjectsIssueWhenIssueGiven()
    {
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        permissionManager.hasPermission(BROWSE_PROJECTS, issue, bob);

        verify(permissionSchemeManager).hasSchemeAuthority(BROWSE_PROJECTS, project.getGenericValue(), bob, false);
        verify(permissionSchemeManager, never()).hasSchemeAuthority(BROWSE_PROJECTS, issue.getGenericValue(), bob, false);
    }

    @Test
    public void shouldCheckPermissionAgainstIssueWhenIssueGivenAndProjectCheckWasSuccessfulLegacy()
    {
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        issue.setSecurityLevelId((long) Permissions.BROWSE);
        when(permissionSchemeManager.hasSchemeAuthority(BROWSE_PROJECTS, project.getGenericValue(), bob, false)).thenReturn(true);

        permissionManager.hasPermission(Permissions.BROWSE, issue, bob);

        verify(permissionSchemeManager).hasSchemeAuthority(BROWSE_PROJECTS, issue.getGenericValue(), bob, false);
    }

    @Test
    public void shouldCheckPermissionAgainstIssueWhenIssueGivenAndProjectCheckWasSuccessful()
    {
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        issue.setSecurityLevelId((long) Permissions.BROWSE);
        when(permissionSchemeManager.hasSchemeAuthority(BROWSE_PROJECTS, project.getGenericValue(), bob, false)).thenReturn(true);

        permissionManager.hasPermission(BROWSE_PROJECTS, issue, bob);

        verify(permissionSchemeManager).hasSchemeAuthority(BROWSE_PROJECTS, issue.getGenericValue(), bob, false);
    }

    @Test
    public void shouldCheckAgainstIssueSecurityWhenUserHasGeneralAccess()
    {
        issue.setSecurityLevelId(1L);
        when(permissionSchemeManager.hasSchemeAuthority(ProjectPermissions.ADD_COMMENTS, project.getGenericValue(), bob, false)).thenReturn(true);
        when(permissionSchemeManager.hasSchemeAuthority(ProjectPermissions.ADD_COMMENTS, issue.getGenericValue(), bob, false)).thenReturn(true);
        when(issueSecuritySchemeManager.hasSchemeAuthority(1L, issue.getGenericValue(), bob, false)).thenReturn(true);
        when(projectPermissionTypesManager.exists(ProjectPermissions.ADD_COMMENTS)).thenReturn(true);

        assertTrue(permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, bob));
        assertTrue(permissionManager.hasPermission(ProjectPermissions.ADD_COMMENTS, issue, bob));
    }

    @Test
    public void shouldNotAllowAccessWhenPermsAreGoodButUserIsNotActive()
    {
        when(permissionSchemeManager.hasSchemeAuthority(BROWSE_PROJECTS, project.getGenericValue(), bob, false)).thenReturn(true);
        when(permissionSchemeManager.hasSchemeAuthority(BROWSE_PROJECTS, issue.getGenericValue(), bob, true)).thenReturn(true);
        bob.setActive(false);

        assertFalse(permissionManager.hasPermission(Permissions.BROWSE, issue, bob));
        assertFalse(permissionManager.hasPermission(BROWSE_PROJECTS, issue, bob));
    }

    @Test
    public void permissionOverriddenLegacy()
    {
        ApplicationUser user = new MockApplicationUser("user");
        Project project = new MockProject(1l);
        Issue issue = mock(Issue.class);
        when(issue.getProjectObject()).thenReturn(project);
        when(issue.getSecurityLevelId()).thenReturn(1L);
        when(permissionSchemeManager.hasSchemeAuthority(eq(ASSIGN_ISSUES), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(issueSecuritySchemeManager.hasSchemeAuthority(eq(1L), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        ProjectPermissionOverride projectPermissionOverride = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride.hasPermission(argThat(permissionKey(ASSIGN_ISSUES)), eq(project), eq(user))).thenReturn(ProjectPermissionOverride.Decision.DENY);
        ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(ProjectPermissionOverrideModuleDescriptor.class)).thenReturn(moduleDescriptors);
        when(projectPermissionTypesManager.exists(ASSIGN_ISSUES)).thenReturn(true);

        assertThat(permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, issue, user), Matchers.is(false));
        verify(projectPermissionOverride).hasPermission(argThat(permissionKey(ASSIGN_ISSUES)), eq(project), eq(user));
    }

    @Test
    public void permissionOverridden()
    {
        ApplicationUser user = new MockApplicationUser("user");
        Project project = new MockProject(1l);
        Issue issue = mock(Issue.class);
        when(issue.getProjectObject()).thenReturn(project);
        when(issue.getSecurityLevelId()).thenReturn(1L);
        when(permissionSchemeManager.hasSchemeAuthority(eq(ASSIGN_ISSUES), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(issueSecuritySchemeManager.hasSchemeAuthority(eq(1L), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        ProjectPermissionOverride projectPermissionOverride = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride.hasPermission(argThat(permissionKey(ASSIGN_ISSUES)), eq(project), eq(user))).thenReturn(ProjectPermissionOverride.Decision.DENY);
        ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(ProjectPermissionOverrideModuleDescriptor.class)).thenReturn(moduleDescriptors);
        when(projectPermissionTypesManager.exists(ASSIGN_ISSUES)).thenReturn(true);

        assertThat(permissionManager.hasPermission(ASSIGN_ISSUES, issue, user), Matchers.is(false));
        verify(projectPermissionOverride).hasPermission(argThat(permissionKey(ASSIGN_ISSUES)), eq(project), eq(user));
    }

    @Test
    public void permissionOverrideExceptionHandledAndCorePermissionCheckReturned()
    {
        issue.setSecurityLevelId(1L);
        ApplicationUser user = new MockApplicationUser("user");
        ProjectPermissionOverride projectPermissionOverride = mock(ProjectPermissionOverride.class);
        doAnswer(new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                throw new RuntimeException("sorry mate");
            }
        }).when(projectPermissionOverride).hasPermission(argThat(permissionKey(Permissions.ASSIGN_ISSUE)), eq(project), eq(user));
        ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(eq(ProjectPermissionOverrideModuleDescriptor.class))).thenReturn(moduleDescriptors);
        when(issueSecuritySchemeManager.hasSchemeAuthority(eq(1L), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(permissionSchemeManager.hasSchemeAuthority(eq(ASSIGN_ISSUES), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(projectPermissionTypesManager.exists(ASSIGN_ISSUES)).thenReturn(true);

        assertThat(permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, issue, user), Matchers.is(true));
        verify(projectPermissionOverride).hasPermission(argThat(permissionKey(ASSIGN_ISSUES)), eq(project), eq(user));
    }

    @Test
    public void permissionOverrideNotExecutedForBrowsePermissionLegacy()
    {
        ApplicationUser user = new MockApplicationUser("user");
        issue.setSecurityLevelId(1L);
        when(issueSecuritySchemeManager.hasSchemeAuthority(eq(1L), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(permissionSchemeManager.hasSchemeAuthority(eq(BROWSE_PROJECTS), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        ProjectPermissionOverride projectPermissionOverride = mock(ProjectPermissionOverride.class);
        final ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(eq(ProjectPermissionOverrideModuleDescriptor.class))).thenReturn(moduleDescriptors);

        assertThat(permissionManager.hasPermission(Permissions.BROWSE, issue, user), Matchers.is(true));
        verify(projectPermissionOverride, never()).hasPermission(argThat(permissionKey(BROWSE_PROJECTS)), eq(project), eq(user));
    }

    @Test
    public void permissionOverrideNotExecutedForBrowsePermission()
    {
        ApplicationUser user = new MockApplicationUser("user");
        issue.setSecurityLevelId(1L);
        when(issueSecuritySchemeManager.hasSchemeAuthority(eq(1L), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(permissionSchemeManager.hasSchemeAuthority(eq(BROWSE_PROJECTS), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        ProjectPermissionOverride projectPermissionOverride = mock(ProjectPermissionOverride.class);
        final ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(eq(ProjectPermissionOverrideModuleDescriptor.class))).thenReturn(moduleDescriptors);

        assertThat(permissionManager.hasPermission(BROWSE_PROJECTS, issue, user), Matchers.is(true));
        verify(projectPermissionOverride, never()).hasPermission(argThat(permissionKey(BROWSE_PROJECTS)), eq(project), eq(user));
    }

    @Test
    public void permissionOverridingForGenericEntity()
    {
        User user = new MockUser("user");
        GenericValue gv = new MockGenericValue("Project", 1l);
        ProjectPermissionOverride projectPermissionOverride = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride.hasPermission(argThat(permissionKey(ProjectPermissions.DELETE_ALL_ATTACHMENTS)), any(Project.class), any(ApplicationUser.class))).thenReturn(ProjectPermissionOverride.Decision.DENY);
        when(permissionSchemeManager.hasSchemeAuthority(eq(ProjectPermissions.DELETE_ALL_ATTACHMENTS), any(GenericValue.class), eq(user), anyBoolean())).thenReturn(true);
        ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(eq(ProjectPermissionOverrideModuleDescriptor.class))).thenReturn(moduleDescriptors);

        assertThat(permissionManager.hasPermission(Permissions.BROWSE, gv, user), Matchers.is(false));
        assertThat(permissionManager.hasPermission(BROWSE_PROJECTS, gv, user), Matchers.is(false));
    }

    @Test
    public void permissionOverridingForUnknownEntityLegacy()
    {
        User user = new MockUser("user");
        GenericValue gv = new MockGenericValue("Project2", 1l);
        ProjectPermissionOverride projectPermissionOverride = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride.hasPermission(argThat(permissionKey(ProjectPermissions.DELETE_ALL_ATTACHMENTS)), any(Project.class), any(ApplicationUser.class))).thenReturn(ProjectPermissionOverride.Decision.DENY);
        when(permissionSchemeManager.hasSchemeAuthority(eq(ProjectPermissions.DELETE_ALL_ATTACHMENTS), any(GenericValue.class), eq(user), anyBoolean())).thenReturn(true);
        ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(eq(ProjectPermissionOverrideModuleDescriptor.class))).thenReturn(moduleDescriptors);
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        exception.expect(IllegalArgumentException.class);
        permissionManager.hasPermission(Permissions.BROWSE, gv, user);
    }

    @Test
    public void permissionOverridingForUnknownEntity()
    {
        User user = new MockUser("user");
        GenericValue gv = new MockGenericValue("Project2", 1l);
        ProjectPermissionOverride projectPermissionOverride = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride.hasPermission(argThat(permissionKey(ProjectPermissions.DELETE_ALL_ATTACHMENTS)), any(Project.class), any(ApplicationUser.class))).thenReturn(ProjectPermissionOverride.Decision.DENY);
        when(permissionSchemeManager.hasSchemeAuthority(eq(ProjectPermissions.DELETE_ALL_ATTACHMENTS), any(GenericValue.class), eq(user), anyBoolean())).thenReturn(true);
        ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(eq(ProjectPermissionOverrideModuleDescriptor.class))).thenReturn(moduleDescriptors);
        when(projectPermissionTypesManager.exists(BROWSE_PROJECTS)).thenReturn(true);

        exception.expect(IllegalArgumentException.class);
        permissionManager.hasPermission(BROWSE_PROJECTS, gv, user);
    }

    @Test
    public void permissionNotOverriddenAllAbstainedLegacy()
    {
        issue.setSecurityLevelId(1L);
        ApplicationUser user = new MockApplicationUser("username");
        ProjectPermissionOverride projectPermissionOverride1 = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride1.hasPermission(any(ProjectPermissionKey.class), Mockito.any(Project.class), Mockito.any(ApplicationUser.class))).thenReturn(ProjectPermissionOverride.Decision.ABSTAIN);
        ProjectPermissionOverride projectPermissionOverride2 = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride2.hasPermission(any(ProjectPermissionKey.class), Mockito.any(Project.class), Mockito.any(ApplicationUser.class))).thenReturn(ProjectPermissionOverride.Decision.ABSTAIN);
        when(permissionSchemeManager.hasSchemeAuthority(eq(ASSIGN_ISSUES), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(issueSecuritySchemeManager.hasSchemeAuthority(eq(1L), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(projectPermissionTypesManager.exists(ASSIGN_ISSUES)).thenReturn(true);

        ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride1), permissionOverrideModuleDescriptor(projectPermissionOverride2));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(eq(ProjectPermissionOverrideModuleDescriptor.class))).thenReturn(moduleDescriptors);

        assertThat(permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, issue, user), Matchers.is(true));
        verify(projectPermissionOverride1).hasPermission(any(ProjectPermissionKey.class), any(Project.class), any(ApplicationUser.class));
        verify(projectPermissionOverride2).hasPermission(any(ProjectPermissionKey.class), any(Project.class), any(ApplicationUser.class));
    }

    @Test
    public void permissionNotOverriddenAllAbstained()
    {
        issue.setSecurityLevelId(1L);
        ApplicationUser user = new MockApplicationUser("username");
        ProjectPermissionOverride projectPermissionOverride1 = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride1.hasPermission(any(ProjectPermissionKey.class), Mockito.any(Project.class), Mockito.any(ApplicationUser.class))).thenReturn(ProjectPermissionOverride.Decision.ABSTAIN);
        ProjectPermissionOverride projectPermissionOverride2 = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride2.hasPermission(any(ProjectPermissionKey.class), Mockito.any(Project.class), Mockito.any(ApplicationUser.class))).thenReturn(ProjectPermissionOverride.Decision.ABSTAIN);
        when(permissionSchemeManager.hasSchemeAuthority(eq(ASSIGN_ISSUES), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(issueSecuritySchemeManager.hasSchemeAuthority(eq(1L), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(true);
        when(projectPermissionTypesManager.exists(ASSIGN_ISSUES)).thenReturn(true);

        ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride1), permissionOverrideModuleDescriptor(projectPermissionOverride2));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(eq(ProjectPermissionOverrideModuleDescriptor.class))).thenReturn(moduleDescriptors);

        assertThat(permissionManager.hasPermission(ASSIGN_ISSUES, issue, user), Matchers.is(true));
        verify(projectPermissionOverride1).hasPermission(any(ProjectPermissionKey.class), any(Project.class), any(ApplicationUser.class));
        verify(projectPermissionOverride2).hasPermission(any(ProjectPermissionKey.class), any(Project.class), any(ApplicationUser.class));
    }

    @Test
    public void permissionOverridingNotExecutedForPermissionDeniedLegacy()
    {
        ApplicationUser user = new MockApplicationUser("username");
        Project project = new MockProject(1l);
        Issue issue = mock(Issue.class);
        when(issue.getProjectObject()).thenReturn(project);
        ProjectPermissionOverride projectPermissionOverride = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride.hasPermission(any(ProjectPermissionKey.class), Mockito.any(Project.class), Mockito.any(ApplicationUser.class))).thenReturn(ProjectPermissionOverride.Decision.ABSTAIN);
        ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(eq(ProjectPermissionOverrideModuleDescriptor.class))).thenReturn(moduleDescriptors);
        when(permissionSchemeManager.hasSchemeAuthority(eq(ASSIGN_ISSUES), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(false);

        assertThat(permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, issue, user), Matchers.is(false));
        verify(projectPermissionOverride, never()).hasPermission(any(ProjectPermissionKey.class), eq(project), eq(user));
    }

    @Test
    public void permissionOverridingNotExecutedForPermissionDenied()
    {
        ApplicationUser user = new MockApplicationUser("username");
        Project project = new MockProject(1l);
        Issue issue = mock(Issue.class);
        when(issue.getProjectObject()).thenReturn(project);
        ProjectPermissionOverride projectPermissionOverride = mock(ProjectPermissionOverride.class);
        when(projectPermissionOverride.hasPermission(any(ProjectPermissionKey.class), Mockito.any(Project.class), Mockito.any(ApplicationUser.class))).thenReturn(ProjectPermissionOverride.Decision.ABSTAIN);
        ArrayList<ProjectPermissionOverrideModuleDescriptor> moduleDescriptors = Lists.newArrayList(permissionOverrideModuleDescriptor(projectPermissionOverride));
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(eq(ProjectPermissionOverrideModuleDescriptor.class))).thenReturn(moduleDescriptors);
        when(permissionSchemeManager.hasSchemeAuthority(eq(ASSIGN_ISSUES), any(GenericValue.class), Mockito.any(User.class), anyBoolean())).thenReturn(false);

        assertThat(permissionManager.hasPermission(ASSIGN_ISSUES, issue, user), Matchers.is(false));
        verify(projectPermissionOverride, never()).hasPermission(any(ProjectPermissionKey.class), eq(project), eq(user));
    }

    @Test
    public void permissionManagerForNullUserShouldDelegateToGlobal() throws Exception
    {
        final DefaultPermissionManager pm = new DefaultPermissionManager(projectPermissionTypesManager);

        // test admin permission with no scheme
        pm.hasPermission(Permissions.ADMINISTER, (User) null);
        Mockito.verify(globalPermissionManager).hasPermission(Permissions.ADMINISTER);

        // test group filter permission with no scheme
        pm.hasPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, (User) null);
        Mockito.verify(globalPermissionManager).hasPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS);

        // test group filter permission with no scheme
        pm.hasPermission(Permissions.CREATE_SHARED_OBJECTS, (User) null);
        Mockito.verify(globalPermissionManager).hasPermission(Permissions.CREATE_SHARED_OBJECTS);

    }

    @Test
    public void usePermissionWithNoSchemeShouldCauseIllegalArgExc() throws Exception
    {
        globalPermissionManager = new DefaultGlobalPermissionManager(mock(CrowdService.class), mock(OfBizDelegator.class), mock(EventPublisher.class), globalPermissionTypesManager, new MemoryCacheManager());

        // test use permission with no scheme
        try
        {
            globalPermissionManager.addPermission(new GlobalPermissionType(GlobalPermissionKey.USE.getKey(), null, null, false), null);
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }

        try
        {
            globalPermissionManager.addPermission(Permissions.BROWSE, null);
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    private static ProjectPermissionOverrideModuleDescriptor permissionOverrideModuleDescriptor(final ProjectPermissionOverride projectPermissionOverride)
    {
        ProjectPermissionOverrideModuleDescriptor projectPermissionOverrideModuleDescriptor = mock(ProjectPermissionOverrideModuleDescriptor.class);
        when(projectPermissionOverrideModuleDescriptor.getModule()).thenReturn(projectPermissionOverride);
        return projectPermissionOverrideModuleDescriptor;
    }

    private Matcher<ProjectPermissionKey> permissionKey(final int permissionid)
    {
        return new ArgumentMatcher<ProjectPermissionKey>()
        {

            @Override
            public boolean matches(final Object argument)
            {
                return new ProjectPermissionKey(permissionid).equals(argument);
            }
        };
    }

    private Matcher<ProjectPermissionKey> permissionKey(final ProjectPermissionKey permissionKey)
    {
        return new ArgumentMatcher<ProjectPermissionKey>()
        {

            @Override
            public boolean matches(final Object argument)
            {
                return permissionKey.equals(argument);
            }
        };
    }
}
