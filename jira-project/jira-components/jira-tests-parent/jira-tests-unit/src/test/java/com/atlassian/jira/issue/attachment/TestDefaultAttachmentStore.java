package com.atlassian.jira.issue.attachment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.BoundedExecutorServiceWrapper;
import com.atlassian.jira.util.Supplier;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultAttachmentStore
{
    @Rule
    public InitMockitoMocks mocks = new InitMockitoMocks(this);

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Mock
    AttachmentDirectoryAccessor directoryAccessor;

    @Before
    public void setUp()
    {
        when(directoryAccessor.getAttachmentDirectory(any(Issue.class))).thenReturn(testFolder.getRoot());
        when(directoryAccessor.getAttachmentRootPath()).thenReturn(testFolder.getRoot());
    }

    @Test
    public void putAttachmentShouldCorrectlyWriteToFile() throws Exception
    {
        final String TEST_CONTENT = "TEST FILE CONTENT";
        Project project = mock(Project.class);
        Issue issue = mock(Issue.class);
        EventPublisher ep = mock(EventPublisher.class);
        when(issue.getKey()).thenReturn("MKY-5");
        when(issue.getProjectObject()).thenReturn(project);
        when(project.getKey()).thenReturn("MKY");
        when(project.getOriginalKey()).thenReturn("MKY");

        Attachment mockAttachment = mock(Attachment.class);
        when(mockAttachment.getIssueObject()).thenReturn(issue);
        when(mockAttachment.getFilename()).thenReturn("foo.txt");

        FileBasedAttachmentStore as = new DefaultAttachmentStore(directoryAccessor, ep,
                new BoundedExecutorServiceWrapper.Builder().withExecutorService(new Supplier<ListeningExecutorService>()
                {
                    @Override
                    public ListeningExecutorService get()
                    {
                        return MoreExecutors.sameThreadExecutor();
                    }
                }));

        File tempSource = File.createTempFile("jira-blobstore", "tmp");
        FileWriter fw = new FileWriter(tempSource);
        fw.write(TEST_CONTENT);
        fw.close();

        InputStream is = new FileInputStream(tempSource);

        try
        {
            as.put(mockAttachment, is).claim();

            File expectedFile = as.getAttachmentFile(mockAttachment);
            assertTrue("Attachment file exists", expectedFile.exists());
            BufferedReader br = new BufferedReader(new FileReader(expectedFile));
            assertEquals(TEST_CONTENT, br.readLine());
        }
        finally
        {
            File testFile = new File("MKY/MKY-5/0");
            testFile.delete();
        }
    }

    @Test
    public void putAttachmentShouldReturnFailedPromiseOnError() throws Exception
    {
        Project project = mock(Project.class);
        Issue issue = mock(Issue.class);
        EventPublisher ep = mock(EventPublisher.class);
        when(issue.getKey()).thenReturn("MKY-6");
        when(issue.getProjectObject()).thenReturn(project);
        when(project.getKey()).thenReturn("MKY");
        when(project.getOriginalKey()).thenReturn("MKY");

        Attachment mockAttachment = mock(Attachment.class);
        when(mockAttachment.getIssueObject()).thenReturn(issue);
        when(mockAttachment.getFilename()).thenReturn("foo.txt");
        InputStream is = mock(InputStream.class);
        when(is.read(any(byte[].class))).thenThrow(new IOException("TEST"));

        FileBasedAttachmentStore as = new DefaultAttachmentStore(directoryAccessor, ep,
                new BoundedExecutorServiceWrapper.Builder().withExecutorService(new Supplier<ListeningExecutorService>()
                {
                    @Override
                    public ListeningExecutorService get()
                    {
                        return MoreExecutors.sameThreadExecutor();
                    }
                }));

        try
        {
            as.put(mockAttachment, is).claim();
            fail("No exception thrown");
        }
        catch (AttachmentWriteException e)
        {
            assertTrue("Correct exception thrown", true);
            verify(is).close();
        }
        finally
        {
            File testFile = new File("MKY/MKY-6/0");
            testFile.delete();
        }
    }

    @Test
    public void deleteAttachmentShouldWork() throws Exception
    {
        Project project = mock(Project.class);
        Issue issue = mock(Issue.class);
        EventPublisher ep = mock(EventPublisher.class);
        when(issue.getKey()).thenReturn("MKY-5");
        when(issue.getProjectObject()).thenReturn(project);
        when(project.getKey()).thenReturn("MKY");
        when(project.getOriginalKey()).thenReturn("MKY");

        Attachment mockAttachment = mock(Attachment.class);
        when(mockAttachment.getIssueObject()).thenReturn(issue);
        when(mockAttachment.getId()).thenReturn(0L);
        when(mockAttachment.getFilename()).thenReturn("foo.txt");
        testFolder.newFolder("MKY", "MKY-5");
        File attachmentFile = testFolder.newFile("MKY/MKY-5/0");
        assertTrue(attachmentFile.exists());
        FileBasedAttachmentStore as = new DefaultAttachmentStore(directoryAccessor, ep);

        as.delete(mockAttachment).claim();
        assertFalse(attachmentFile.exists());
    }

    @Test
    public void deleteAttachmentWorksIfThereIsNoAttachment() throws Exception
    {
        Project project = mock(Project.class);
        Issue issue = mock(Issue.class);
        EventPublisher ep = mock(EventPublisher.class);
        when(issue.getKey()).thenReturn("MKY-5");
        when(issue.getProjectObject()).thenReturn(project);
        when(project.getKey()).thenReturn("MKY");
        when(project.getOriginalKey()).thenReturn("MKY");

        Attachment mockAttachment = mock(Attachment.class);
        when(mockAttachment.getIssueObject()).thenReturn(issue);
        when(mockAttachment.getId()).thenReturn(0L);
        when(mockAttachment.getFilename()).thenReturn("foo.txt");

        FileBasedAttachmentStore as = new DefaultAttachmentStore(directoryAccessor, ep);

        as.delete(mockAttachment).claim();
    }

    @Test
    public void deleteAttachmentContainerForIssueWorks() throws Exception
    {
        File tmp = testFolder.newFolder();
        AttachmentDirectoryAccessor ada = mock(AttachmentDirectoryAccessor.class);
        when(ada.getAttachmentDirectory(any(Issue.class))).thenReturn(tmp);

        assertTrue(tmp.exists());

        Issue issue = mock(Issue.class);
        EventPublisher ep = mock(EventPublisher.class);
        FileBasedAttachmentStore as = new DefaultAttachmentStore(directoryAccessor, ep);
        as.deleteAttachmentContainerForIssue(issue).claim();
        assertFalse(tmp.exists());
    }
}
