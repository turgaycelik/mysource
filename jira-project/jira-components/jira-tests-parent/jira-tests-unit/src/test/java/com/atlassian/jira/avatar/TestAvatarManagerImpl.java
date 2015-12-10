package com.atlassian.jira.avatar;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AbstractJiraHome;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.util.TempDirectoryUtil;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.avatar.Avatar.Type.ISSUETYPE;
import static com.atlassian.jira.avatar.Avatar.Type.PROJECT;
import static com.atlassian.jira.avatar.Avatar.Type.USER;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link com.atlassian.jira.avatar.AvatarManagerImpl}.
 *
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAvatarManagerImpl
{
    private static final ApplicationUser ANONYMOUS_APP_USER = null;
    private static final String FILENAME_STUB = "filename-stub";
    private static final String AVATAR_SUBJECT = "subject1";

    @Mock AvatarStore avatarStore;

    ApplicationUser fredAppUser = new MockApplicationUser("fred", "Fred", "Fred Flintstone", "fred@example.com");
    ApplicationUser adminAppUser = new MockApplicationUser("admin", "Admin", "Admin I. Strator", "admin@example.com");
    ApplicationUser projAdminAppUser = new MockApplicationUser("projadmin", "ProjAdmin", "Project Admin", "projadmin@example.com");
    User fred = fredAppUser.getDirectoryUser();
    User admin = adminAppUser.getDirectoryUser();
    User projAdmin = projAdminAppUser.getDirectoryUser();

    AvatarTagger avatarTagger;

    @After
    public void tearDown()
    {
        fred = null;
        admin = null;
        projAdmin = null;
        fredAppUser = null;
        adminAppUser = null;
        projAdminAppUser = null;
        avatarStore = null;
        avatarTagger = null;
    }



    @Test
    public void testGetById()
    {
        final Avatar avatar = new AvatarImpl(null, "foo", "mime/type", PROJECT, "otto", false);
        when(avatarStore.getById(1001L)).thenReturn(avatar);

        final AvatarManagerImpl avatarManager = new AvatarManagerImpl(avatarStore, null, null, null, avatarTagger);
        assertThat(avatarManager.getById(1001L), is(avatar));
    }

    @Test
    public void testCreate()
    {
        final Avatar avatar1 = new AvatarImpl(null, "foo", "mime/type", PROJECT, "otto", false);
        final Avatar avatar2 = new AvatarImpl(null, "foo2", "mime/type", PROJECT, null, true);
        when(avatarStore.create(avatar1)).thenReturn(avatar2);

        final AvatarManagerImpl avatarManager = new AvatarManagerImpl(avatarStore, null, null, null, avatarTagger);
        assertThat(avatarManager.create(avatar1), is(avatar2));
    }

    @Test
    public void testCreateSad() throws IOException
    {
        final Avatar system = new AvatarImpl(null, "foo2", "mime/type", PROJECT, null, true);
        final AvatarManagerImpl avatarManager = new AvatarManagerImpl(null, null, null, null, avatarTagger);

        try
        {
            avatarManager.create(system, null, null);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            assertThat(expected.getMessage().toLowerCase(), containsString("system avatars"));
        }
    }

    @Test
    public void shouldCreateInStoreNonsystemAvatarWithPassedParameters() throws IOException
    {
        // given
        final ArgumentCaptor<Avatar> avatarArgumentCaptor = ArgumentCaptor.forClass(Avatar.class);
        final AvatarImageDataStorage imageDataStore = mock(AvatarImageDataStorage.class);
        when(imageDataStore.getNextFilenameStub()).thenReturn(FILENAME_STUB);
        final AvatarManagerImpl avatarManager = new AvatarManagerImpl(avatarStore, null, null, null, avatarTagger, imageDataStore);
        final AvatarImageDataProvider imageDataSource = mock(AvatarImageDataProvider.class);

        // when
        avatarManager.create(ISSUETYPE, AVATAR_SUBJECT, imageDataSource);

        // then
        verify(avatarStore).create(avatarArgumentCaptor.capture());

        final Avatar value = avatarArgumentCaptor.getValue();
        assertThat(value.getId(), nullValue());
        assertThat(value.getAvatarType(), is(ISSUETYPE));
        assertThat(value.getContentType(), is("image/png"));
        assertThat(value.getOwner(), is(AVATAR_SUBJECT));
        assertThat(value.isSystemAvatar(), is(false));
        assertThat(value.getFileName(), is(FILENAME_STUB + ".png"));
    }

    @Test
    public void shouldPassNewlyCreatedAvatarToImageStore() throws IOException
    {
        // given
        final ArgumentCaptor<Avatar> avatarArgumentCaptor = ArgumentCaptor.forClass(Avatar.class);
        final AvatarImageDataStorage imageDataStore = mock(AvatarImageDataStorage.class);
        when(imageDataStore.getNextFilenameStub()).thenReturn(FILENAME_STUB);
        final Avatar newlyCreatedAvatar = mock(Avatar.class);
        when(avatarStore.create(any(Avatar.class))).thenReturn(newlyCreatedAvatar);

        AvatarManagerImpl avatarManager = new AvatarManagerImpl(avatarStore, null, null, null, avatarTagger, imageDataStore);
        final AvatarImageDataProvider imageDataSource = mock(AvatarImageDataProvider.class);

        // when
        avatarManager.create(ISSUETYPE, AVATAR_SUBJECT, imageDataSource);

        // then
        verify(imageDataStore).storeAvatarFiles(refEq(newlyCreatedAvatar), refEq(imageDataSource));
    }

    @Test
    public void testUpdate()
    {
        final Avatar avatar = new AvatarImpl(99L, "foo", "mime/type", PROJECT, "otto", false);
        final AvatarManagerImpl avatarManager = new AvatarManagerImpl(avatarStore, null, null, null, avatarTagger);

        avatarManager.update(avatar);

        verify(avatarStore).update(avatar);
    }

    @Test
    public void testDelete()
    {
        final Avatar avatar = new AvatarImpl(6543L, "foo", "mime/type", PROJECT, "otto", false);
        when(avatarStore.getById(6543L)).thenReturn(avatar);
        when(avatarStore.delete(6543L)).thenReturn(true);

        final AvatarManagerImpl avatarManager = new AvatarManagerImpl(avatarStore, null, null, null, avatarTagger);
        final boolean result = avatarManager.delete(6543L, false);

        verify(avatarStore).getById(6543L);
        verify(avatarStore).delete(6543L);
        assertThat("Return value", result, is(true));
   }

    @Test
    public void testGetAllSystemAvatars()
    {
        final List<Avatar> systemAvatars = ImmutableList.<Avatar>of(
                AvatarImpl.createSystemAvatar("foo", "mime/type", PROJECT),
                AvatarImpl.createSystemAvatar("foo2", "mime/type", PROJECT) );
        when(avatarStore.getAllSystemAvatars(PROJECT)).thenReturn(systemAvatars);

        final AvatarManager avatarManager = new AvatarManagerImpl(avatarStore, null, null, null, avatarTagger);
        assertThat(avatarManager.getAllSystemAvatars(PROJECT), equalTo(systemAvatars));
    }

    @Test
    public void testGetCustomAvatarsForOwner()
    {
        final List<Avatar> customAvatars = ImmutableList.<Avatar>of(
                AvatarImpl.createSystemAvatar("foo", "mime/type", PROJECT),
                AvatarImpl.createSystemAvatar("foo2", "mime/type", PROJECT) );
        when(avatarStore.getCustomAvatarsForOwner(PROJECT, "skywalker")).thenReturn(customAvatars);

        final AvatarManager avatarManager = new AvatarManagerImpl(avatarStore, null, null, null, avatarTagger);
        assertThat(avatarManager.getCustomAvatarsForOwner(PROJECT, "skywalker"), equalTo(customAvatars));
    }

    @Test
    public void testCreateAvatarFile() throws IOException
    {
        final JiraHome jiraHome = new SimpleJiraHome("func_tests_jira_home");
        final AvatarManagerImpl avatarManager = new AvatarManagerImpl(null, jiraHome, null, null, avatarTagger);
        final Avatar avatar = new AvatarImpl(123L, "filename", "image/png", PROJECT, "owner", false);
        final File avatarFile = avatarManager.createAvatarFile(avatar, "");

        assertThat("isDirectory", avatarFile.isDirectory(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithStream() throws IOException
    {
        final JiraHome jiraHome = new SimpleJiraHome("func_tests_jira_home");
        final AvatarManager avatarManager = new AvatarManagerImpl(null, jiraHome, null, null, avatarTagger);
        final ByteArrayInputStream imageData = new ByteArrayInputStream("foo".getBytes("UTF-8"));
        avatarManager.create(null, imageData, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSystemAvatarWithStream() throws IOException
    {
        final JiraHome jiraHome = new SimpleJiraHome("func_tests_jira_home");
        final AvatarManager avatarManager = new AvatarManagerImpl(null, jiraHome, null, null, avatarTagger);
        final ByteArrayInputStream imageData = new ByteArrayInputStream("foo".getBytes("UTF-8"));
        avatarManager.create(new AvatarImpl(null, "foo2", "mime/type", PROJECT, "otto2", true), imageData, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAvatarWithNullStream() throws IOException
    {
        final JiraHome jiraHome = new SimpleJiraHome("func_tests_jira_home");
        final AvatarManager avatarManager = new AvatarManagerImpl(null, jiraHome, null, null, avatarTagger);
        avatarManager.create(new AvatarImpl(null, "foo2", "mime/type", PROJECT, "otto2", false), null, null);
    }

    @Test
    public void testCreateCustomAvatarWithStream() throws IOException
    {
        final Avatar avatar = new AvatarImpl(null, "foo2", "mime/type", PROJECT, "otto2", false);
        when(avatarStore.create(avatar)).thenReturn(avatar);

        final JiraHome jiraHome = new SimpleJiraHome("func_tests_jira_home");
        final AvatarManager avatarManager = new AvatarManagerImpl(avatarStore, jiraHome, null, null, avatarTagger)
        {
            @Override
            File processImage(final Avatar created, final InputStream imageData, final Selection croppingSelection,
                    final ImageSize size) throws IOException
            {
                final File tempFile = File.createTempFile("nothing", ".empty");
                tempFile.deleteOnExit();
                return tempFile;
            }
        };

        final ByteArrayInputStream imageData = new ByteArrayInputStream("foo".getBytes("UTF-8"));
        final Avatar avatarCreated = avatarManager.create(avatar, imageData, null);
        IOUtil.shutdownStream(imageData);
        assertThat(avatarCreated, is(sameInstance(avatar)));
    }

    @Test
    public void testProcessAvatarDataWillGenerateImageWhenImageDoesntExist() throws IOException
    {
        final AtomicBoolean processImageCalled = new AtomicBoolean(false);
        final File avData = File.createTempFile("TestAvatarManagerImpl.java", "testProcessAvatarData");
        createFile(avData, "imageData");

        final AvatarImpl avatar = new AvatarImpl(null, "foo2", "mime/type", PROJECT, "otto2", false);
        final AvatarManager.ImageSize testSize = AvatarManager.ImageSize.XLARGE;
        final List<String> checkedSizes = new ArrayList<String>(4);

        AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, null, avatarTagger)
        {
            @Override
            public File getAvatarFile(Avatar av, String sizeFlag)
            {
                checkedSizes.add(sizeFlag);
                if (sizeFlag.equals(testSize.getFilenameFlag()))
                {
                    return new File(sizeFlag)
                    {
                        @Override
                        public boolean exists()
                        {
                            return false;
                        }
                    };
                }
                else
                {
                    return avData;
                }
            }

            @Override
            File processImage(Avatar created, InputStream imageData, Selection croppingSelection, ImageSize size)
                    throws IOException
            {
                processImageCalled.set(true);
                final File tempFile = File.createTempFile("nothing", ".empty");
                tempFile.deleteOnExit();
                return tempFile;
            }
        };
        final AtomicBoolean consumeCalled = new AtomicBoolean(false);
        final Consumer<InputStream> mockConsumer = new Consumer<InputStream>()
        {
            public void consume(@Nonnull final InputStream in)
            {
                consumeCalled.set(true);
            }
        };
        am.processAvatarData(avatar, mockConsumer, testSize);
        assertTrue("Our larger image is read from", consumeCalled.get());
        assertTrue("The image was processed to generate a new file", processImageCalled.get());
        assertThat(checkedSizes, hasItem(testSize.getFilenameFlag()));
    }

    @Test
    public void testProcessAvatarData() throws IOException
    {
        final File avData = File.createTempFile("TestAvatarManagerImpl.java", "testProcessAvatarData");
        createFile(avData, "imageData");

        final AvatarImpl avatar = new AvatarImpl(null, "foo2", "mime/type", PROJECT, "otto2", false);
        final AtomicBoolean getAvatarFileCalled = new AtomicBoolean(false);

        final AvatarManagerImpl avatarManager = new AvatarManagerImpl(null, null, null, null, avatarTagger)
        {
            @Override
            public File getAvatarFile(Avatar av, String sizeFlag)
            {
                assertEquals(avatar, av);
                assertEquals("medium_", sizeFlag);
                getAvatarFileCalled.set(true);
                return avData;
            }
        };
        final AtomicBoolean consumeCalled = new AtomicBoolean(false);
        final Consumer<InputStream> mockConsumer = new Consumer<InputStream>()
        {
            public void consume(@Nonnull final InputStream in)
            {
                assertStreamEqualsString("imageData", in);
                consumeCalled.set(true);
            }
        };
        avatarManager.processAvatarData(avatar, mockConsumer, AvatarManager.ImageSize.MEDIUM);
        assertThat("consumeCalled", consumeCalled.get(), is(true));
    }

    @Test
    public void testProcessAvatarDataSystem() throws IOException
    {
        final InputStream bogus = new ByteArrayInputStream("imageData".getBytes());

        final AvatarImpl avatar = new AvatarImpl(null, "foo2", "mime/type", PROJECT, null, true);
        AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, null, avatarTagger)
        {
            @Override
            InputStream getClasspathStream(final String path)
            {
                return bogus;
            }
        };
        final AtomicBoolean consumeCalled = new AtomicBoolean(false);
        final Consumer<InputStream> mockConsumer = new Consumer<InputStream>()
        {
            public void consume(@Nonnull final InputStream in)
            {
                assertStreamEqualsString("imageData", in);
                consumeCalled.set(true);
            }
        };
        am.processAvatarData(avatar, mockConsumer, AvatarManager.ImageSize.MEDIUM);
        assertThat("consumeCalled", consumeCalled.get(), is(true));
    }

    @Test
    public void testProcessAvatarDataSystemNotFound() throws IOException
    {
        final AvatarImpl avatar = new AvatarImpl(null, "foo2", "mime/type", PROJECT, null, true);
        AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, null, avatarTagger)
        {
            @Override
            InputStream getClasspathStream(final String path)
            {
                return null;
            }
        };

        try
        {
            am.processAvatarData(avatar, null, AvatarManager.ImageSize.MEDIUM);
            fail("expected IOException because the classpath avatar data was null");
        }
        catch (IOException yay)
        {
            assertThat(yay.getMessage(), containsString("File not found"));
        }
    }

    @Test
    public void testGetDefaultAvatarId()
    {
        final long ISSUE_TYPE_AVATAR_ID = 5555;

        final ApplicationProperties mockApplicationProperties = mock(ApplicationProperties.class);
        when(mockApplicationProperties.getString("jira.avatar.default.id")).thenReturn("3423");
        when(mockApplicationProperties.getString("jira.avatar.user.default.id")).thenReturn("4444");
        when(mockApplicationProperties.getString(APKeys.JIRA_DEFAULT_ISSUETYPE_AVATAR_ID)).thenReturn(String.valueOf(ISSUE_TYPE_AVATAR_ID));

        final AvatarManagerImpl am = new AvatarManagerImpl(null, null, mockApplicationProperties, null, avatarTagger);
        final Long id = am.getDefaultAvatarId(Avatar.Type.PROJECT);
        assertEquals(new Long(3423), id);
        final Long userId = am.getDefaultAvatarId(USER);
        assertEquals(new Long(4444), userId);
        final long defaultIssueTypeAvatar = am.getDefaultAvatarId(ISSUETYPE);
        assertThat(defaultIssueTypeAvatar, equalTo(ISSUE_TYPE_AVATAR_ID));
    }

    @Test
    public void testHasPermissionToViewProject()
    {
        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        final ProjectManager mockProjectManager = mock(ProjectManager.class);
        final Project mockProject = new MockProject(10012L);
        final Project mockProject2 = new MockProject(10022L);

        when(mockProjectManager.getProjectObj(10012L)).thenReturn(mockProject);
        when(mockProjectManager.getProjectObj(10022L)).thenReturn(mockProject2);

        when(mockPermissionManager.hasPermission(0, adminAppUser)).thenReturn(true);
        when(mockPermissionManager.hasPermission(10, mockProject, ANONYMOUS_APP_USER)).thenReturn(true);
        when(mockPermissionManager.hasPermission(23, mockProject2, projAdminAppUser)).thenReturn(true);

        final AvatarManagerImpl avatarManager = new AvatarManagerImpl(null, null, null, mockPermissionManager, avatarTagger)
        {
            @Override
            ProjectManager getProjectManager()
            {
                return mockProjectManager;
            }
        };

        assertThat(avatarManager, canView(null, PROJECT, "10012"));
        assertThat(avatarManager, not(canView(fred, PROJECT, "10022")));
        assertThat(avatarManager, not(canView(fred, PROJECT, "INVALIDPROJECTID")));
        assertThat(avatarManager, not(canView(fred, PROJECT, null)));
        assertThat(avatarManager, canView(admin, PROJECT, "10012"));
        assertThat(avatarManager, canView(admin, PROJECT, "10022"));
        assertThat(avatarManager, not(canView(projAdmin, PROJECT, "10012")));
        assertThat(avatarManager, canView(projAdmin, PROJECT, "10022"));
    }

    @Test
    public void testHasPermissionToViewUser()
    {
        final UserManager userManager = mock(UserManager.class);
        when(userManager.getUserByKey("admin")).thenReturn(adminAppUser);
        when(userManager.getUserByKey("fred")).thenReturn(fredAppUser);

        final PermissionManager permissionManager = mock(PermissionManager.class);
        when(permissionManager.hasPermission(1, fredAppUser)).thenReturn(true);
        when(permissionManager.hasPermission(1, ANONYMOUS_APP_USER)).thenReturn(false);

        new MockComponentWorker()
                .addMock(UserManager.class, userManager)
                .addMock(PermissionManager.class, permissionManager)
                .init();

        final AvatarManager avatarManager = new AvatarManagerImpl(null, null, null, permissionManager, avatarTagger);
        assertThat(avatarManager, not(canView(null, USER, "admin")));
        assertThat(avatarManager, canView(fred, USER, null));
        assertThat(avatarManager, canView(fred, USER, "fred"));
        assertThat(avatarManager, canView(fred, USER, "admin"));

        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testHasPermissionToEditUser()
    {
        final UserManager userManager = mock(UserManager.class);
        when(userManager.getUserByKey("admin")).thenReturn(adminAppUser);
        when(userManager.getUserByKey("fred")).thenReturn(fredAppUser);

        final PermissionManager permissionManager = mock(PermissionManager.class);
        when(permissionManager.hasPermission(0, adminAppUser)).thenReturn(true);

        new MockComponentWorker()
                .addMock(UserManager.class, userManager)
                .addMock(PermissionManager.class, permissionManager)
                .init();

        final AvatarManager avatarManager = new AvatarManagerImpl(null, null, null, permissionManager, avatarTagger);
        assertThat(avatarManager, not(canEdit(null, USER, "admin")));
        assertThat(avatarManager, not(canEdit(fred, USER, null)));
        assertThat(avatarManager, canEdit(fred, USER, "fred"));
        assertThat(avatarManager, not(canEdit(fred, USER, "admin")));
        assertThat(avatarManager, canEdit(admin, USER, "fred"));

        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testHasPermissionToEditProject()
    {
        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        final ProjectManager mockProjectManager = mock(ProjectManager.class);

        final Project mockProject = new MockProject(10012L);
        final Project mockProject2 = new MockProject(10022L);

        when(mockProjectManager.getProjectObj(10012L)).thenReturn(mockProject);
        when(mockProjectManager.getProjectObj(10022L)).thenReturn(mockProject2);
        when(mockPermissionManager.hasPermission(0, adminAppUser)).thenReturn(true);
        when(mockPermissionManager.hasPermission(23, mockProject2, projAdminAppUser)).thenReturn(true);

        final AvatarManagerImpl avatarManager = new AvatarManagerImpl(null, null, null, mockPermissionManager, avatarTagger)
        {
            @Override
            ProjectManager getProjectManager()
            {
                return mockProjectManager;
            }
        };

        assertThat(avatarManager, not(canEdit(null, PROJECT, "10012")));
        assertThat(avatarManager, not(canEdit(fred, PROJECT, "10022")));
        assertThat(avatarManager, not(canEdit(fred, PROJECT, "INVALIDPROJECTID")));
        assertThat(avatarManager, not(canEdit(fred, PROJECT, null)));
        assertThat(avatarManager, canEdit(admin, PROJECT, "10012"));
        assertThat(avatarManager, canEdit(admin, PROJECT, "10022"));
        assertThat(avatarManager, not(canEdit(projAdmin, PROJECT, "10012")));
        assertThat(avatarManager, canEdit(projAdmin, PROJECT, "10022"));
    }

    @Test
    public void testGetAvatarDirectory()
    {
        final JiraHome jiraHome = new SimpleJiraHome("localHomePath", "sharedHomePath");
        final AvatarManager avatarManager = new AvatarManagerImpl(null, jiraHome, null, null, avatarTagger);
        final File dir = avatarManager.getAvatarBaseDirectory();

        assertEquals(jiraHome.getHomePath() + "/data/avatars", dir.getAbsolutePath());
    }


    private void assertStreamEqualsString(final String expectedContents, final InputStream in)
    {
        try
                {
                    assertEquals(expectedContents, IOUtil.toString(in));

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void createFile(File file, String contents) throws IOException
    {
        final FileWriter writer = new FileWriter(file);
        try
        {
            writer.append(contents);
        }
        finally
        {
            writer.close();
        }
    }

    private static class SimpleJiraHome extends AbstractJiraHome
    {
        private final String name;
        private final String localName;
        private File tempFile;

        private SimpleJiraHome(final String name)
        {
            this.name = name;
            this.localName = name;
        }

        private SimpleJiraHome(final String name, final String localName)
        {
            this.name = name;
            this.localName = localName;
        }

        @Nonnull
        public File getHome()
        {
            return getDir(name);
        }

        private File getDir(String aName)
        {
            if (tempFile != null)
            {
                return tempFile;
            }

            final File file = TempDirectoryUtil.createTempDirectory(aName);
            if (!file.exists())
            {
                assertTrue(file.mkdir());
            }
            file.deleteOnExit();
            tempFile = file;
            return file;
        }

        @Nonnull
        @Override
        public File getLocalHome()
        {
            return getDir(localName);
        }
    }



    static HasPermissionMatcher canEdit(final User user, final Avatar.Type type, final String target)
    {
        return new HasPermissionMatcher(user, type, target, true);
    }

    static HasPermissionMatcher canView(final User user, final Avatar.Type type, final String target)
    {
        return new HasPermissionMatcher(user, type, target, false);
    }

    static class HasPermissionMatcher extends TypeSafeMatcher<AvatarManager>
    {
        private final User user;
        private final Avatar.Type type;
        private final String target;
        private final boolean edit;

        HasPermissionMatcher(final User user, final Avatar.Type type, final String target, final boolean edit)
        {
            this.user = user;
            this.type = type;
            this.target = target;
            this.edit = edit;
        }

        @Override
        protected boolean matchesSafely(final AvatarManager avatarManager)
        {
            if (edit)
            {
                return avatarManager.hasPermissionToEdit(user, type, target);
            }
            return avatarManager.hasPermissionToView(user, type, target);
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("hasPermissionTo")
                    .appendText(edit ? "Edit" : "View")
                    .appendText("(user=")
                    .appendValue(user)
                    .appendText(",type=")
                    .appendValue(type)
                    .appendText(",target=")
                    .appendText(target)
                    .appendText(")");
        }
    }
}
