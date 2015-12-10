package com.atlassian.jira.security;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.fugue.Option;
import com.atlassian.jira.event.MockEventPublisher;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.security.plugin.GlobalPermissionTypesManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.util.UserUtil;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.jira.security.Permissions.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class TestDefaultGlobalPermissionManager
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    @AvailableInContainer
    private UserUtil userUtil;
    @Mock
    private GlobalPermissionTypesManager globalPermissionTypesManager;
    @Mock
    private CacheManager cacheManager;
    protected MockCrowdService crowdService;
    protected MockEventPublisher eventPublisher;

    protected DefaultGlobalPermissionManager globalPermissionManager;

    @Before
    public void setUp()
    {
        crowdService = new MockCrowdService();
        eventPublisher = new MockEventPublisher();
        when(globalPermissionTypesManager.getAll()).thenReturn(MockGlobalPermissionTypeManager.SYSTEM_PERMISSIONS);
        globalPermissionManager = new DefaultGlobalPermissionManager(crowdService, new MockOfBizDelegator(), eventPublisher, globalPermissionTypesManager, new MemoryCacheManager());

        when(globalPermissionTypesManager.getGlobalPermission(GlobalPermissionKey.USE)).thenReturn(Option.option(new GlobalPermissionType(GlobalPermissionKey.USE.getKey(), null, null, false)));
        when(globalPermissionTypesManager.getGlobalPermission(GlobalPermissionKey.ADMINISTER)).thenReturn(Option.option(new GlobalPermissionType(GlobalPermissionKey.ADMINISTER.getKey(), null, null, false)));
        when(globalPermissionTypesManager.getGlobalPermission(GlobalPermissionKey.SYSTEM_ADMIN)).thenReturn(Option.option(new GlobalPermissionType(GlobalPermissionKey.SYSTEM_ADMIN.getKey(), null, null, false)));
        when(globalPermissionTypesManager.getGlobalPermission(GlobalPermissionKey.BULK_CHANGE)).thenReturn(Option.option(new GlobalPermissionType(GlobalPermissionKey.BULK_CHANGE.getKey(), null, null, true)));
        when(globalPermissionTypesManager.getGlobalPermission(GlobalPermissionKey.MANAGE_GROUP_FILTER_SUBSCRIPTIONS)).thenReturn(Option.option(new GlobalPermissionType(GlobalPermissionKey.MANAGE_GROUP_FILTER_SUBSCRIPTIONS.getKey(), null, null, true)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddUseToNullGroup_Legacy() throws Exception
    {
        globalPermissionManager.addPermission(Permissions.USE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddUseToNullGroup() throws Exception
    {
        globalPermissionManager.addPermission(new GlobalPermissionType("USE", null, null, false), null);
    }

    @Test
    public void canAddToNullGroup() throws Exception
    {
        globalPermissionManager.addPermission(new GlobalPermissionType("Foo", null, null, true), null);
    }

    @Test
    public void hasPermission_Legacy() throws Exception
    {
        // user1 belongs to group1
        final ApplicationUser user1 = new MockApplicationUser("anne");
        crowdService.addUser(user1);
        final Group group1 = new MockGroup("group1");
        crowdService.addGroup(new MockGroup("group1"));
        crowdService.addUserToGroup(user1, group1);
        // user2 belongs to group2
        final ApplicationUser user2 = new MockApplicationUser("bob");
        crowdService.addUser(user2);
        final Group group2 = new MockGroup("group2");
        crowdService.addGroup(new MockGroup("group2"));
        crowdService.addUserToGroup(user2, group2);

        assertFalse(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user1));
        globalPermissionManager.addPermission(Permissions.ADMINISTER, "group1");
        assertTrue(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user1));
        assertFalse(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user2));

        assertFalse(globalPermissionManager.hasPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, user2));
        globalPermissionManager.addPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, "group2");
        assertTrue(globalPermissionManager.hasPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, user2));
        assertFalse(globalPermissionManager.hasPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, user1));
    }

    @Test
    public void hasPermission() throws Exception
    {
        // user1 belongs to group1
        final ApplicationUser user1 = new MockApplicationUser("anne");
        crowdService.addUser(user1);
        final Group group1 = new MockGroup("group1");
        crowdService.addGroup(new MockGroup("group1"));
        crowdService.addUserToGroup(user1, group1);
        // user2 belongs to group2
        final ApplicationUser user2 = new MockApplicationUser("bob");
        crowdService.addUser(user2);
        final Group group2 = new MockGroup("group2");
        crowdService.addGroup(new MockGroup("group2"));
        crowdService.addUserToGroup(user2, group2);

        assertFalse(globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user1));
        globalPermissionManager.addPermission(globalPermissionManager.getGlobalPermission(GlobalPermissionKey.ADMINISTER).get(), "group1");
        assertTrue(globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user1));
        assertFalse(globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user2));

        assertFalse(globalPermissionManager.hasPermission(GlobalPermissionKey.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, user2));
        globalPermissionManager.addPermission(globalPermissionManager.getGlobalPermission(GlobalPermissionKey.MANAGE_GROUP_FILTER_SUBSCRIPTIONS).get(), "group2");
        assertTrue(globalPermissionManager.hasPermission(GlobalPermissionKey.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, user2));
        assertFalse(globalPermissionManager.hasPermission(GlobalPermissionKey.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, user1));
    }

    @Test
    public void testSysAdminImpliesAdminHack() throws Exception
    {
        // user1 belongs to group1
        final ApplicationUser bob = new MockApplicationUser("bob");
        crowdService.addUser(bob);
        final Group group1 = new MockGroup("group1");
        crowdService.addGroup(new MockGroup("group1"));
        crowdService.addUserToGroup(bob, group1);
        // user2 belongs to group2
        final ApplicationUser joe = new MockApplicationUser("joe");
        crowdService.addUser(joe);
        final Group group2 = new MockGroup("group2");
        crowdService.addGroup(new MockGroup("group2"));
        crowdService.addUserToGroup(joe, group2);

        // Add bob's group to the SYSTEM_ADMIN global permission
        globalPermissionManager.addPermission(Permissions.SYSTEM_ADMIN, "group1");
        // Add joe's group to the ADMIN global permission
        globalPermissionManager.addPermission(Permissions.ADMINISTER, "group2");

        // Verify that asking for bob in the ADMIN global role returns true
        assertTrue(globalPermissionManager.hasPermission(Permissions.ADMINISTER, bob));
        // Make sure that the explict call for SYS_ADMIN is correct as well
        assertTrue(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, bob));

        // Verify that joe is an Admin
        assertTrue(globalPermissionManager.hasPermission(Permissions.ADMINISTER, joe));
        // Verify that joe is not a Sys Admin
        assertFalse(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, joe));
    }

    @Test
    public void testAddUsePermissionClearsUserCount() throws CreateException
    {
        globalPermissionManager.addPermission(Permissions.SYSTEM_ADMIN, "group1");
        Mockito.verify(userUtil).clearActiveUserCount();
    }

    @Test
    public void testAddNonUsePermissionDoesNotClearUserCount() throws CreateException
    {
        globalPermissionManager.addPermission(Permissions.BULK_CHANGE, "group1");
        Mockito.verify(userUtil, never()).clearActiveUserCount();
    }

    @Test
    public void testRemovePermissionWithUserLimit() throws RemoveException, CreateException
    {
        //let's create a permission first.
        globalPermissionManager.addPermission(Permissions.ADMINISTER, "group1");

        Mockito.verify(userUtil, times(1)).clearActiveUserCount();
        globalPermissionManager.removePermission(Permissions.ADMINISTER, "group1");
        Mockito.verify(userUtil, times(2)).clearActiveUserCount();
    }

    @Test
    public void testRemoveNonUsePermissionWithUserLimit() throws RemoveException, CreateException
    {
        //let's create a permission first.
        globalPermissionManager.addPermission(Permissions.BULK_CHANGE, "group1");

        globalPermissionManager.removePermission(Permissions.BULK_CHANGE, "group1");
        Mockito.verify(userUtil, never()).clearActiveUserCount();
    }

    @Test
    public void testRemovePermissionsWithLimitedLicense()
            throws CreateException, RemoveException, OperationNotPermittedException, InvalidGroupException
    {
        crowdService.addGroup(new MockGroup("group1"));
        //let's create a permission first.
        globalPermissionManager.addPermission(Permissions.ADMINISTER, "group1");
        globalPermissionManager.addPermission(Permissions.SYSTEM_ADMIN, "group1");
        globalPermissionManager.addPermission(Permissions.BULK_CHANGE, "group1");


        Mockito.verify(userUtil, times(2)).clearActiveUserCount();
        globalPermissionManager.removePermissions("group1");
        Mockito.verify(userUtil, times(4)).clearActiveUserCount();
    }

    @Test
    public void testIsGlobalPermission()
    {
        assertFalse(globalPermissionManager.isGlobalPermission(COMMENT_EDIT_ALL));
        assertFalse(globalPermissionManager.isGlobalPermission(COMMENT_EDIT_OWN));
        assertFalse(globalPermissionManager.isGlobalPermission(COMMENT_DELETE_ALL));
        assertFalse(globalPermissionManager.isGlobalPermission(COMMENT_DELETE_OWN));
        assertFalse(globalPermissionManager.isGlobalPermission(ATTACHMENT_DELETE_ALL));
        assertFalse(globalPermissionManager.isGlobalPermission(ATTACHMENT_DELETE_OWN));
        assertFalse(globalPermissionManager.isGlobalPermission(WORKLOG_DELETE_ALL));
        assertFalse(globalPermissionManager.isGlobalPermission(WORKLOG_DELETE_OWN));
        assertFalse(globalPermissionManager.isGlobalPermission(WORKLOG_EDIT_ALL));
        assertFalse(globalPermissionManager.isGlobalPermission(WORKLOG_EDIT_OWN));

        assertTrue(globalPermissionManager.isGlobalPermission(SYSTEM_ADMIN));
        assertTrue(globalPermissionManager.isGlobalPermission(ADMINISTER));
        assertTrue(globalPermissionManager.isGlobalPermission(USE));
        assertTrue(globalPermissionManager.isGlobalPermission(USER_PICKER));
        assertTrue(globalPermissionManager.isGlobalPermission(CREATE_SHARED_OBJECTS));
        assertTrue(globalPermissionManager.isGlobalPermission(MANAGE_GROUP_FILTER_SUBSCRIPTIONS));
        assertTrue(globalPermissionManager.isGlobalPermission(BULK_CHANGE));
    }

    @Test
    public void testGetPluginGlobalPermissions()
    {
        when(globalPermissionTypesManager.getAll()).thenReturn(Lists.newArrayList(
                new GlobalPermissionType( "one", null, null, true),
                new GlobalPermissionType( "two", null, null, true)));

        Iterable<GlobalPermissionType> globalPermissions = globalPermissionManager.getAllGlobalPermissions();

        assertThat(globalPermissions, Matchers.<GlobalPermissionType>hasItem(Matchers.hasProperty("key", Matchers.is("one"))));
        assertThat(globalPermissions, Matchers.<GlobalPermissionType>hasItem(Matchers.hasProperty("key", Matchers.is("two"))));
    }

    @Test
    public void testGetPluggablePermission()
    {
        when(globalPermissionTypesManager.getGlobalPermission(Mockito.eq("permissionKey"))).thenReturn(some(new GlobalPermissionType(null, null, null, true)));

        assertThat(globalPermissionManager.getGlobalPermission("permissionKey").isDefined(), Matchers.is(true));
    }

    @Test
    public void testRemovalNonExistingPermission()
    {
        exception.expect(IllegalArgumentException.class);
        globalPermissionManager.removePermission(100, null);
    }

}
