package com.atlassian.jira.io;

import java.io.File;

import com.atlassian.jira.mock.servlet.MockHttpSession;
import com.atlassian.jira.util.AttachmentConfig;
import com.atlassian.jira.util.MockAttachmentConfig;
import com.atlassian.jira.web.HttpServletVariables;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings ("ResultOfMethodCallIgnored")
@RunWith (MockitoJUnitRunner.class)
public class TempFileFactoryImplTest
{
    @Mock
    HttpServletVariables httpServletVariables;
    MockHttpSession session;
    AttachmentConfig attachmentConfig;
    File tmpFile;
    TempFileFactoryImpl factory;

    @Before
    public void setUp() throws Exception
    {
        session = new MockHttpSession();
        attachmentConfig = new MockAttachmentConfig();
        when(httpServletVariables.getHttpSession()).thenReturn(session);

        tmpFile = new File(System.getProperty("java.io.tmpdir"), "filename");
        tmpFile.createNewFile();

        factory = createTempFileFactory();
    }

    @After
    public void tearDown() throws Exception
    {
        tmpFile.delete();
    }

    @Test (expected = IllegalArgumentException.class)
    public void makeSessionTempFileChecksIfFileIsInTempDir() throws Exception
    {
        factory.makeSessionTempFile("/etc/passwd");
    }

    @Test (expected = IllegalArgumentException.class)
    public void makeSessionTempThrowsIfFileDoesNotExist() throws Exception
    {
        tmpFile.delete();
        factory.makeSessionTempFile(tmpFile.getPath());
    }

    @Test (expected = IllegalStateException.class)
    public void makeSessionTempThrowsIfSessionDoesNotExist() throws Exception
    {
        when(httpServletVariables.getHttpSession()).thenThrow(IllegalStateException.class);

        factory.makeSessionTempFile(tmpFile.getPath());
    }

    @Test
    public void relativePathsAreRelativeToJavaTempDir() throws Exception
    {
        factory.makeSessionTempFile(tmpFile.getName());
    }

    @Test
    public void getTempFileDoesNotCreateNewTempFile() throws Exception
    {
        assertNull(factory.getSessionTempFile(tmpFile.getName()));
    }

    @Test
    public void getTempFileReturnsExistingTempFile() throws Exception
    {
        SessionTempFile sessionTempFile = factory.makeSessionTempFile(tmpFile.getName());
        assertThat(sessionTempFile.getFile(), equalTo(tmpFile));

        assertThat(factory.getSessionTempFile(tmpFile.getName()), is(sessionTempFile));
    }

    @Test
    public void getTempFileDoesNotReturnUnboundTempFile() throws Exception
    {
        SessionTempFile sessionTempFile = factory.makeSessionTempFile(tmpFile.getName());
        assertThat(sessionTempFile.getFile(), equalTo(tmpFile));
        sessionTempFile.unbind();

        assertNull(factory.getSessionTempFile(tmpFile.getName()));
    }

    @Test
    public void getTempFileDoesNotReturnDeletedTempFile() throws Exception
    {
        SessionTempFile sessionTempFile = factory.makeSessionTempFile(tmpFile.getName());
        assertThat(sessionTempFile.getFile(), equalTo(tmpFile));
        sessionTempFile.delete();

        assertNull(factory.getSessionTempFile(tmpFile.getName()));
    }

    private TempFileFactoryImpl createTempFileFactory()
    {
        return new TempFileFactoryImpl(httpServletVariables, attachmentConfig);
    }
}
