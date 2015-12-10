package com.atlassian.jira.user.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.MockEventPublisher;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.security.DefaultGlobalPermissionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.MockGlobalPermissionTypeManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.security.plugin.GlobalPermissionTypesManager;
import com.atlassian.jira.studio.MockStudioHooks;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockCrowdDirectoryService;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.CollectionAssert;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.seraph.spi.rememberme.RememberMeTokenDao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * TestUserUtilImpl without being a JiraMockTestCase
 *
 * @since v4.3
 */
public class TestUserUtilImpl
{
    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private JiraLicenseService jiraLicenseService;

    @Mock
    private LicenseDetails licenseDetails;

    @Mock
    @AvailableInContainer
    private EventPublisher eventPublisher;

    @Mock @AvailableInContainer
    private GlobalPermissionManager globalPermissionManager;

    @Mock
    private UserManager userManager;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    @Mock
    @AvailableInContainer
    private RememberMeTokenDao rememberMeTokenDao;

    @Mock
    @AvailableInContainer
    private LoginManager loginManager;

    private GlobalPermissionTypesManager globalPermissionTypesManager = new MockGlobalPermissionTypeManager();

    @Mock
    private UserUtil userUtil;

    private CacheManager cacheManager;

    @Before
    public void setup()
    {
        cacheManager = new MemoryCacheManager();
        when(jiraLicenseService.getLicense()).thenReturn(licenseDetails);
        when(licenseDetails.isLicenseSet()).thenReturn(true);
        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(true);
        when(applicationProperties.getString(APKeys.JIRA_BASEURL)).thenReturn("http://localhost");
        when(globalPermissionManager.getAllGlobalPermissions()).thenReturn(globalPermissionTypesManager.getAll());
    }

    @Test
    public void testGetAllUsersInGroupNamesEmpty() throws Exception
    {
        UserUtilImpl userUtil = new UserUtilImpl(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks(), cacheManager);
        try
        {
            userUtil.getAllUsersInGroupNames(null);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            // good.
        }

        SortedSet<User> users = userUtil.getAllUsersInGroupNames(new ArrayList<String>());
        assertTrue(users.isEmpty());
    }

    @Test
    public void testGetAllUsersInGroupNames() throws Exception
    {
        // Set up users and groups.
        MockCrowdService crowdService = new MockCrowdService();
        crowdService.addUser(new MockUser("ZAC"), "");
        final User userAdam = new MockUser("adam");
        final Group groupAnts = new MockGroup("ants");
        crowdService.addUser(userAdam, "");
        crowdService.addGroup(groupAnts);
        crowdService.addUserToGroup(userAdam, groupAnts);
        final User userBetty = new MockUser("betty");
        final User userBertie = new MockUser("bertie");
        final Group groupBeetles = new MockGroup("beetles");
        crowdService.addUser(userBetty, "");
        crowdService.addUser(userBertie, "");
        crowdService.addGroup(groupBeetles);
        crowdService.addUserToGroup(userBetty, groupBeetles);
        crowdService.addUserToGroup(userBertie, groupBeetles);

        // Create my UserUtilImpl
        UserUtilImpl userUtil = new UserUtilImpl(null, null, crowdService, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks(), cacheManager);

        // Ants
        Collection<String> groupNames = CollectionBuilder.list("ants");
        SortedSet<User> users = userUtil.getAllUsersInGroupNames(groupNames);
        assertEquals(1, users.size());
        assertTrue(users.contains(userAdam));

        // Beetles
        groupNames = CollectionBuilder.list("beetles");
        users = userUtil.getAllUsersInGroupNames(groupNames);
        assertEquals(2, users.size());
        assertTrue(users.contains(userBertie));
        assertTrue(users.contains(userBetty));

        // Ants and Beetles
        groupNames = CollectionBuilder.list("ants", "beetles");
        users = userUtil.getAllUsersInGroupNames(groupNames);
        assertEquals(3, users.size());
        assertTrue(users.contains(userAdam));
        assertTrue(users.contains(userBertie));
        assertTrue(users.contains(userBetty));
    }

    @Test
    public void testGetAllUsersInGroupsEmpty() throws Exception
    {
        UserUtilImpl userUtil = new UserUtilImpl(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks(), cacheManager);
        try
        {
            userUtil.getAllUsersInGroups(null);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            // good.
        }

        SortedSet<User> users = userUtil.getAllUsersInGroups(new ArrayList<Group>());
        assertTrue(users.isEmpty());
    }

    @Test
    public void testGetAllUsersInGroups() throws Exception
    {
        // Set up users and groups.
        MockCrowdService crowdService = new MockCrowdService();
        crowdService.addUser(new MockUser("ZAC"), "");
        final User userAdam = new MockUser("adam");
        final Group groupAnts = new MockGroup("ants");
        crowdService.addUser(userAdam, "");
        crowdService.addGroup(groupAnts);
        crowdService.addUserToGroup(userAdam, groupAnts);
        final User userBetty = new MockUser("betty");
        final User userBertie = new MockUser("bertie");
        final Group groupBeetles = new MockGroup("beetles");
        crowdService.addUser(userBetty, "");
        crowdService.addUser(userBertie, "");
        crowdService.addGroup(groupBeetles);
        crowdService.addUserToGroup(userBetty, groupBeetles);
        crowdService.addUserToGroup(userBertie, groupBeetles);

        // Create my UserUtilImpl
        UserUtilImpl userUtil = new UserUtilImpl(null, null, crowdService, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks(), cacheManager);

        // Ants
        Collection<Group> groups = CollectionBuilder.<Group>list(new MockGroup("ants"));
        SortedSet<User> users = userUtil.getAllUsersInGroups(groups);
        assertEquals(1, users.size());
        assertTrue(users.contains(userAdam));

        // Beetles
        groups = CollectionBuilder.<Group>list(new MockGroup("beetles"));
        users = userUtil.getAllUsersInGroups(groups);
        assertEquals(2, users.size());
        assertTrue(users.contains(userBertie));
        assertTrue(users.contains(userBetty));

        // Ants and Beetles
        groups = CollectionBuilder.<Group>list(new MockGroup("ants"), new MockGroup("beetles"));
        users = userUtil.getAllUsersInGroups(groups);
        assertEquals(3, users.size());
        assertTrue(users.contains(userAdam));
        assertTrue(users.contains(userBertie));
        assertTrue(users.contains(userBetty));
    }

    @Test
    public void testCreateUserNoNotificationDispatchedUserCreatedEvent() throws Exception
    {
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, new MockCrowdService(), null, null, null, null, null, null,
                null, null, null, null, userManager, eventPublisher, new MockStudioHooks(), cacheManager);

        userUtil.createUserNoNotification("username", "password", "emailAddress", "displayName");

        ArgumentCaptor<UserEvent> captureUserEvent = ArgumentCaptor.forClass(UserEvent.class);
        verify(eventPublisher).publish(captureUserEvent.capture());
        UserEvent userEvent = captureUserEvent.getValue();
        assertEquals("username", userEvent.getParams().get(UserUtilImpl.USERNAME));
        assertNull(userEvent.getParams().get(UserUtilImpl.SEND_EMAIL));
    }

    @Test
    public void testCreateUserWithNotificationDispatchedUserCreatedEvent() throws Exception
    {
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, new MockCrowdService(), null, null, null, null, null, null,
                null, null, null, null, userManager, eventPublisher, new MockStudioHooks(), cacheManager);

        userUtil.createUserWithNotification("username", "password", "emailAddress", "displayName", UserEventType.USER_SIGNUP);

        ArgumentCaptor<UserEvent> captureUserEvent = ArgumentCaptor.forClass(UserEvent.class);
        verify(eventPublisher).publish(captureUserEvent.capture());
        UserEvent userEvent = captureUserEvent.getValue();
        assertEquals("username", userEvent.getParams().get(UserUtilImpl.USERNAME));
        assertEquals(true, userEvent.getParams().get(UserUtilImpl.SEND_EMAIL));
        assertEquals(UserEventType.USER_SIGNUP, userEvent.getEventType());
    }

    @Test
    public void testAddUserToGroup() throws Exception
    {
        final MockCrowdService mockCrowdService = new MockCrowdService();
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, null, null, new MockStudioHooks(), cacheManager);
        final Group group = new MockGroup("goodies");
        mockCrowdService.addGroup(new MockGroup("goodies"));
        final User user = new MockUser("bill");
        mockCrowdService.addUser(user, null);

        // Pre condition
        assertThat(userUtil.getGroupNamesForUser("bill"), not(hasItem("goodies")));
        userUtil.addUserToGroup(group, user);
        // post condition
        assertThat(userUtil.getGroupNamesForUser("bill"), hasItem("goodies"));
    }

    @Test
    public void testRemoveUserFromGroup() throws Exception
    {
        final MockCrowdService mockCrowdService = new MockCrowdService();
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, null, null, new MockStudioHooks(), cacheManager);
        final Group group = new MockGroup("goodies");
        mockCrowdService.addGroup(new MockGroup("goodies"));
        final User user = new MockUser("bill");
        mockCrowdService.addUser(user, null);
        mockCrowdService.addUserToGroup(user, group);

        // Pre condition
        assertThat(userUtil.getGroupNamesForUser("bill"), hasItem("goodies"));
        userUtil.removeUserFromGroup(group, user);
        // post condition
        assertThat(userUtil.getGroupNamesForUser("bill"), not(hasItem("goodies")));
    }

    @Test
    public void testAddUserToGroups() throws Exception
    {
        final MockCrowdService mockCrowdService = new MockCrowdService();
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, null, null, new MockStudioHooks(), cacheManager);
        final Group group1 = new MockGroup("goodies");
        mockCrowdService.addGroup(group1);
        final Group group2 = new MockGroup("baddies");
        mockCrowdService.addGroup(group2);
        final User user = new MockUser("bill");
        mockCrowdService.addUser(user, null);

        // Pre condition
        assertThat(userUtil.getGroupNamesForUser("bill"), not(hasItem("goodies")));
        assertThat(userUtil.getGroupNamesForUser("bill"), not(hasItem("baddies")));
        userUtil.addUserToGroups(Arrays.asList(group1, group2), user);
        // post condition
        assertThat(userUtil.getGroupNamesForUser("bill"), hasItem("goodies"));
        assertThat(userUtil.getGroupNamesForUser("bill"), hasItem("baddies"));
    }

    @Test
    public void testRemoveUserFromGroups() throws Exception
    {
        final MockCrowdService mockCrowdService = new MockCrowdService();
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, null, null, new MockStudioHooks(), cacheManager);
        final Group group1 = new MockGroup("goodies");
        mockCrowdService.addGroup(group1);
        final Group group2 = new MockGroup("baddies");
        mockCrowdService.addGroup(group2);
        final Group group3 = new MockGroup("others");
        mockCrowdService.addGroup(group3);
        final User user = new MockUser("bill");
        mockCrowdService.addUser(user, null);
        userUtil.addUserToGroups(Arrays.asList(group1, group2, group3), user);

        // Pre condition
        assertThat(userUtil.getGroupNamesForUser("bill"), hasItem("goodies"));
        assertThat(userUtil.getGroupNamesForUser("bill"), hasItem("baddies"));
        assertThat(userUtil.getGroupNamesForUser("bill"), hasItem("others"));
        userUtil.removeUserFromGroups(Arrays.asList(group1, group2), user);
        // post condition
        assertThat(userUtil.getGroupNamesForUser("bill"), not(hasItem("goodies")));
        assertThat(userUtil.getGroupNamesForUser("bill"), not(hasItem("baddies")));
        assertThat(userUtil.getGroupNamesForUser("bill"), hasItem("others"));
    }

    @Test
    public void testGetActiveUserCount() throws Exception
    {
        final MockCrowdService mockCrowdService = new MockCrowdService();
        final GlobalPermissionManager globalPermissionManager = new DefaultGlobalPermissionManager(mockCrowdService, new MockOfBizDelegator(), new MockEventPublisher(), globalPermissionTypesManager, new MemoryCacheManager());
        final UserUtil userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null,
                null, null, null, null, null, null, null, null, new MockStudioHooks(), cacheManager);
        new MockComponentWorker().init()
                .addMock(UserUtil.class, userUtil)
                .addMock(GlobalPermissionManager.class, globalPermissionManager);

        final Group usersGroup = new MockGroup("users");
        mockCrowdService.addGroup(new MockGroup("users"));
        final User user1 = new MockUser("bill");
        mockCrowdService.addUser(user1, null);
        mockCrowdService.addUserToGroup(user1, usersGroup);
        globalPermissionManager.addPermission(Permissions.USE, "users");

        assertEquals(1, userUtil.getActiveUserCount());

        // add a second user
        final User user2 = new MockUser("ted");
        mockCrowdService.addUser(user2, null);
        mockCrowdService.addUserToGroup(user2, usersGroup);

        // Old value is cached
        assertEquals(1, userUtil.getActiveUserCount());
        userUtil.clearActiveUserCount();
        assertEquals(2, userUtil.getActiveUserCount());
    }

    @Test
    public void testGetActiveUserCountSameUserInMultipleGroups() throws Exception
    {
        final MockCrowdService mockCrowdService = new MockCrowdService();
        final GlobalPermissionManager globalPermissionManager = new DefaultGlobalPermissionManager(mockCrowdService, new MockOfBizDelegator(), new MockEventPublisher(), globalPermissionTypesManager, new MemoryCacheManager());
        final UserUtil userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null,
                null, null, null, null, null, null, null, null, new MockStudioHooks(), cacheManager);
        new MockComponentWorker().init()
                .addMock(UserUtil.class, userUtil)
                .addMock(GlobalPermissionManager.class, globalPermissionManager);

        final Group usersGroup1 = new MockGroup("users1");
        mockCrowdService.addGroup(usersGroup1);
        final Group usersGroup2 = new MockGroup("users2");
        mockCrowdService.addGroup(usersGroup2);
        final User user1 = new MockUser("bill");
        mockCrowdService.addUser(user1, null);
        final User user2 = new MockUser("ted");
        mockCrowdService.addUser(user2, null);
        mockCrowdService.addUserToGroup(user1, usersGroup1);
        mockCrowdService.addUserToGroup(user1, usersGroup2);
        mockCrowdService.addUserToGroup(user2, usersGroup2);
        globalPermissionManager.addPermission(Permissions.USE, "users1");

        assertEquals(1, userUtil.getActiveUserCount());

        // Now make group 2 have login permission as well
        globalPermissionManager.addPermission(Permissions.USE, "users2");

        // Old value is cached
        userUtil.clearActiveUserCount();
        assertEquals(2, userUtil.getActiveUserCount());
    }

    @Test
    public void testGetActiveUserCountGroupsInMultiplePermissions() throws Exception
    {
        final MockCrowdService mockCrowdService = new MockCrowdService();
        final GlobalPermissionManager globalPermissionManager = new DefaultGlobalPermissionManager(mockCrowdService, new MockOfBizDelegator(), new MockEventPublisher(), globalPermissionTypesManager, new MemoryCacheManager());
        final UserUtil userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null,
                null, null, null, null, null, null, null, null, new MockStudioHooks(), cacheManager);
        new MockComponentWorker().init()
                .addMock(UserUtil.class, userUtil)
                .addMock(GlobalPermissionManager.class, globalPermissionManager);

        final Group usersGroup1 = new MockGroup("users1");
        mockCrowdService.addGroup(usersGroup1);
        final Group usersGroup2 = new MockGroup("admins");
        mockCrowdService.addGroup(usersGroup2);
        final User user1 = new MockUser("bill");
        mockCrowdService.addUser(user1, null);
        final User user2 = new MockUser("ted");
        mockCrowdService.addUser(user2, null);
        mockCrowdService.addUserToGroup(user1, usersGroup1);
        mockCrowdService.addUserToGroup(user2, usersGroup2);
        globalPermissionManager.addPermission(Permissions.ADMINISTER, "admins");

        assertEquals(1, userUtil.getActiveUserCount());
        globalPermissionManager.addPermission(Permissions.USE, "users1");
        globalPermissionManager.addPermission(Permissions.USE, "admins");
        // add a rubbish group as well
        globalPermissionManager.addPermission(Permissions.USE, "invalid");

        userUtil.clearActiveUserCount();
        assertEquals(2, userUtil.getActiveUserCount());
    }

    @Test
    public void testHasExceededUserLimit() throws Exception
    {
        UserUtilImpl userUtil = new UserUtilImpl(null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, new MockStudioHooks(), cacheManager)
        {
            @Override
            public int getActiveUserCount()
            {
                return 10;
            }
        };

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(true);
        assertFalse(userUtil.hasExceededUserLimit());

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(false);
        when(licenseDetails.getMaximumNumberOfUsers()).thenReturn(5);
        assertTrue(userUtil.hasExceededUserLimit());

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(false);
        when(licenseDetails.getMaximumNumberOfUsers()).thenReturn(10);
        assertFalse(userUtil.hasExceededUserLimit());

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(false);
        when(licenseDetails.getMaximumNumberOfUsers()).thenReturn(9);
        assertTrue(userUtil.hasExceededUserLimit());
    }

    @Test
    public void testCanActivateNumberOfUsers() throws Exception
    {
        UserUtilImpl userUtil = new UserUtilImpl(null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, new MockStudioHooks(), cacheManager)
        {
            @Override
            public int getActiveUserCount()
            {
                return 10;
            }
        };

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(true);
        assertTrue(userUtil.canActivateNumberOfUsers(3000000));

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(false);
        when(licenseDetails.getMaximumNumberOfUsers()).thenReturn(5);
        assertFalse(userUtil.canActivateNumberOfUsers(1));

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(false);
        when(licenseDetails.getMaximumNumberOfUsers()).thenReturn(12);
        assertTrue(userUtil.canActivateNumberOfUsers(2));
        assertFalse(userUtil.canActivateNumberOfUsers(3));

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(false);
        when(licenseDetails.getMaximumNumberOfUsers()).thenReturn(10);
        assertTrue(userUtil.canActivateNumberOfUsers(0));
        assertFalse(userUtil.canActivateNumberOfUsers(1));
    }

    @Test
    public void testCanActivateUsers() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        UserUtilImpl userUtil = new UserUtilImpl(null, null, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, null, null, new MockStudioHooks(), cacheManager)
        {
            @Override
            public int getActiveUserCount()
            {
                return 10;
            }

            @Override
            public Set<String> getGroupsWithUsePermission()
            {
                return ImmutableSet.of("nobs", "knockers");
            }
        };

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(false);
        when(licenseDetails.getMaximumNumberOfUsers()).thenReturn(20);
        assertTrue(userUtil.canActivateUsers(Arrays.asList("dick", "jane", "bob")));

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(false);
        when(licenseDetails.getMaximumNumberOfUsers()).thenReturn(12);
        assertFalse(userUtil.canActivateUsers(Arrays.asList("dick", "jane", "bob")));

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(false);
        when(licenseDetails.getMaximumNumberOfUsers()).thenReturn(12);
        // Make one user already a member of a group with use permission - now we only need 2 more licenses
        mockCrowdService.addUser(new MockUser("dick"), null);
        mockCrowdService.addGroup(new MockGroup("knockers"));
        mockCrowdService.addUserToGroup(new MockUser("dick"), new MockGroup("knockers"));
        assertTrue(userUtil.canActivateUsers(Arrays.asList("dick", "jane", "bob")));

        // What about if we have already exceeded the license?
        when(licenseDetails.getMaximumNumberOfUsers()).thenReturn(5);
        assertFalse(userUtil.canActivateUsers(Arrays.asList("jane")));
        // dick is already activated
        assertTrue(userUtil.canActivateUsers(Arrays.asList("dick")));
    }

    @Test
    public void addToJiraUsePermission_WhenAllowed() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        final GlobalPermissionManager globalPermissionManager = new DefaultGlobalPermissionManager(mockCrowdService, new MockOfBizDelegator(), new MockEventPublisher(), globalPermissionTypesManager, new MemoryCacheManager());
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, null, null, new MockStudioHooks(), cacheManager)
        {
            @Override
            public boolean canActivateNumberOfUsers(int i)
            {
                return true;
            }
        };
        new MockComponentWorker().init()
                .addMock(UserUtil.class, userUtil)
                .addMock(GlobalPermissionManager.class, globalPermissionManager);

        final Group usersGroup1 = new MockGroup("group1");
        mockCrowdService.addGroup(usersGroup1);
        final Group usersGroup2 = new MockGroup("group2");
        mockCrowdService.addGroup(usersGroup2);
        final Group usersGroup3 = new MockGroup("group3");
        mockCrowdService.addGroup(usersGroup3);
        final User user1 = new MockUser("bill");
        mockCrowdService.addUser(user1, null);
        globalPermissionManager.addPermission(Permissions.USE, "group1");
        globalPermissionManager.addPermission(Permissions.USE, "group3");

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(true);
        userUtil.addToJiraUsePermission(user1);

        // assert they now belong to groups 1 and 3 but not 2
        assertTrue(mockCrowdService.isUserMemberOfGroup(user1, usersGroup1));
        assertFalse(mockCrowdService.isUserMemberOfGroup(user1, usersGroup2));
        assertTrue(mockCrowdService.isUserMemberOfGroup(user1, usersGroup3));
    }

    @Test
    public void addToJiraUsePermission_WhenNotAllowed() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        final GlobalPermissionManager globalPermissionManager = new DefaultGlobalPermissionManager(mockCrowdService, new MockOfBizDelegator(), new MockEventPublisher(), globalPermissionTypesManager, new MemoryCacheManager());
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, null, null, new MockStudioHooks(), cacheManager)
        {
            @Override
            public boolean canActivateNumberOfUsers(int i)
            {
                return false;
            }
        };
        new MockComponentWorker().init()
                .addMock(UserUtil.class, userUtil)
                .addMock(GlobalPermissionManager.class, globalPermissionManager);

        final Group usersGroup1 = new MockGroup("group1");
        mockCrowdService.addGroup(usersGroup1);
        final Group usersGroup2 = new MockGroup("group2");
        mockCrowdService.addGroup(usersGroup2);
        final Group usersGroup3 = new MockGroup("group3");
        mockCrowdService.addGroup(usersGroup3);
        final User user1 = new MockUser("bill");
        mockCrowdService.addUser(user1, null);
        globalPermissionManager.addPermission(Permissions.USE, "group1");
        globalPermissionManager.addPermission(Permissions.USE, "group3");

        when(licenseDetails.isUnlimitedNumberOfUsers()).thenReturn(true);
        userUtil.addToJiraUsePermission(user1);

        // assert they now belong to no groups
        assertFalse(mockCrowdService.isUserMemberOfGroup(user1, usersGroup1));
        assertFalse(mockCrowdService.isUserMemberOfGroup(user1, usersGroup2));
        assertFalse(mockCrowdService.isUserMemberOfGroup(user1, usersGroup3));
    }

    @Test
    /**
     * Pretty pointless test, but I copied this over from the legacy test.
     */
    public void getUser() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        UserManager userManager = new DefaultUserManager(mockCrowdService, null, null, new MockUserKeyStore(), null, null);
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, userManager, null, new MockStudioHooks(), cacheManager);
        mockCrowdService.addUser(new MockUser("bill", "Billy Bob", ""), null);

        assertNull(userUtil.getUser(null));
        assertNull(userUtil.getUserByKey(null));
        assertNull(userUtil.getUserByName(null));

        assertNull(userUtil.getUser("asdfa"));
        assertNull(userUtil.getUserByKey("asdfa"));
        assertNull(userUtil.getUserByName("asdfa"));

        assertEquals("Billy Bob", userUtil.getUser("bill").getDisplayName());
        assertEquals("Billy Bob", userUtil.getUserByKey("bill").getDisplayName());
        assertEquals("Billy Bob", userUtil.getUserByName("bill").getDisplayName());
    }


    @Test
    public void getAdmins() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        final GlobalPermissionManager globalPermissionManager = new DefaultGlobalPermissionManager(mockCrowdService, new MockOfBizDelegator(), new MockEventPublisher(), globalPermissionTypesManager, new MemoryCacheManager());
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, null, null, new MockStudioHooks(), cacheManager);
        new MockComponentWorker().init()
                .addMock(UserUtil.class, userUtil)
                .addMock(GlobalPermissionManager.class, globalPermissionManager);

        final Group group1 = new MockGroup("group1");
        mockCrowdService.addGroup(group1);
        final Group group2 = new MockGroup("group2");
        mockCrowdService.addGroup(group2);
        final User user1 = new MockUser("bill");
        mockCrowdService.addUser(user1, null);
        mockCrowdService.addUserToGroup(user1, group1);
        final User user2 = new MockUser("ted");
        mockCrowdService.addUser(user2, null);
        mockCrowdService.addUserToGroup(user2, group2);
        globalPermissionManager.addPermission(Permissions.ADMINISTER, "group1");
        globalPermissionManager.addPermission(Permissions.SYSTEM_ADMIN, "group2");

        Collection<User> admins = userUtil.getAdministrators();
        assertEquals(1, admins.size());
        assertThat(admins, Matchers.hasItems(user1));
        Collection<User> sysAdmins = userUtil.getSystemAdministrators();
        assertEquals(1, sysAdmins.size());
        assertThat(sysAdmins, Matchers.hasItems(user2));
    }

    @Test
    public void createUserAndTestPassword() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        UserManager userManager = new DefaultUserManager(mockCrowdService, new MockCrowdDirectoryService(), null, new MockUserKeyStore(), null, null);
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, userManager, null, new MockStudioHooks(), cacheManager);

        userUtil.createUserNoNotification("bill", "asdfasdf", "bill@example.com", "Billy Jean");

        User authenticatedUser = mockCrowdService.authenticate("bill", "asdfasdf");
        assertEquals("Billy Jean", authenticatedUser.getDisplayName());

        try
        {
            mockCrowdService.authenticate("bill", "xxx");
            fail();
        }
        catch (FailedAuthenticationException ex)
        {
            // expected
        }
    }

    @Test
    public void changePassword() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        UserManager userManager = new DefaultUserManager(mockCrowdService, new MockCrowdDirectoryService(), null, new MockUserKeyStore(), null, null);
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, userManager, null, new MockStudioHooks(), cacheManager);

        userUtil.createUserNoNotification("bill", "asdfasdf", "bill@example.com", "Billy Jean");

        User authenticatedUser = mockCrowdService.authenticate("bill", "asdfasdf");
        assertEquals("Billy Jean", authenticatedUser.getDisplayName());

        try
        {
            mockCrowdService.authenticate("bill", "xxx");
            fail();
        }
        catch (FailedAuthenticationException ex)
        {
            // expected
        }

        userUtil.changePassword(new MockUser("bill"), "xxx");
        authenticatedUser = mockCrowdService.authenticate("bill", "xxx");
        assertEquals("Billy Jean", authenticatedUser.getDisplayName());
    }

    @Test
    public void createUserWithBlankPassword() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        UserManager userManager = new DefaultUserManager(mockCrowdService, new MockCrowdDirectoryService(), null, new MockUserKeyStore(), null, null);
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, userManager, null, new MockStudioHooks(), cacheManager);

        userUtil.createUserNoNotification("bill", "", "bill@example.com", "Billy Jean");

        try
        {
            mockCrowdService.authenticate("bill", "");
            fail("Blank password should have been set to random password ot not allow login");
        }
        catch (FailedAuthenticationException ex)
        {
            // expected
        }
    }

    @Test
    public void testGetDisplayableNameSafely()
    {
        UserUtilImpl userUtil = new UserUtilImpl(null, globalPermissionManager, null, null, null, null, null, null, null,
                null, null, null, null, userManager, null, new MockStudioHooks(), cacheManager);

        assertNull(userUtil.getDisplayableNameSafely((User) null));
        assertEquals("bob", userUtil.getDisplayableNameSafely(new MockUser("bob", null, null)));
        assertEquals("bob", userUtil.getDisplayableNameSafely(new MockUser("bob", "", "")));
        assertEquals("Bob Belcher", userUtil.getDisplayableNameSafely(new MockUser("bob", "Bob Belcher", "")));

        assertNull(userUtil.getDisplayableNameSafely((ApplicationUser) null));
        assertEquals("bob", userUtil.getDisplayableNameSafely(new MockApplicationUser("bob", null, null)));
        assertEquals("bob", userUtil.getDisplayableNameSafely(new MockApplicationUser("bob", "", "")));
        assertEquals("Bob Belcher", userUtil.getDisplayableNameSafely(new MockApplicationUser("bob", "Bob Belcher", "")));
    }

    @Test
    public void getUsersInGroupNames() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        UserUtilImpl userUtil = new UserUtilImpl(null, null, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, userManager, null, new MockStudioHooks(), cacheManager);

        final Group group1 = new MockGroup("group1");
        mockCrowdService.addGroup(group1);
        final Group group2 = new MockGroup("group2");
        mockCrowdService.addGroup(group2);
        final Group group3 = new MockGroup("group3");
        mockCrowdService.addGroup(group3);
        final User user1 = new MockUser("bill");
        mockCrowdService.addUser(user1, null);
        mockCrowdService.addUserToGroup(user1, group1);
        mockCrowdService.addUserToGroup(user1, group2);
        final User user2 = new MockUser("ted");
        mockCrowdService.addUser(user2, null);
        mockCrowdService.addUserToGroup(user2, group2);
        final User user3 = new MockUser("edna");
        mockCrowdService.addUser(user3, null);

        CollectionAssert.assertContainsExactly(
                Arrays.asList(user1),
                userUtil.getUsersInGroupNames(Arrays.asList("group1")));
        CollectionAssert.assertContainsExactly(
                Arrays.asList(user1, user2),
                userUtil.getUsersInGroupNames(Arrays.asList("group2")));
        CollectionAssert.assertContainsExactly(
                Arrays.asList(user1, user2),
                userUtil.getUsersInGroupNames(Arrays.asList("group1", "group2")));
        CollectionAssert.assertContainsExactly(
                Arrays.asList(),
                userUtil.getUsersInGroupNames(Arrays.asList("rubbish")));
        CollectionAssert.assertContainsExactly(
                Arrays.asList(user1),
                userUtil.getUsersInGroupNames(Arrays.asList("group1", "rubbish")));
    }

    @Test
    public void getUsersInGroups() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        UserUtilImpl userUtil = new UserUtilImpl(null, null, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, userManager, null, new MockStudioHooks(), cacheManager);

        final Group group1 = new MockGroup("group1");
        mockCrowdService.addGroup(group1);
        final Group group2 = new MockGroup("group2");
        mockCrowdService.addGroup(group2);
        final Group group3 = new MockGroup("group3");
        mockCrowdService.addGroup(group3);
        final User user1 = new MockUser("bill");
        mockCrowdService.addUser(user1, null);
        mockCrowdService.addUserToGroup(user1, group1);
        mockCrowdService.addUserToGroup(user1, group2);
        final User user2 = new MockUser("ted");
        mockCrowdService.addUser(user2, null);
        mockCrowdService.addUserToGroup(user2, group2);
        final User user3 = new MockUser("edna");
        mockCrowdService.addUser(user3, null);

        CollectionAssert.assertContainsExactly(
                Arrays.asList(user1),
                userUtil.getUsersInGroups(Arrays.asList(group1)));
        CollectionAssert.assertContainsExactly(
                Arrays.asList(user1, user2),
                userUtil.getUsersInGroups(Arrays.asList(group2)));
        CollectionAssert.assertContainsExactly(
                Arrays.asList(user1, user2),
                userUtil.getUsersInGroups(Arrays.asList(group1, group2)));
        CollectionAssert.assertContainsExactly(
                Arrays.asList(),
                userUtil.getUsersInGroups(Arrays.<Group>asList(new MockGroup("xxxx"))));
        CollectionAssert.assertContainsExactly(
                Arrays.asList(user1),
                userUtil.getUsersInGroups(Arrays.asList(group1, new MockGroup("xxxx"))));
    }

    @Test
    public void getUsersInGroupNamesSortsCorrectly() throws Exception
    {
        MockCrowdService mockCrowdService = new MockCrowdService();
        UserUtilImpl userUtil = new UserUtilImpl(null, null, mockCrowdService, null, null, null, null, null, null,
                null, null, null, null, userManager, null, new MockStudioHooks(), cacheManager);

        final Group group1 = new MockGroup("group1");
        mockCrowdService.addGroup(group1);
        final User user1 = new MockUser("a", "Erin Erlang", "");
        mockCrowdService.addUser(user1, null);
        mockCrowdService.addUserToGroup(user1, group1);
        final User user2 = new MockUser("b", "bob down", "");
        mockCrowdService.addUser(user2, null);
        mockCrowdService.addUserToGroup(user2, group1);
        final User user3 = new MockUser("c", "Andrew Aadvark", "");
        mockCrowdService.addUser(user3, null);
        mockCrowdService.addUserToGroup(user3, group1);
        final User user4 = new MockUser("d", "Cathy Caprioska", "");
        mockCrowdService.addUser(user4, null);
        mockCrowdService.addUserToGroup(user4, group1);
        final User user5 = new MockUser("e", "Derryn Derp", "");
        mockCrowdService.addUser(user5, null);
        mockCrowdService.addUserToGroup(user5, group1);

        SortedSet<User> users = userUtil.getUsersInGroupNames(Arrays.asList("group1"));
        Iterator<User> iter = users.iterator();
        assertEquals("Andrew Aadvark", iter.next().getDisplayName());
        assertEquals("bob down", iter.next().getDisplayName());
        assertEquals("Cathy Caprioska", iter.next().getDisplayName());
        assertEquals("Derryn Derp", iter.next().getDisplayName());
        assertEquals("Erin Erlang", iter.next().getDisplayName());
    }

    @Test
    public void shouldNotThrowNullPointerExceptionWhenRemovingNullUser()
    {
        // Set up
        final ApplicationUser mockLoggedInUser = mock(ApplicationUser.class);

        // Invoke
        userUtil.removeUser(mockLoggedInUser, null);

        // Check
        verifyNoMoreInteractions(userManager);
    }
}
