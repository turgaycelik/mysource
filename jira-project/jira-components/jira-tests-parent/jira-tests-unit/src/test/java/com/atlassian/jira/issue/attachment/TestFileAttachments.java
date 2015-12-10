package com.atlassian.jira.issue.attachment;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.3
 */
public class TestFileAttachments
{
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void computeIssueKeyForOriginalProjectKeyWorksForIssueWithCorrectProjectKey()
    {
        String projectKey = "PRJ";
        String issueKey = "PRJ-100";
        assertEquals(issueKey, FileAttachments.computeIssueKeyForOriginalProjectKey(projectKey, issueKey));
    }

    @Test
    public void computeIssueKeyForOriginalProjectKeyWorksForIssueWithDifferentProjectKey()
    {
        String projectKey = "PRX";
        String issueKey = "PRJ-100";
        assertEquals("PRX-100", FileAttachments.computeIssueKeyForOriginalProjectKey(projectKey, issueKey));
    }

    @Test
    public void getAttachmentFileHolderDefaultFile() throws Exception
    {
        File rootDir = tmpFolder.getRoot();
        String projectKey = "PRJ";
        String issueKey = "PRJ-100";
        Attachment attachment = mock(Attachment.class);
        when(attachment.getId()).thenReturn(1L);
        when(attachment.getFilename()).thenReturn("Foo.txt");
        AttachmentKey key = AttachmentKeys.from(projectKey, issueKey, attachment);
        tmpFolder.newFolder(projectKey, issueKey);
        File expectedFile = tmpFolder.newFile(projectKey + "/" + issueKey + "/1");
        File result = FileAttachments.getAttachmentFileHolder(key, rootDir);
        assertEquals(expectedFile, result);
    }

    @Test
    public void getAttachmentFileHolderLegacyFile() throws Exception
    {
        File rootDir = tmpFolder.getRoot();
        String projectKey = "PRJ";
        String issueKey = "PRJ-100";
        Attachment attachment = mock(Attachment.class);
        when(attachment.getId()).thenReturn(1L);
        when(attachment.getFilename()).thenReturn("Foo.txt");
        AttachmentKey key = AttachmentKeys.from(projectKey, issueKey, attachment);
        tmpFolder.newFolder(projectKey, issueKey);
        File expectedFile = tmpFolder.newFile(projectKey + "/" + issueKey + "/1_Foo.txt");
        File result = FileAttachments.getAttachmentFileHolder(key, rootDir);
        assertEquals(expectedFile, result);
    }
}
