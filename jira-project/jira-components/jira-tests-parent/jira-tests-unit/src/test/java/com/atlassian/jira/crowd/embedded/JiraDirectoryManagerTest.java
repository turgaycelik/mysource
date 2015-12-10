package com.atlassian.jira.crowd.embedded;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.dao.permission.InternalUserPermissionDAO;
import com.atlassian.crowd.directory.InternalRemoteDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.directory.DirectoryDeletedEvent;
import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.user.UserCreatedEvent;
import com.atlassian.crowd.event.user.UserCredentialUpdatedEvent;
import com.atlassian.crowd.event.user.UserRenamedEvent;
import com.atlassian.crowd.exception.DirectoryCurrentlySynchronisingException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.NestedGroupsNotSupportedException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.directory.BulkAddResult;
import com.atlassian.crowd.manager.directory.DirectoryManagerGeneric;
import com.atlassian.crowd.manager.directory.DirectoryPermissionException;
import com.atlassian.crowd.manager.directory.DirectorySynchroniser;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;
import com.atlassian.crowd.manager.directory.monitor.poller.DirectoryPollerManager;
import com.atlassian.crowd.manager.lock.DirectoryLockManager;
import com.atlassian.crowd.manager.permission.PermissionManager;
import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.permission.InternalGrantedPermission;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.event.api.EventPublisher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


/**
 * DirectoryManagerGeneric Tester.
 * <p>
 * CWD-4028
 * </p>
 * <p>
 * This test has been stolen from crowd to help ensure the correctness of JiraDirectoryManager.
 * If CWD-4028 gets fixed, then JIRA should be able to switch back to using DirectoryManagerGeneric
 * and will not need this mock anymore.
 * </p>
 */
public class JiraDirectoryManagerTest
{
    private DirectoryManagerGeneric directoryManager = null;

    @Mock private RemoteDirectory remoteDirectory;
    @Mock private DirectoryDao directoryDao;
    @Mock private ApplicationDAO applicationDAO;
    @Mock private EventPublisher eventPublisher;
    @Mock private PermissionManager permissionManager;
    @Mock private DirectorySynchroniser directorySynchroniser;
    @Mock private DirectoryPollerManager directoryPollerManager;
    @Mock private DirectoryLockManager directoryLockManager;
    @Mock private DirectoryInstanceLoader directoryInstanceLoader;
    @Mock private Directory directory;
    @Mock private SynchronisationStatusManager synchronisationStatusManager;
    @Mock private InternalUserPermissionDAO userPermissionDao;
    @Mock private Lock lock;


    private static final long DIRECTORY_ID = 1L;

    private static final String USERNAME1 = "user1";
    private static final String USERNAME2 = "user2";
    private static final String USERNAME3 = "user3";

    private static final User USER1 = new UserTemplate(USERNAME1, DIRECTORY_ID);
    private static final User USER2 = new UserTemplate(USERNAME2, DIRECTORY_ID);
    private static final User USER3 = new UserTemplate(USERNAME3, DIRECTORY_ID);

    private static final String GROUP_NAME1 = "group1";
    private static final String GROUP_NAME2 = "group2";
    private static final String GROUP_NAME3 = "group3";

    private static final GroupTemplate GROUP1 = new GroupTemplate(GROUP_NAME1, DIRECTORY_ID);
    private static final GroupTemplate GROUP2 = new GroupTemplate(GROUP_NAME2, DIRECTORY_ID);
    private static final GroupTemplate GROUP3 = new GroupTemplate(GROUP_NAME3, DIRECTORY_ID);

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);

        directoryManager = new JiraDirectoryManager(
                directoryDao,
                applicationDAO,
                eventPublisher,
                permissionManager,
                directoryInstanceLoader,
                directorySynchroniser,
                directoryPollerManager,
                directoryLockManager,
                synchronisationStatusManager,
                userPermissionDao);

        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);

        when(directory.getName()).thenReturn("testing-directory");
        when(directory.getId()).thenReturn(DIRECTORY_ID);
        when(directory.getType()).thenReturn(DirectoryType.INTERNAL);

        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(remoteDirectory);

        when(directorySynchroniser.isSynchronising(anyLong())).thenReturn(false);
        when(directoryLockManager.getLock(anyLong())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
    }

    @After
    public void tearDown()
    {
        directoryManager = null;
        remoteDirectory = null;
        directoryDao = null;
        applicationDAO = null;
        eventPublisher = null;
        permissionManager = null;
        directorySynchroniser = null;
        directoryPollerManager = null;
        directoryInstanceLoader = null;
        directory = null;
    }

    @Test
    public void testAddAllGroups_EventsPublished() throws Exception
    {
        final InternalRemoteDirectory internalRemoteDirectory = new MockInternalRemoteDirectory();
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(internalRemoteDirectory);
        when(permissionManager.hasPermission(directory, OperationType.CREATE_GROUP)).thenReturn(true);

        directoryManager.addAllGroups(DIRECTORY_ID, ImmutableList.of(GROUP1, GROUP2, GROUP3), false);

        verify(eventPublisher, times(3)).publish(isA(GroupCreatedEvent.class));
    }

    @Test
    public void testAddAllUsers_EventsPublished() throws Exception
    {
        final UserTemplateWithCredentialAndAttributes USER_COMPLETE1 = new UserTemplateWithCredentialAndAttributes(USER1, PasswordCredential.NONE);
        final UserTemplateWithCredentialAndAttributes USER_COMPLETE2 = new UserTemplateWithCredentialAndAttributes(USER2, PasswordCredential.NONE);

        final InternalRemoteDirectory internalRemoteDirectory = new MockInternalRemoteDirectory();
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(internalRemoteDirectory);
        when(permissionManager.hasPermission(directory, OperationType.CREATE_USER)).thenReturn(true);

        directoryManager.addAllUsers(DIRECTORY_ID, ImmutableList.of(USER_COMPLETE1, USER_COMPLETE2), false);

        verify(eventPublisher, times(2)).publish(isA(UserCreatedEvent.class));
    }

    @Test
    public void testAddAllUsersToGroup_EventsPublished() throws Exception
    {
        final InternalRemoteDirectory internalRemoteDirectory = new MockInternalRemoteDirectory()
        {
            @Override
            public void addUserToGroup(String username, String groupName) throws GroupNotFoundException, UserNotFoundException, OperationFailedException, MembershipAlreadyExistsException
            {
                // ignore
            }
        };
        internalRemoteDirectory.addGroup(GROUP1);
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(internalRemoteDirectory);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);

        directoryManager.addAllUsersToGroup(DIRECTORY_ID, ImmutableList.of(USERNAME1, USERNAME2), GROUP_NAME1);

        verify(eventPublisher, times(2)).publish(isA(GroupMembershipCreatedEvent.class));
    }

    @Test
    public void testRemoveInternalDirectory() throws Exception
    {
        // now make the call
        directoryManager.removeDirectory(directory);

        // Remove directory from applications
        verify(applicationDAO).removeDirectoryMappings(DIRECTORY_ID);

        // Delete the directory
        verify(directoryDao).remove(directory);

        verify(eventPublisher).publish(any(DirectoryDeletedEvent.class));
    }

    @Test
    public void testRemoveDelegatedDirectory() throws Exception
    {
        // Make the directory type delegated
        when(directory.getType()).thenReturn(DirectoryType.DELEGATING);

        directoryManager.removeDirectory(directory);

        // Remove directory from applications
        verify(applicationDAO).removeDirectoryMappings(DIRECTORY_ID);

        // Delete the directory
        verify(directoryDao).remove(directory);

        verify(eventPublisher).publish(any(DirectoryDeletedEvent.class));
    }

    @Test
    public void testRemoveConnectorDirectory() throws Exception
    {
        when(directory.getType()).thenReturn(DirectoryType.CONNECTOR);

        directoryManager.removeDirectory(directory);

        // Remove directory from applications
        verify(applicationDAO).removeDirectoryMappings(DIRECTORY_ID);

        // Delete the directory
        verify(directoryDao).remove(directory);

        verify(eventPublisher).publish(any(DirectoryDeletedEvent.class));
    }

    /**
     * Tests that removing a directory while a synchronising is occurring will throw a
     * {@link com.atlassian.crowd.exception.DirectoryCurrentlySynchronisingException}.
     */
    @Test(expected = DirectoryCurrentlySynchronisingException.class)
    public void testRemoveConnectorDirectory_DirectoryIsSynchronising() throws Exception
    {
        when(lock.tryLock()).thenReturn(false);
        when(directory.getType()).thenReturn(DirectoryType.CONNECTOR);

        directoryManager.removeDirectory(directory);

        // Remove directory from applications
        verify(applicationDAO, never()).removeDirectoryMappings(DIRECTORY_ID);

        // Delete the directory
        verify(directoryDao, never()).remove(directory);

        verify(eventPublisher, never()).publish(any(DirectoryDeletedEvent.class));
    }

    @Test
    public void addUserToGroupSucceeds() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.findUserByName(USERNAME1)).thenReturn(USER1);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);

        directoryManager.addUserToGroup(DIRECTORY_ID, USERNAME1, GROUP_NAME1);

        verify(remoteDirectory).addUserToGroup(USERNAME1, GROUP_NAME1);

        verify(eventPublisher).publish(any(GroupMembershipCreatedEvent.class));
    }

    @Test
    public void addUserToGroupFailsWhenPermissionIsDenied() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.findUserByName(USERNAME1)).thenReturn(USER1);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(false);

        try
        {
            directoryManager.addUserToGroup(DIRECTORY_ID, USERNAME1, GROUP_NAME1);
            fail("DirectoryPermissionException expected");
        }
        catch (DirectoryPermissionException e)
        {
            // expected
        }

        verify(remoteDirectory, never()).addUserToGroup(USERNAME1, GROUP_NAME1);

        verify(eventPublisher, never()).publish(any(GroupMembershipCreatedEvent.class));
    }

    @Test
    public void addUserToGroupFailsWhenMembershipAlreadyExists() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.findUserByName(USERNAME1)).thenReturn(USER1);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);

        doThrow(new MembershipAlreadyExistsException(DIRECTORY_ID, USERNAME1, GROUP_NAME1))
            .when(remoteDirectory).addUserToGroup(USERNAME1, GROUP_NAME1);

        try
        {
            directoryManager.addUserToGroup(DIRECTORY_ID, USERNAME1, GROUP_NAME1);
            fail("MembershipAlreadyExistsException expected");
        }
        catch (MembershipAlreadyExistsException e)
        {
            // expected
        }

        verify(eventPublisher, never()).publish(any(GroupMembershipCreatedEvent.class));
    }

    @Test
    public void addGroupToGroupSucceeds() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        when(remoteDirectory.findGroupByName(GROUP_NAME2)).thenReturn(GROUP2);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);

        directoryManager.addGroupToGroup(DIRECTORY_ID, GROUP_NAME1, GROUP_NAME2);

        verify(remoteDirectory).addGroupToGroup(GROUP_NAME1, GROUP_NAME2);

        verify(eventPublisher).publish(any(GroupMembershipCreatedEvent.class));
    }

    @Test
    public void addGroupToGroupFailsWhenPermissionIsDenied() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        when(remoteDirectory.findGroupByName(GROUP_NAME2)).thenReturn(GROUP2);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(false);

        try
        {
            directoryManager.addGroupToGroup(DIRECTORY_ID, GROUP_NAME1, GROUP_NAME2);
            fail("DirectoryPermissionException expected");
        }
        catch (DirectoryPermissionException e)
        {
            // expected
        }

        verify(remoteDirectory, never()).addGroupToGroup(GROUP_NAME1, GROUP_NAME2);

        verify(eventPublisher, never()).publish(any(GroupMembershipCreatedEvent.class));
    }

    @Test
    public void addGroupToGroupFailsWhenDirectoryDoesNotSupportNestedGroups() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(false);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        when(remoteDirectory.findGroupByName(GROUP_NAME2)).thenReturn(GROUP2);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(false);

        try
        {
            directoryManager.addGroupToGroup(DIRECTORY_ID, GROUP_NAME1, GROUP_NAME2);
            fail("NestedGroupsNotSupportedException expected");
        }
        catch (NestedGroupsNotSupportedException e)
        {
            // expected
        }

        verify(remoteDirectory, never()).addGroupToGroup(GROUP_NAME1, GROUP_NAME2);

        verify(eventPublisher, never()).publish(any(GroupMembershipCreatedEvent.class));
    }

    @Test
    public void addGroupToGroupFailsWhenMembershipAlreadyExists() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        when(remoteDirectory.findGroupByName(GROUP_NAME2)).thenReturn(GROUP2);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);

        doThrow(new MembershipAlreadyExistsException(DIRECTORY_ID, GROUP_NAME1, GROUP_NAME2))
            .when(remoteDirectory).addGroupToGroup(GROUP_NAME1, GROUP_NAME2);

        try
        {
            directoryManager.addGroupToGroup(DIRECTORY_ID, GROUP_NAME1, GROUP_NAME2);
            fail("MembershipAlreadyExistsException expected");
        }
        catch (MembershipAlreadyExistsException e)
        {
            // expected
        }

        verify(eventPublisher, never()).publish(any(GroupMembershipCreatedEvent.class));
    }

    /**
     * Test that nested search for user members returns requested amount of results in the order they are found.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchNestedGroupRelationships_UserChildrenMaxResults() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME3)).thenReturn(GROUP3);
        when(remoteDirectory.searchGroupRelationships(createUserChildrenQuery(GROUP_NAME3, 1))).thenReturn(ImmutableList.of(USER3));
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME3, EntityQuery.ALL_RESULTS))).thenReturn(ImmutableList.<Group>of(GROUP2));
        when(remoteDirectory.searchGroupRelationships(createUserChildrenQuery(GROUP_NAME2, 1))).thenReturn(ImmutableList.of(USER2));
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME2, EntityQuery.ALL_RESULTS))).thenReturn(ImmutableList.<Group>of(GROUP1));
        when(remoteDirectory.searchGroupRelationships(createUserChildrenQuery(GROUP_NAME1, 1))).thenReturn(ImmutableList.of(USER1));
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME1, EntityQuery.ALL_RESULTS))).thenReturn(ImmutableList.<Group>of());

        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(GROUP_NAME3).returningAtMost(1);

        final List<String> results = directoryManager.searchNestedGroupRelationships(DIRECTORY_ID, query);

        assertEquals(1, results.size());
        assertEquals(USERNAME3, results.get(0));
    }

    /**
     * Test that nested search for group members returns requested amount of results in the order they are found.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchNestedGroupRelationships_GroupChildrenMaxResults() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME3)).thenReturn(GROUP3);
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME3, 1))).thenReturn(ImmutableList.<Group>of(GROUP2));
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME2, 1))).thenReturn(ImmutableList.<Group>of(GROUP1));
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME1, 1))).thenReturn(ImmutableList.<Group>of());

        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group()).withName(GROUP_NAME3).returningAtMost(1);

        final List<String> results = directoryManager.searchNestedGroupRelationships(DIRECTORY_ID, query);

        assertEquals(1, results.size());
        assertEquals(GROUP_NAME2, results.get(0));
    }

    /**
     * Test that nested search for user's group memberships returns requested amount of results in the order they are found.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchNestedGroupRelationships_UserParentsMaxResults() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findUserByName(USERNAME3)).thenReturn(USER3);
        when(remoteDirectory.searchGroupRelationships(createUserParentsQuery(USERNAME3, 1))).thenReturn(ImmutableList.<Group>of(GROUP3));
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME3, 1))).thenReturn(ImmutableList.<Group>of(GROUP2));
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME2, 1))).thenReturn(ImmutableList.<Group>of(GROUP1));
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME1, 1))).thenReturn(ImmutableList.<Group>of());

        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(USERNAME3).returningAtMost(1);

        final List<String> results = directoryManager.searchNestedGroupRelationships(DIRECTORY_ID, query);

        assertEquals(1, results.size());
        assertEquals(GROUP_NAME3, results.get(0));
    }

    /**
     * Test that nested search for group's group memberships returns requested amount of results in the order they are found.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchNestedGroupRelationships_GroupParentsMaxResults() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME3)).thenReturn(GROUP3);
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME3, 1))).thenReturn(ImmutableList.<Group>of(GROUP2));
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME2, 1))).thenReturn(ImmutableList.<Group>of(GROUP1));
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME1, 1))).thenReturn(ImmutableList.<Group>of());

        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.group()).withName(GROUP_NAME3).returningAtMost(1);

        final List<String> results = directoryManager.searchNestedGroupRelationships(DIRECTORY_ID, query);

        assertEquals(1, results.size());
        assertEquals(GROUP_NAME2, results.get(0));
    }

    @Test
    public void testUpdatePrincipalCredentialFiresEvent() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(permissionManager.hasPermission(eq(directory), any(OperationType.class))).thenReturn(true);

        directoryManager.updateUserCredential(DIRECTORY_ID, "beep", new PasswordCredential("newPassword"));

        verify(directoryDao, times(2)).findById(DIRECTORY_ID);
        verify(permissionManager).hasPermission(eq(directory), any(OperationType.class));
        verify(remoteDirectory).updateUserCredential(anyString(), any(PasswordCredential.class));

        // this is the point of the test - must be called.
        verify(eventPublisher).publish(any(UserCredentialUpdatedEvent.class));
    }

    private static MembershipQuery<User> createUserChildrenQuery(String name, int maxResults)
    {
        return QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(name).returningAtMost(maxResults);
    }

    private static MembershipQuery<Group> createGroupMembershipQuery(String name, int maxResults)
    {
        return QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group()).withName(name).returningAtMost(maxResults);
    }

    private static MembershipQuery<Group> createUserParentsQuery(String name, int maxResults)
    {
        return QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(name).returningAtMost(maxResults);
    }

    private static MembershipQuery<Group> createGroupParentsQuery(String name, int maxResults)
    {
        return QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.group()).withName(name).returningAtMost(maxResults);
    }

    private void allowAddingUser(String name) throws Exception
    {
        when(remoteDirectory.findUserByName(name)).thenThrow(new UserNotFoundException(name));
        when(permissionManager.hasPermission(directory, OperationType.CREATE_USER)).thenReturn(true);
    }

    private void allowAddingGroup(String name) throws Exception
    {
        when(remoteDirectory.findGroupByName(name)).thenThrow(new GroupNotFoundException(name));
        when(permissionManager.hasPermission(directory, OperationType.CREATE_GROUP)).thenReturn(true);
    }

    @Test
    public void ableToAddUser() throws Exception
    {
        allowAddingUser("user");

        UserTemplate user = new UserTemplate("user");

        directoryManager.addUser(1, user, PasswordCredential.NONE);
        verify(remoteDirectory).addUser(user, PasswordCredential.NONE);
    }

    @Test(expected = InvalidUserException.class)
    public void unableToCreateUserWithTrailingWhitespace() throws Exception
    {
        allowAddingUser("user ");

        UserTemplate user = new UserTemplate("user ");

        directoryManager.addUser(1, user, PasswordCredential.NONE);
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void unableToAddDuplicateUser() throws Exception
    {
        when(remoteDirectory.findUserByName("user")).thenReturn(mock(User.class));

        UserTemplate user = new UserTemplate("user");
        directoryManager.addUser(1, user, PasswordCredential.NONE);
    }

    @Test
    public void ableToAddGroup() throws Exception
    {
        allowAddingGroup("group");

        GroupTemplate group = new GroupTemplate("group");

        directoryManager.addGroup(1, group);
        verify(remoteDirectory).addGroup(group);
    }

    @Test(expected = InvalidGroupException.class)
    public void unableToCreateGroupWithTrailingWhitespace() throws Exception
    {
        allowAddingGroup("group ");

        GroupTemplate group = new GroupTemplate("group ");

        directoryManager.addGroup(1, group);
    }

    private static Set<String> namesOf(Iterable<? extends DirectoryEntity> list)
    {
        Set<String> names = new HashSet<String>(8);

        for (DirectoryEntity e : list)
        {
            assertTrue(names.add(e.getName()));
        }

        return names;
    }

    @Test
    public void bulkAddUsersFailsToAddUsersWithTrailingWhitespace() throws Exception
    {
        allowAddingUser("user");
        allowAddingUser("user ");
        allowAddingUser("user2");

        Collection<UserTemplateWithCredentialAndAttributes> users =
                ImmutableList.of(
                        new UserTemplateWithCredentialAndAttributes("user", DIRECTORY_ID, PasswordCredential.NONE),
                        new UserTemplateWithCredentialAndAttributes("user ", DIRECTORY_ID, PasswordCredential.NONE),
                        new UserTemplateWithCredentialAndAttributes("user2", DIRECTORY_ID, PasswordCredential.NONE)
                );

        BulkAddResult<User> result = directoryManager.addAllUsers(DIRECTORY_ID, users, false);

        assertThat(result.getExistingEntities(), Matchers.<User>empty());
        assertEquals(2, result.getAddedSuccessfully());
        assertEquals(ImmutableSet.of("user "), namesOf(result.getFailedEntities()));
    }

    @Test
    public void bulkAddGroupsFailsToAddGroupsWithTrailingWhitespace() throws Exception
    {
        allowAddingGroup("group");
        allowAddingGroup("group ");
        allowAddingGroup("group2");

        Collection<GroupTemplate> groups =
                ImmutableList.of(
                        new GroupTemplate("group"),
                        new GroupTemplate("group "),
                        new GroupTemplate("group2")
                );

        BulkAddResult<Group> result = directoryManager.addAllGroups(DIRECTORY_ID, groups, false);

        assertThat(result.getExistingEntities(), Matchers.<Group>empty());
        assertEquals(2, result.getAddedSuccessfully());
        assertEquals(ImmutableSet.of("group "), namesOf(result.getFailedEntities()));
    }

    @Test
    public void renamingUserSucceeds() throws Exception
    {
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_USER)).thenReturn(true);
        directoryManager.renameUser(DIRECTORY_ID, "user", "user2");
        verify(remoteDirectory).renameUser("user", "user2");
    }

    @Test(expected = InvalidUserException.class)
    public void renamingUserToHaveTrailingWhitespaceFails() throws Exception
    {
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_USER)).thenReturn(true);
        directoryManager.renameUser(DIRECTORY_ID, "user", "user ");
    }

    @Test
    public void renamingUserToHaveTrailingWhitespaceFailsWithAppropriateUserInException() throws Exception
    {
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_USER)).thenReturn(true);

        try
        {
            directoryManager.renameUser(DIRECTORY_ID, "user", "user ");
            fail();
        }
        catch (InvalidUserException e)
        {
            com.atlassian.crowd.embedded.api.User user = e.getUser();
            assertEquals(DIRECTORY_ID, user.getDirectoryId());
            assertEquals("user ", user.getName());
        }
    }

    @Test
    public void renamingUserPublishesEvent() throws Exception
    {
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_USER)).thenReturn(true);
        when(remoteDirectory.renameUser("user", "user2")).thenReturn(new UserTemplate("user2"));

        directoryManager.renameUser(DIRECTORY_ID, "user", "user2");

        ArgumentCaptor<UserRenamedEvent> eventCaptor = ArgumentCaptor.forClass(UserRenamedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("user", eventCaptor.getValue().getOldUsername());
        assertEquals("user2", eventCaptor.getValue().getUser().getName());
    }

    @Test
    public void renamingGroupSucceeds() throws Exception
    {
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        directoryManager.renameGroup(DIRECTORY_ID, GROUP_NAME1, "new-name-for-group");
        verify(remoteDirectory).renameGroup(GROUP_NAME1, "new-name-for-group");
    }

    @Test(expected = InvalidGroupException.class)
    public void renamingGroupToHaveTrailingWhitespaceFails() throws Exception
    {
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        directoryManager.renameGroup(DIRECTORY_ID, GROUP_NAME1, "group ");
    }

    @Test
    public void renamingGroupToHaveTrailingWhitespaceFailsWithAppropriateGroupInException() throws Exception
    {
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);

        try
        {
            directoryManager.renameGroup(DIRECTORY_ID, GROUP_NAME1, "group ");
            fail();
        }
        catch (InvalidGroupException e)
        {
            Group group = e.getGroup();
            assertEquals(DIRECTORY_ID, group.getDirectoryId());
            assertEquals("group ", group.getName());
            assertEquals(GroupType.GROUP, group.getType());
        }
    }

    @Test
    public void testRemoveGroupRemovesPermissions() throws Exception
    {
        when(permissionManager.hasPermission(directory, OperationType.DELETE_GROUP)).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        InternalGrantedPermission permission = mock(InternalGrantedPermission.class);
        when(userPermissionDao.findAllPermissionsForGroup(GROUP_NAME1, DIRECTORY_ID)).thenReturn(ImmutableList.of(permission));
        
        directoryManager.removeGroup(DIRECTORY_ID, GROUP_NAME1);
        
        verify(userPermissionDao).findAllPermissionsForGroup(GROUP_NAME1, DIRECTORY_ID);
        verify(userPermissionDao).revoke(permission);
        verifyNoMoreInteractions(userPermissionDao);
    }

    @Test
    public void testAddAllUsersToGroupReturnsExistingMembershipsForInternalRemoteDirectory() throws Exception
    {
        final InternalRemoteDirectory internalRemoteDirectory = new MockInternalRemoteDirectory();
        internalRemoteDirectory.addGroup(GROUP1);
        internalRemoteDirectory.addUserToGroup(USERNAME1, GROUP_NAME1);
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(internalRemoteDirectory);
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);

        final BulkAddResult<String> bulkAddResult = directoryManager.addAllUsersToGroup(DIRECTORY_ID, ImmutableSet.of(USERNAME1, USERNAME2), GROUP_NAME1);
        assertThat(bulkAddResult.getExistingEntities(), contains(USERNAME1));
        assertThat(bulkAddResult.getFailedEntities(), Matchers.<String>empty());
    }

    @Test
    public void testAddAllUsersToGroupReturnsExistingMembershipsForNonInternalRemoteDirectory() throws Exception
    {
        when(remoteDirectory.findGroupByName(GROUP_NAME1)).thenReturn(GROUP1);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);
        doThrow(new MembershipAlreadyExistsException(USERNAME1, GROUP_NAME1)).when(remoteDirectory).addUserToGroup(USERNAME1, GROUP_NAME1);

        final BulkAddResult<String> bulkAddResult = directoryManager.addAllUsersToGroup(DIRECTORY_ID, ImmutableSet.of(USERNAME1, USERNAME2), GROUP_NAME1);
        assertThat(bulkAddResult.getExistingEntities(), contains(USERNAME1));
        assertThat(bulkAddResult.getFailedEntities(), Matchers.<String>empty());
    }
}
