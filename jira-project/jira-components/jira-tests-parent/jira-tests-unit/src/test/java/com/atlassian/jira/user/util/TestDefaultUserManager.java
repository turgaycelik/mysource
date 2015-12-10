package com.atlassian.jira.user.util;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.applinks.api.auth.oauth.ConsumerTokenService;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableDirectory;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockCrowdDirectoryService;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager.UserState;

import org.hamcrest.Matchers;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v4.4
 */
public class TestDefaultUserManager
{
    @Test
    public void testCanUpdateUser_readOnlyDirectory() throws Exception
    {
        final User mockUser = createMockUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), null, false);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null, null, null);
        assertFalse("canUpdateUser", defaultUserManager.canUpdateUser(mockUser));
    }

    @Test
    public void testCanUpdateUser_writableDirectory() throws Exception
    {
        final User mockUser = createMockUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), null, true);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null, null, null);
        assertTrue("canUpdateUser", defaultUserManager.canUpdateUser(mockUser));
    }

    @Test
    public void testCanUpdateUserPassword_readOnlyDirectory() throws Exception
    {
        final User mockUser = createMockUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), null, false);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null, null, null);
        assertFalse("canUpdateUserPassword", defaultUserManager.canUpdateUserPassword(mockUser));
    }

    @Test
    public void testCanUpdateUserPassword_writableNonDelegatedLdapDirectory() throws Exception
    {
        final User mockUser = createMockUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), DirectoryType.CONNECTOR, true);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null, null, null);
        assertTrue("canUpdateUserPassword", defaultUserManager.canUpdateUserPassword(mockUser));
    }

    @Test
    public void testCanUpdateUserPassword_writableDelegatedLdapDirectory() throws Exception
    {
        final User mockUser = createMockUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), DirectoryType.DELEGATING, true);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null, null, null);
        assertFalse("canUpdateUserPassword", defaultUserManager.canUpdateUserPassword(mockUser));
    }

    @Test
    public void testCanRenameUser_invalidDirectory() throws Exception
    {
        final ApplicationUser mockUser = createMockApplicationUser();
        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null, null, null);
        assertFalse("canRenameUser", defaultUserManager.canRenameUser(mockUser));
    }

    @Test
    public void testCanRenameUser_readOnlyDirectory() throws Exception
    {
        final ApplicationUser mockUser = createMockApplicationUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), null, false);
        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null, null, null);
        assertFalse("canRenameUser", defaultUserManager.canRenameUser(mockUser));
    }

    @Test
    public void testCanRenameUser_badDirectoryType() throws Exception
    {
        final ApplicationUser mockUser = createMockApplicationUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), DirectoryType.UNKNOWN, true);
        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null, null, null);
        assertFalse("canRenameUser", defaultUserManager.canRenameUser(mockUser));
    }

    @Test
    public void testCanRenameUser_actingAsCrowdServer() throws Exception
    {
        final ApplicationUser mockUser = createMockApplicationUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), DirectoryType.INTERNAL, true);
        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        final ApplicationManager applicationManager = mock(ApplicationManager.class);
        final Application embeddedCrowd = mock(Application.class);
        final Application externalApplication = mock(Application.class);
        final ApplicationProperties applicationProperties = new MockApplicationProperties();

        crowdDirectoryService.addDirectory(mockDirectory);
        when(embeddedCrowd.isPermanent()).thenReturn(true);
        when(applicationManager.findAll()).thenReturn(asList(embeddedCrowd, externalApplication));

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null,
                applicationManager, applicationProperties);
        assertFalse("canRenameUser", defaultUserManager.canRenameUser(mockUser));
    }

    @Test
    public void testCanRenameUser_applicationPropertyOverridesCrowdServerCheck() throws Exception
    {
        final ApplicationUser mockUser = createMockApplicationUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), DirectoryType.INTERNAL, true);
        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        final ApplicationManager applicationManager = mock(ApplicationManager.class);
        final Application embeddedCrowd = mock(Application.class);
        final Application externalApplication = mock(Application.class);
        final ApplicationProperties applicationProperties = new MockApplicationProperties();

        crowdDirectoryService.addDirectory(mockDirectory);
        when(embeddedCrowd.isPermanent()).thenReturn(true);
        when(applicationManager.findAll()).thenReturn(asList(embeddedCrowd, externalApplication));
        applicationProperties.setOption(APKeys.JIRA_OPTION_USER_CROWD_ALLOW_RENAME, true);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null,
                applicationManager, applicationProperties);
        assertTrue("canRenameUser", defaultUserManager.canRenameUser(mockUser));
    }

    @Test
    public void testCanRenameUser_internalDirectoryAndNotActingAsCrowdServer() throws Exception
    {
        final ApplicationUser mockUser = createMockApplicationUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), DirectoryType.INTERNAL, true);
        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        final ApplicationManager applicationManager = mock(ApplicationManager.class);
        final Application embeddedCrowd = mock(Application.class);
        final ApplicationProperties applicationProperties = new MockApplicationProperties();

        crowdDirectoryService.addDirectory(mockDirectory);
        when(embeddedCrowd.isPermanent()).thenReturn(true);
        when(applicationManager.findAll()).thenReturn(asList(embeddedCrowd));

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null,
                applicationManager, applicationProperties);
        assertTrue("canRenameUser", defaultUserManager.canRenameUser(mockUser));
    }

    @Test
    public void testHasPasswordWritableDirectory_none() throws Exception
    {
        final Directory readOnlyDirectory = createMockDirectory(1, null, false);
        final Directory writableDelegatedDirectory = createMockDirectory(2, DirectoryType.DELEGATING, true);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(readOnlyDirectory);
        crowdDirectoryService.addDirectory(writableDelegatedDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null, null, null);
        assertFalse("hasPasswordWritableDirectory", defaultUserManager.hasPasswordWritableDirectory());
    }

    @Test
    public void testHasPasswordWritableDirectory_some() throws Exception
    {
        final Directory readOnlyDirectory = createMockDirectory(1, null, false);
        final Directory writableDelegatedDirectory = createMockDirectory(2, DirectoryType.DELEGATING, true);
        final Directory writableDirectory = createMockDirectory(3, DirectoryType.CONNECTOR, true);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(readOnlyDirectory);
        crowdDirectoryService.addDirectory(writableDelegatedDirectory);
        crowdDirectoryService.addDirectory(writableDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null, null, null, null);
        assertTrue("hasPasswordWritableDirectory", defaultUserManager.hasPasswordWritableDirectory());
    }

    @Test
    public void testUpdateUserEmail() throws Exception
    {
        final String mail = "mail@example.com";
        final String mailWithWhiteCharacters = " " + mail + "\t";
        final User johnny = new MockUser("johnny", "John Doe", mailWithWhiteCharacters);
        final UserUtil userUtil = mock(UserUtil.class);
        new MockComponentWorker().addMock(UserUtil.class, userUtil).init();

        final MockCrowdService mockCrowdService = new MockCrowdService();
        final DefaultUserManager defaultUserManager = new DefaultUserManager(mockCrowdService, null, null, null, null, null);
        defaultUserManager.updateUser(johnny);

        final User updatedJohnny = defaultUserManager.getUser("johnny");
        verify(userUtil).clearActiveUserCount();
        assertEquals(mail, updatedJohnny.getEmailAddress());
    }

    @Test
    public void testRenameUser_simple() throws Exception
    {
        final DirectoryManager directoryManager = mock(DirectoryManager.class);
        final UserKeyStore userKeyStore = mock(UserKeyStore.class);
        final CrowdService crowdService = mock(CrowdService.class);
        final UserUtil userUtil = mock(UserUtil.class);
        final ConsumerTokenService consumerTokenService = mock(ConsumerTokenService.class);

        new MockComponentWorker()
                .addMock(UserUtil.class, userUtil)
                .addMock(ConsumerTokenService.class, consumerTokenService)
                .init();

        final ApplicationUser newFred = new MockApplicationUser("FredKey", "NewFred", "Fred Flintstone", "fred@example.com");
        when(userKeyStore.getUsernameForKey("FredKey")).thenReturn("oldfred");

        final DefaultUserManager userManager = new DefaultUserManager(crowdService, null, directoryManager, userKeyStore, null, null);
        userManager.updateUser(newFred);

        verify(directoryManager).renameUser(1L, "oldfred", "NewFred");
        verify(userKeyStore, never()).ensureUniqueKeyForNewUser("oldfred");
        verify(crowdService).updateUser(newFred.getDirectoryUser());
        verify(userUtil).clearActiveUserCount();
    }

    @Test
    public void testRenameUser_unshadow() throws Exception
    {
        final DirectoryManager directoryManager = mock(DirectoryManager.class);
        final UserKeyStore userKeyStore = mock(UserKeyStore.class);
        final CrowdService crowdService = mock(CrowdService.class);
        final UserUtil userUtil = mock(UserUtil.class);
        final ConsumerTokenService consumerTokenService = mock(ConsumerTokenService.class);
        final ApplicationUser newFred = new MockApplicationUser("FredKey", "NewFred", "Fred Flintstone", "fred@example.com");
        final User unshadowed = new ImmutableUser(2, "oldfred", "Old Fred", "oldfred@example.com", true);

        new MockComponentWorker()
                .addMock(UserUtil.class, userUtil)
                .addMock(ConsumerTokenService.class, consumerTokenService)
                .init();

        when(userKeyStore.getUsernameForKey("FredKey")).thenReturn("oldfred");
        when(crowdService.getUser("oldfred")).thenReturn(unshadowed);
        when(userKeyStore.ensureUniqueKeyForNewUser("oldfred")).thenReturn("NewKey");

        final DefaultUserManager userManager = new DefaultUserManager(crowdService, null, directoryManager, userKeyStore, null, null);
        userManager.updateUser(newFred);

        verify(directoryManager).renameUser(1L, "oldfred", "NewFred");
        verify(userKeyStore).ensureUniqueKeyForNewUser("oldfred");
        verify(crowdService).updateUser(newFred.getDirectoryUser());
        verify(userUtil).clearActiveUserCount();
    }

    @Test
    public void testRenameUser_exists() throws Exception
    {
        final DirectoryManager directoryManager = mock(DirectoryManager.class);
        final UserKeyStore userKeyStore = mock(UserKeyStore.class);
        final CrowdService crowdService = mock(CrowdService.class);
        final UserUtil userUtil = mock(UserUtil.class);
        new MockComponentWorker().addMock(UserUtil.class, userUtil).init();

        when(userKeyStore.getUsernameForKey("FredKey")).thenReturn("oldfred");
        when(crowdService.getUser("newfred")).thenReturn(new MockUser("NewFred", "Some Other Fred", "someone@example.com"));

        final ApplicationUser newFred = new MockApplicationUser("FredKey", "NewFred", "Fred Flintstone", "fred@example.com");
        final DefaultUserManager userManager = new DefaultUserManager(crowdService, null, directoryManager, userKeyStore, null, null);
        try
        {
            userManager.updateUser(newFred);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            // expected
        }

        verify(crowdService).getUser("newfred");
        verify(userKeyStore).getUsernameForKey("FredKey");
        verifyNoMoreInteractions(crowdService, userKeyStore, directoryManager, userUtil);
    }

    @Test
    public void testRenameUser_evictDeleted() throws Exception
    {
        final DirectoryManager directoryManager = mock(DirectoryManager.class);
        final UserKeyStore userKeyStore = mock(UserKeyStore.class);
        final CrowdService crowdService = mock(CrowdService.class);
        final UserUtil userUtil = mock(UserUtil.class);
        final MockComponentWorker mockComponentWorker = new MockComponentWorker();
        final ConsumerTokenService consumerTokenService = mock(ConsumerTokenService.class);
        mockComponentWorker
                .addMock(UserUtil.class, userUtil)
                .addMock(ConsumerTokenService.class, consumerTokenService)
                .init();

        final ApplicationUser newFred = new MockApplicationUser("FredKey", "NewFred", "Fred Flintstone", "fred@example.com");
        when(userKeyStore.getUsernameForKey("FredKey")).thenReturn("oldfred");
        when(userKeyStore.getKeyForUsername("newfred")).thenReturn("Deleted user (otherwise crowdService.getUser would have returned it)");
        when(userKeyStore.getKeyForUsername("newfred#1")).thenReturn("Doesn't matter if these really still exist or not");
        when(userKeyStore.getKeyForUsername("newfred#2")).thenReturn("Same here");

        final DefaultUserManager userManager = new DefaultUserManager(crowdService, null, directoryManager, userKeyStore, null, null);
        userManager.updateUser(newFred);

        verify(crowdService).getUser("newfred");
        verify(crowdService).getUser("oldfred");
        verify(userKeyStore).getUsernameForKey("FredKey");
        verify(userKeyStore).getKeyForUsername("newfred");
        verify(userKeyStore).getKeyForUsername("newfred#1");
        verify(userKeyStore).getKeyForUsername("newfred#2");
        verify(userKeyStore).getKeyForUsername("newfred#3");
        verify(userKeyStore).renameUser("newfred", "newfred#3");
        // verify(userKeyStore).renameUser("oldfred", "newfred");  // would be invoked indirectly by...
        verify(directoryManager).renameUser(1L, "oldfred", "NewFred");
        verify(crowdService).updateUser(newFred.getDirectoryUser());
        verify(userUtil).clearActiveUserCount();

        verifyNoMoreInteractions(userKeyStore, directoryManager, crowdService, userUtil);
    }

    @Test
    public void testGetUsers() throws Exception
    {
        final CrowdDirectoryService crowdDirectoryService = mock(CrowdDirectoryService.class);
        final Directory dir1 = directory(1L, true);
        final Directory dir2 = directory(2L, false);
        final Directory dir3 = directory(3L, true);
        final Directory dir4 = directory(4L, true);
        when(crowdDirectoryService.findAllDirectories()).thenReturn(asList(dir1, dir2, dir3, dir4));

        final User fred1 = user(1L, "fred");
        final User fred2 = user(2L, "fred");
        final User ginny2 = user(2L, "ginny");
        final User george2 = user(2L, "george");
        final User george3 = user(3L, "george");
        final User harry3 = user(3L, "harry");
        final User harry4 = user(4L, "harry");
        final User ron3 = user(3L, "ron");
        final User hermione4 = user(4L, "hermione");
        final User[] expectedUsers = new User[] { fred1, george3, harry3, ron3, hermione4 };

        final DirectoryManager directoryManager = mock(DirectoryManager.class);
        mockUsersInDirectory(directoryManager, 1L, fred1);
        mockUsersInDirectory(directoryManager, 2L, fred2, ginny2, george2);
        mockUsersInDirectory(directoryManager, 3L, george3, harry3, ron3);
        mockUsersInDirectory(directoryManager, 4L, harry4, hermione4);

        final DefaultUserManager userManager = new DefaultUserManager(null, crowdDirectoryService, directoryManager, null, null, null);
        assertThat(userManager.getUsers(), Matchers.containsInAnyOrder(expectedUsers));
        assertThat(userManager.getAllUsers(), Matchers.containsInAnyOrder(expectedUsers));
        assertEquals(expectedUsers.length, userManager.getTotalUserCount());

        //noinspection unchecked
        verify(directoryManager, never()).searchUsers(eq(2L), any(UserQuery.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUserState_NullUsername() throws Exception
    {
        final DefaultUserManager userManager = new DefaultUserManager(null, null, null, null, null, null);
        //noinspection ConstantConditions
        assertEquals(UserState.INVALID_USER, userManager.getUserState(null, 1L));
    }

    @Test
    public void testGetUserState_InvalidUserFromDirectoryIdMinus1() throws Exception
    {
        final DefaultUserManager userManager = new DefaultUserManager(null, null, null, null, null, null);
        assertEquals(UserState.INVALID_USER, userManager.getUserState("fred", -1L));
    }

    @Test
    public void testGetUserState_InvalidUserFromExhaustiveSearch() throws Exception
    {
        final CrowdDirectoryService crowdDirectoryService = mock(CrowdDirectoryService.class);
        final Directory dir1 = directory(1L, true);
        final Directory dir2 = directory(2L, false);
        final Directory dir3 = directory(3L, true);
        when(crowdDirectoryService.findAllDirectories()).thenReturn(asList(dir1, dir2, dir3));

        final DirectoryManager directoryManager = mock(DirectoryManager.class);
        final UserUtil userUtil = mock(UserUtil.class);
        final MockComponentWorker mockComponentWorker = new MockComponentWorker();
        final ConsumerTokenService consumerTokenService = mock(ConsumerTokenService.class);
        mockComponentWorker
                .addMock(UserUtil.class, userUtil)
                .addMock(ConsumerTokenService.class, consumerTokenService)
                .init();

        final DefaultUserManager userManager = new DefaultUserManager(null, crowdDirectoryService, directoryManager, null, null, null);
        assertEquals(UserState.INVALID_USER, userManager.getUserState("fred", 4L));

        verify(directoryManager).findUserByName(1L, "fred");
        verify(directoryManager, never()).findUserByName(2L, "fred");
        verify(directoryManager).findUserByName(3L, "fred");
    }

    @Test
    public void testGetUserState_InvalidUserFromDisabledDirectoryEvenWhenFoundInOtherFirst() throws Exception
    {
        final CrowdDirectoryService crowdDirectoryService = mock(CrowdDirectoryService.class);
        final Directory dir1 = directory(1L, true);
        final Directory dir2 = directory(2L, false);
        final Directory dir3 = directory(3L, true);
        when(crowdDirectoryService.findAllDirectories()).thenReturn(asList(dir1, dir2, dir3));

        final DirectoryManager directoryManager = mock(DirectoryManager.class);
        mockUserInDirectory(directoryManager, 1L, "fred");

        final DefaultUserManager userManager = new DefaultUserManager(null, crowdDirectoryService, directoryManager, null, null, null);
        assertEquals(UserState.INVALID_USER, userManager.getUserState("fred", 2L));

        verify(directoryManager).findUserByName(1L, "fred");
        verify(directoryManager, never()).findUserByName(2L, "fred");
        verify(directoryManager, never()).findUserByName(3L, "fred");
    }

    @Test
    public void testGetUserState_IsShadowed() throws Exception
    {
        final CrowdDirectoryService crowdDirectoryService = mock(CrowdDirectoryService.class);
        final Directory dir1 = directory(1L, true);
        final Directory dir2 = directory(2L, false);
        final Directory dir3 = directory(3L, true);
        final Directory dir4 = directory(4L, true);
        final Directory dir5 = directory(5L, true);
        when(crowdDirectoryService.findAllDirectories()).thenReturn(asList(dir1, dir2, dir3, dir4, dir5));

        final DirectoryManager directoryManager = mock(DirectoryManager.class);
        mockUserInDirectory(directoryManager, 1L, "fred");
        mockUserInDirectory(directoryManager, 4L, "fred");

        final DefaultUserManager userManager = new DefaultUserManager(null, crowdDirectoryService, directoryManager, null, null, null);
        assertEquals(UserState.SHADOW_USER, userManager.getUserState("fred", 4L));

        verify(directoryManager).findUserByName(1L, "fred");
        verify(directoryManager, never()).findUserByName(2L, "fred");
        verify(directoryManager, never()).findUserByName(3L, "fred");
        verify(directoryManager).findUserByName(4L, "fred");
        verify(directoryManager, never()).findUserByName(5L, "fred");
    }

    @Test
    public void testGetUserState_HasShadow() throws Exception
    {
        final CrowdDirectoryService crowdDirectoryService = mock(CrowdDirectoryService.class);
        final Directory dir1 = directory(1L, true);
        final Directory dir2 = directory(2L, false);
        final Directory dir3 = directory(3L, true);
        final Directory dir4 = directory(4L, true);
        final Directory dir5 = directory(5L, true);
        when(crowdDirectoryService.findAllDirectories()).thenReturn(asList(dir1, dir2, dir3, dir4, dir5));

        final DirectoryManager directoryManager = mock(DirectoryManager.class);
        mockUserInDirectory(directoryManager, 1L, "fred");
        mockUserInDirectory(directoryManager, 4L, "fred");

        final DefaultUserManager userManager = new DefaultUserManager(null, crowdDirectoryService, directoryManager, null, null, null);
        assertEquals(UserState.NORMAL_USER_WITH_SHADOW, userManager.getUserState("fred", 1L));

        verify(directoryManager).findUserByName(1L, "fred");
        verify(directoryManager, never()).findUserByName(2L, "fred");
        verify(directoryManager).findUserByName(3L, "fred");
        verify(directoryManager).findUserByName(4L, "fred");
        verify(directoryManager, never()).findUserByName(5L, "fred");
    }



    private User createMockUser()
    {
        return new MockUser("user1");
    }

    private ApplicationUser createMockApplicationUser()
    {
        return new MockApplicationUser("User1");
    }

    private Directory createMockDirectory(final long id, final DirectoryType type, final boolean writable)
    {
        final ImmutableDirectory.Builder builder = ImmutableDirectory.newBuilder();
        builder.setId(id);

        if (writable)
        {
            final Set<OperationType> allowedOperations = new HashSet<OperationType>();
            allowedOperations.add(OperationType.CREATE_USER);
            allowedOperations.add(OperationType.UPDATE_USER);
            builder.setAllowedOperations(allowedOperations);
        }

        builder.setType(type);

        return builder.toDirectory();
    }

    private User user(final long directoryId, final String name)
    {
        return new ImmutableUser(directoryId, name, name, name + "@example.com", true)
        {
            @Override
            public String toString()
            {
                return name + ':' + directoryId;
            }
        };
    }

    private void mockUserInDirectory(final DirectoryManager directoryManager, long directoryId, String userName) throws Exception
    {
        final com.atlassian.crowd.model.user.User user = mock(com.atlassian.crowd.model.user.User.class);
        when(user.getDirectoryId()).thenReturn(directoryId);
        when(user.getName()).thenReturn(userName);

        // when(...).thenReturn(...) form doesn't work here due to the mismatched User types
        doReturn(user).when(directoryManager).findUserByName(directoryId, userName);
    }

    private void mockUsersInDirectory(final DirectoryManager directoryManager, long directoryId, User... users) throws Exception
    {
        //noinspection unchecked
        when(directoryManager.searchUsers(eq(directoryId), any(UserQuery.class))).thenReturn(asList(users));

        for (User user : users)
        {
            mockUserInDirectory(directoryManager, directoryId, user.getName());
        }
    }

    private Directory directory(long id, boolean active)
    {
        final Directory dir = mock(Directory.class);
        when(dir.getId()).thenReturn(id);
        when(dir.isActive()).thenReturn(active);
        return dir;
    }
}
