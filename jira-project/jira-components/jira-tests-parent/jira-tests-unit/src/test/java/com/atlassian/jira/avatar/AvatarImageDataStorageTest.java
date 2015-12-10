package com.atlassian.jira.avatar;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.atlassian.core.util.FileUtils;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;

import com.google.common.io.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AvatarImageDataStorageTest
{
    private final Answer writeRequestedSizeToOutput = new Answer()
    {
        @Override
        public Object answer(final InvocationOnMock invocation) throws Throwable
        {
            Avatar.Size size = (Avatar.Size) invocation.getArguments()[0];
            OutputStream stream = (OutputStream) invocation.getArguments()[1];
            stream.write(size.getParam().getBytes());

            return null;
        }
    };
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @InjectMocks
    private AvatarImageDataStorage testObj;
    @Mock
    private JiraHome jiraHome;
    @Mock
    private AvatarImageDataProvider imageDateProvider;

    private File fakeAvatarsDir;
    private AvatarImpl mockAvatar;


    @Before
    public void setUp() throws Exception
    {
        testObj = mock(AvatarImageDataStorage.class);
        File fakeHome = Files.createTempDir();
        when(jiraHome.getHome()).thenReturn(fakeHome);

        fakeAvatarsDir = new File(fakeHome, AvatarImageDataStorage.AVATAR_DIRECTORY);
        FileUtils.recursiveDelete(fakeAvatarsDir);

        testObj = new AvatarImageDataStorage(jiraHome);
        mockAvatar = new AvatarImpl(0l, "filename.txt", "image/png", Avatar.Type.ISSUETYPE, "owner", false);
    }

    @After
    public void tearDown() {
        FileUtils.recursiveDelete(fakeAvatarsDir);
    }

    @Test (expected = IOException.class)
    public void shouldCleanAllFilesWhenImageProviderThrowsException() throws Exception
    {
        Mockito.doAnswer(writeRequestedSizeToOutput).when(imageDateProvider).storeImage(Mockito.any(Avatar.Size.class), Mockito.any(OutputStream.class));
        Mockito.doThrow(IOException.class).when(imageDateProvider).storeImage(Mockito.any(Avatar.Size.class), Mockito.any(OutputStream.class));

        try
        {
            testObj.storeAvatarFiles(mockAvatar, imageDateProvider);
        }
        catch (IOException x)
        {
            assertThat(fakeAvatarsDir.listFiles(), is(emptyArray()));
            throw x;
        }
    }

    @Test
    public void shouldCreateFilesForAllImageTypes() throws Exception
    {
        Mockito.doAnswer(writeRequestedSizeToOutput).when(imageDateProvider).storeImage(Mockito.any(Avatar.Size.class), Mockito.any(OutputStream.class));

        testObj.storeAvatarFiles(mockAvatar, imageDateProvider);
        assertThat(fakeAvatarsDir.list(), arrayContainingInAnyOrder(
                "0_filename.txt", "0_medium_filename.txt", "0_small_filename.txt", "0_xlarge_filename.txt",
                "0_xsmall_filename.txt", "0_xxlarge@2x_filename.txt", "0_xxlarge_filename.txt",
                "0_xxxlarge@2x_filename.txt", "0_xxxlarge_filename.txt"
        ));
    }

    @Test
    public void shouldStoreSampleContentInFiles() throws Exception
    {
        Mockito.doAnswer(writeRequestedSizeToOutput).when(imageDateProvider).storeImage(Mockito.any(Avatar.Size.class), Mockito.any(OutputStream.class));

        testObj.storeAvatarFiles(mockAvatar, imageDateProvider);

        final String content = Files.readFirstLine(new File(fakeAvatarsDir, "0_xxxlarge_filename.txt"), Charset.defaultCharset());

        assertThat( content, equalTo("xxxlarge"));
    }
}
