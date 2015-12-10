package com.atlassian.jira.bc.user.search;

import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests DefaultUserPickerSearchService without being a JiraMockTestCase.
 *
 * @see UserPickerSearchService
 */
public class TestDefaultUserPickerSearchService
{
    private JiraServiceContext jiraCtx;
    private MockApplicationProperties applicationProperties = new MockApplicationProperties();
    private MockPermissionManager permissionManager;

    @AvailableInContainer @Mock
    private PluginEventManager pluginEventManager;
    @AvailableInContainer @Mock
    private PluginAccessor pluginAccessor;
    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        permissionManager = new MockPermissionManager(true);
        jiraCtx = new MockJiraServiceContext();
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "show");
        when(authenticationContext.getLocale()).thenReturn(Locale.getDefault());
    }

    @Test
    public void testNullQuery()
    {
        DefaultUserPickerSearchService userPickerSearchService = new DefaultUserPickerSearchService(null, null, authenticationContext, null, null, null, null);

        final List<User> results = userPickerSearchService.findUsers(jiraCtx, null);
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testEmptyQuery()
    {
        DefaultUserPickerSearchService userPickerSearchService = new DefaultUserPickerSearchService(null, null, authenticationContext, null, null, null, null);

        final List<User> results = userPickerSearchService.findUsers(jiraCtx, "");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testWhitespaceOnlyQuery()
    {
        DefaultUserPickerSearchService userPickerSearchService = new DefaultUserPickerSearchService(null, null, authenticationContext, null, null, null, null);

        final List<User> results = userPickerSearchService.findUsers(jiraCtx, "\t ");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testEmptyResults()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("a", "Adam Smith", ""));
        userManager.addUser(new MockUser("b", "Bea Smith", ""));
        userManager.addUser(new MockUser("c", "Bea Hive", ""));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager, null, null, null);

        Collection results = searchService.findUsers(jiraCtx, "Adam");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "noob");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testMultipleMatches()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("a", "Adam Smith", ""));
        userManager.addUser(new MockUser("b", "Bea Smith", ""));
        userManager.addUser(new MockUser("c", "Bea Hive", ""));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager, null, null, null);

        Collection results = searchService.findUsers(jiraCtx, "Smith");
        assertNotNull(results);
        assertEquals(2, results.size());

        assertTrue(results.contains(new MockUser("a")));
        assertTrue(results.contains(new MockUser("b")));

        results = searchService.findUsers(jiraCtx, "Bea");
        assertNotNull(results);
        assertEquals(2, results.size());

        assertTrue(results.contains(new MockUser("b")));
        assertTrue(results.contains(new MockUser("c")));
    }

    @Test
    public void testEmailAddress()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("a", "Adam Smith", "asmith@example.com"));
        userManager.addUser(new MockUser("b", "Bea Smith", "bsmith@example.com"));
        userManager.addUser(new MockUser("c", "Bob Shiver", "bshiver@example.com"));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager, null, null, null);

        // First make email invisible
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "zwitix");
        Collection results = searchService.findUsers(jiraCtx, "as");
        assertEquals(0, results.size());

        // Now set email visible
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "show");
        results = searchService.findUsers(jiraCtx, "as");
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "asmith");
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "asmith@");
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "asmith@ex");
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "asmith@example.com");
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "@example.com");
        assertEquals(0, results.size());

        results = searchService.findUsers(jiraCtx, "bs");
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("b")));
        assertTrue(results.contains(new MockUser("c")));
    }

    @Test
    public void testUserName()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("zzz", "Adam Smith", "asmith@example.com"));
        userManager.addUser(new MockUser("zzw", "Bea Smith", "bsmith@example.com"));
        userManager.addUser(new MockUser("xxx", "Bob Shiver", "bshiver@example.com"));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager, null, null, null);

        // First make email invisible
        Collection results = searchService.findUsers(jiraCtx, "zz");
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("zzz")));
        assertTrue(results.contains(new MockUser("zzw")));
    }

    @Test
    public void testMixed()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("smithy", "Adam Smith", "asmith@example.com"));
        userManager.addUser(new MockUser("fff", "Bea Smith", "bsmith@example.com"));
        userManager.addUser(new MockUser("xxx", "Random Freak", "bshiver@example.com"));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager,null, null, null);

        // First make email invisible
        Collection results = searchService.findUsers(jiraCtx, "smith");
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("smithy")));
        assertTrue(results.contains(new MockUser("fff")));
    }

    @Test
    public void testUserSearchParamsEmptySearch()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("a", "Adam Smith", ""));
        userManager.addUser(new MockUser("b", "Bea Smith", ""));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager, null, null, null);

        UserSearchParams.Builder searchParamsBuilder = UserSearchParams.builder();

        // Default is active only, no empty search allowed
        Collection results = searchService.findUsers(jiraCtx, "", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(0, results.size());

        results = searchService.findUsers(jiraCtx, "Bea", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("b")));

        // Allow Empty
        searchParamsBuilder.allowEmptyQuery(true);
        results = searchService.findUsers(jiraCtx, "", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    public void testUserSearchParamsActive()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("a", "Adam Smith", ""));
        userManager.addUser(new MockUser("b", "Bea Smith", ""));
        final MockUser userC = new MockUser("c", "Bea Hive", "");
        userC.setActive(false);
        userManager.addUser(userC);

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager, null, null, null);

        UserSearchParams.Builder searchParamsBuilder = UserSearchParams.builder();

        // Default is active only, no empty search allowed
        Collection<User> results;
        results = searchService.findUsers(jiraCtx, "Bea", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("b")));

        // include Inactive
        searchParamsBuilder.includeInactive(true);
        results = searchService.findUsers(jiraCtx, "Bea", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("b")));
        assertTrue(results.contains(new MockUser("c")));

        // Inactive only
        searchParamsBuilder.includeActive(false);
        results = searchService.findUsers(jiraCtx, "Bea", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("c")));

        // include no-one
        searchParamsBuilder.includeInactive(false);
        results = searchService.findUsers(jiraCtx, "Bea", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testUserSearchParamsGroupFilters()
    {
        MockUserManager userManager = new MockUserManager();
        final MockUser userA = new MockUser("a", "Adam Smith", "mada@smith.com");
        final MockUser userB = new MockUser("b", "Bea Smith", "aeb@smith.com");
        final MockUser userC = new MockUser("c", "Bea Hive", "aeb@hive.com");
        userC.setActive(false);
        final MockUser userD = new MockUser("d", "Adam Hive", "mada@hive.com");
        userManager.addUser(userA);
        userManager.addUser(userB);
        userManager.addUser(userC);
        userManager.addUser(userD);

        MockGroupManager groupManager = new MockGroupManager();
        final MockGroup group1 = new MockGroup("group1");
        final MockGroup group2 = new MockGroup("group2");
        groupManager.addUserToGroup(userB, group1);
        groupManager.addUserToGroup(userC, group1);
        groupManager.addUserToGroup(userD, group1);
        groupManager.addUserToGroup(userA, group2);

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager, groupManager, null, null);

        Collection<User> results;
        // Default is active only, no empty search allowed
        results = searchService.findUsers(jiraCtx, "bea", UserSearchParams.builder().includeActive(true).includeInactive(false)
                .filter(new UserFilter(true, null, ImmutableSet.<String>of())).build());
        assertNotNull(results);
        assertEquals(0, results.size());

        // Default is active only, no empty search allowed
        results = searchService.findUsers(jiraCtx, "bea", UserSearchParams.builder().includeActive(true).includeInactive(false)
                .filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("b")));

        // include Inactive
        results = searchService.findUsers(jiraCtx, "Bea", UserSearchParams.builder().includeActive(true).includeInactive(true)
                .filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("b")));
        assertTrue(results.contains(new MockUser("c")));

        // Inactive only
        results = searchService.findUsers(jiraCtx, "Bea", UserSearchParams.builder().includeActive(false).includeInactive(true)
                .filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("c")));

        // include no-one
        results = searchService.findUsers(jiraCtx, "Bea", UserSearchParams.builder().includeActive(false).includeInactive(false)
                .filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testUserSearchParamsWithProjectRoleFilters() throws Exception
    {
        MockUserManager userManager = new MockUserManager();
        final MockUser userA = new MockUser("a", "Adam Smith", "mada@smith.com");
        final MockUser userB = new MockUser("b", "Bea Smith", "aeb@smith.com");
        final MockUser userC = new MockUser("c", "Bea Hive", "aeb@hive.com");
        userC.setActive(false);
        final MockUser userD = new MockUser("d", "Adam Hive", "mada@hive.com");
        userManager.addUser(userA);
        userManager.addUser(userB);
        userManager.addUser(userC);
        userManager.addUser(userD);

        final MockProjectManager projectManager = new MockProjectManager();
        final MockProject project1 = new MockProject(1L);
        final MockProject project2 = new MockProject(2L);
        projectManager.addProject(project1);
        projectManager.addProject(project2);

        ProjectRoleManager projectRoleManager = Mockito.mock(ProjectRoleManager.class);
        ProjectRole projectRole1 = Mockito.mock(ProjectRole.class);
        ProjectRole projectRole2 = Mockito.mock(ProjectRole.class);
        when(projectRoleManager.getProjectRole(1001L)).thenReturn(projectRole1);
        when(projectRoleManager.getProjectRole(1002L)).thenReturn(projectRole2);
        ProjectRoleActors projectRoleActors11 = Mockito.mock(ProjectRoleActors.class);
        when(projectRoleManager.getProjectRoleActors(projectRole1, project1)).thenReturn(projectRoleActors11);
        when(projectRoleActors11.getUsers()).thenReturn(ImmutableSet.<User>of(userA));
        ProjectRoleActors projectRoleActors21 = Mockito.mock(ProjectRoleActors.class);
        when(projectRoleManager.getProjectRoleActors(projectRole2, project1)).thenReturn(projectRoleActors21);
        when(projectRoleActors21.getUsers()).thenReturn(ImmutableSet.<User>of(userB));
        ProjectRoleActors projectRoleActors12 = Mockito.mock(ProjectRoleActors.class);
        when(projectRoleManager.getProjectRoleActors(projectRole1, project2)).thenReturn(projectRoleActors12);
        when(projectRoleActors12.getUsers()).thenReturn(ImmutableSet.<User>of(userC));
        ProjectRoleActors projectRoleActors22 = Mockito.mock(ProjectRoleActors.class);
        when(projectRoleManager.getProjectRoleActors(projectRole2, project2)).thenReturn(projectRoleActors22);
        when(projectRoleActors22.getUsers()).thenReturn(ImmutableSet.<User>of(userD));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager, null, projectManager, projectRoleManager);

        Collection<User> results;
        // Default is active only, no empty search allowed
        results = searchService.findUsers(jiraCtx, "bea", UserSearchParams.builder().includeActive(true).includeInactive(false)
                .filter(new UserFilter(true, ImmutableSet.<Long>of(), null)).build());
        assertNotNull(results);
        assertEquals(0, results.size());

        // Default is active only, no empty search allowed
        results = searchService.findUsers(jiraCtx, "Ada", UserSearchParams.builder().includeActive(true).includeInactive(false)
                .filter(new UserFilter(true, ImmutableSet.<Long>of(1001L), null)).filterByProjectIds(ImmutableSet.of(1L)).build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        // include Inactive
        results = searchService.findUsers(jiraCtx, "Hive", UserSearchParams.builder().includeActive(true).includeInactive(true)
                .filter(new UserFilter(true, ImmutableSet.<Long>of(1001L, 1002L), null)).filterByProjectIds(ImmutableSet.of(2L)).build());
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("c")));
        assertTrue(results.contains(new MockUser("d")));

        // Inactive only
        results = searchService.findUsers(jiraCtx, "Hive", UserSearchParams.builder().includeActive(false).includeInactive(true)
                .filter(new UserFilter(true, ImmutableSet.<Long>of(1001L, 1002L), null)).filterByProjectIds(ImmutableSet.of(2L)).build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("c")));

        // include no-one
        results = searchService.findUsers(jiraCtx, "Hive", UserSearchParams.builder().includeActive(true).includeInactive(true)
                .filter(new UserFilter(true, ImmutableSet.<Long>of(1001L, 1002L), null)).filterByProjectIds(ImmutableSet.of(1L)).build());
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testUserSearchParamsWithEmail()
    {
        MockUserManager userManager = new MockUserManager();
        final MockUser userA = new MockUser("a", "Adam Smith", "mada@smith.com");
        final MockUser userB = new MockUser("b", "Bea Smith", "aeb@smith.com");
        final MockUser userC = new MockUser("c", "Bea Hive", "aeb@hive.com");
        userC.setActive(false);
        final MockUser userD = new MockUser("d", "Adam Hive", "mada@hive.com");
        userManager.addUser(userA);
        userManager.addUser(userB);
        userManager.addUser(userC);
        userManager.addUser(userD);

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager, null, null, null);

        UserSearchParams.Builder searchParamsBuilder = UserSearchParams.builder();

        // Default is active only, no empty search allowed
        Collection<User> results;
        searchParamsBuilder.includeActive(true).includeInactive(false).canMatchEmail(false);
        results = searchService.findUsers("adam", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("a")));
        assertTrue(results.contains(new MockUser("d")));

        // Default is active only, no empty search allowed, search by email
        searchParamsBuilder.includeActive(true).includeInactive(false).canMatchEmail(true);
        results = searchService.findUsers("mada", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("a")));
        assertTrue(results.contains(new MockUser("d")));

        // Default is active only, no empty search allowed, search by email disabled
        searchParamsBuilder.includeActive(true).includeInactive(false).canMatchEmail(false);
        results = searchService.findUsers("mada", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(0, results.size());

        // Default is active only, no empty search allowed, search by email separately
        searchParamsBuilder.includeActive(true).includeInactive(false).canMatchEmail(true);
        results = searchService.findUsers("", "mada", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("a")));
        assertTrue(results.contains(new MockUser("d")));

        // Default is active only, no empty search allowed, search by username and email separately
        searchParamsBuilder.includeActive(true).includeInactive(false).canMatchEmail(true);
        results = searchService.findUsers("adam", "mada", searchParamsBuilder.build());
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("a")));
        assertTrue(results.contains(new MockUser("d")));
    }

    @Test
    public void testUserSearchParamsWithEmailWithGroupFilters()
    {
        MockUserManager userManager = new MockUserManager();
        final MockUser userA = new MockUser("a", "Adam Smith", "mada@smith.com");
        final MockUser userB = new MockUser("b", "Bea Smith", "aeb@smith.com");
        final MockUser userC = new MockUser("c", "Bea Hive", "aeb@hive.com");
        userC.setActive(false);
        final MockUser userD = new MockUser("d", "Adam Hive", "mada@hive.com");
        userManager.addUser(userA);
        userManager.addUser(userB);
        userManager.addUser(userC);
        userManager.addUser(userD);

        MockGroupManager groupManager = new MockGroupManager();
        final MockGroup group1 = new MockGroup("group1");
        final MockGroup group2 = new MockGroup("group2");
        groupManager.addUserToGroup(userB, group1);
        groupManager.addUserToGroup(userC, group1);
        groupManager.addUserToGroup(userD, group1);
        groupManager.addUserToGroup(userA, group2);

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, authenticationContext, permissionManager, groupManager, null, null);

        UserSearchParams.Builder searchParamsBuilder = UserSearchParams.builder();

        // Default is active only, no empty search allowed
        Collection<User> results;
        results = searchService.findUsers("bea", UserSearchParams.builder().includeActive(true).includeInactive(false).canMatchEmail(false)
                .filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("b")));

        // Default is active only, no empty search allowed, search by email
        results = searchService.findUsers("aeb", UserSearchParams.builder().includeActive(true).includeInactive(false).canMatchEmail(true)
                .filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("b")));

        // Default is active only, no empty search allowed, search by email disabled
        results = searchService.findUsers("aeb", UserSearchParams.builder().includeActive(true).includeInactive(false).canMatchEmail(false)
                .filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertNotNull(results);
        assertEquals(0, results.size());

        // Default is active only, no empty search allowed, search by email separately
        results = searchService.findUsers("", "aeb", UserSearchParams.builder().includeActive(true).includeInactive(false).canMatchEmail(true)
                .filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("b")));

        // Default is active only, no empty search allowed, search by username and email separately
        results = searchService.findUsers("bea", "aeb", UserSearchParams.builder().includeActive(true).includeInactive(false).canMatchEmail(true)
                .filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("b")));
    }

    @Test
    public void testCanPerformAjaxSearch() throws Exception
    {
        ApplicationUser user = new MockApplicationUser("sarah");
        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(null, applicationProperties, authenticationContext, permissionManager, null, null, null);

        permissionManager.setDefaultPermission(false);
        assertFalse(searchService.canPerformAjaxSearch(user.getDirectoryUser()));
        assertFalse(searchService.canPerformAjaxSearch(new JiraServiceContextImpl(user)));
        permissionManager.setDefaultPermission(true);
        assertTrue(searchService.canPerformAjaxSearch(user.getDirectoryUser()));
        assertTrue(searchService.canPerformAjaxSearch(new JiraServiceContextImpl(user)));
    }

    @Test
    public void testCanShowEmailAddressesWhenSHOW()
    {
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(null, applicationProperties, authenticationContext, permissionManager, null, null, null);
        // show
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "show");
        assertTrue(searchService.canShowEmailAddresses(jiraCtx));
        // mask
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "mask");
        assertTrue(searchService.canShowEmailAddresses(jiraCtx));
        // hidden
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "hidden");
        assertFalse(searchService.canShowEmailAddresses(jiraCtx));
        // user
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "user");
        ApplicationUser user = null;
        assertFalse(searchService.canShowEmailAddresses(new JiraServiceContextImpl(user)));
        user = new MockApplicationUser("gimley");
        assertTrue(searchService.canShowEmailAddresses(new JiraServiceContextImpl(user)));
        // junk
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "asdfasd");
        assertFalse(searchService.canShowEmailAddresses(jiraCtx));
    }
}
