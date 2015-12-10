package com.atlassian.jira.io;

import java.io.File;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;

import com.atlassian.jira.mock.servlet.MockHttpSession;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SessionTempFilesTest
{
    HttpSession session;

    @Before
    public void setUp() throws Exception
    {
        session = new MockHttpSession();
    }

    @Test
    public void fileIsDeletedWhenSessionIsDestroyed() throws Exception
    {
        File file1 = File.createTempFile("abc", null);
        File file2 = File.createTempFile("def", null);
        SessionTempFiles sessionTempFiles = createSessionTempFiles();

        sessionTempFiles.createTempFile(file1);
        sessionTempFiles.createTempFile(file2);

        callValueUnbound(sessionTempFiles);
        assertThat("file1 should be deleted", file1.exists(), equalTo(false));
        assertThat("file2 should be deleted", file2.exists(), equalTo(false));
    }

    @Test
    public void unboundFileIsNotDeletedWhenSessionIsDestroyed() throws Exception
    {
        File file = File.createTempFile("abc", null);
        SessionTempFiles sessionTempFiles = createSessionTempFiles();

        SessionTempFile tempFile = sessionTempFiles.createTempFile(file);
        tempFile.unbind();

        callValueUnbound(sessionTempFiles);
        assertThat("file should NOT be deleted", file.exists(), equalTo(true));
        assertThat("clean up after myself", file.delete(), equalTo(true));
    }

    @Test
    public void alreadyDeletedFileIsNotDeletedWhenSessionIsDestroyed() throws Exception
    {
        File file = File.createTempFile("abc", null);
        SessionTempFiles sessionTempFiles = createSessionTempFiles();

        SessionTempFile tempFile = sessionTempFiles.createTempFile(file);
        tempFile.delete();
        assertThat("file should be deleted before session is destroyed", file.exists(), equalTo(false));

        // create the file again and make sure it doesn't get deleted now
        assertThat("file can be recreated", file.createNewFile(), equalTo(true));
        callValueUnbound(sessionTempFiles);
        assertThat("file should NOT be deleted when session is destroyed", file.exists(), equalTo(true));
        assertThat("clean up after myself", file.delete(), equalTo(true));
    }

    @Test
    public void sessionAttributeIsUsedIfPossible() throws Exception
    {
        SessionTempFiles sessionTempFiles1 = SessionTempFiles.forSession(session);
        assertThat(session.getAttribute(SessionTempFiles.SESSION_ATTRIBUTE_NAME), instanceOf(SessionTempFiles.class));

        SessionTempFiles sessionTempFiles2 = SessionTempFiles.forSession(session);
        assertThat(sessionTempFiles2, is(sessionTempFiles1));
    }

    private void callValueUnbound(final SessionTempFiles sessionTempFiles)
    {
        sessionTempFiles.valueUnbound(new HttpSessionBindingEvent(session, SessionTempFiles.SESSION_ATTRIBUTE_NAME));
    }

    private SessionTempFiles createSessionTempFiles()
    {
        return new SessionTempFiles(session);
    }
}
