package com.atlassian.jira.issue.attachment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class TestAttachmentZipFileCreator
{
    private class IssueWithAttachments extends MockIssue
    {
        private final String issueKey;
        private Collection<? extends Attachment> attachments;

        private IssueWithAttachments(final String issueKey)
        {
            this.issueKey = issueKey;
            this.attachments = new ArrayList<Attachment>();
        }

        private void setAttachments(Collection<? extends Attachment> attachments)
        {
            this.attachments = attachments;
        }

        @Override
        public Collection getAttachments()
        {
            return attachments;
        }

        @Override
        public String getKey()
        {
            return issueKey;
        }
    }

    private class AttachmentImpl extends Attachment
    {
        private final Issue issue;
        private final String fileName;

        public AttachmentImpl(final Issue issue, final String fileName)
        {
            super(null, new MockGenericValue("attachment"), null);
            this.issue = issue;
            this.fileName = fileName;
        }

        @Override
        public String getFilename()
        {
            return fileName;
        }

        @Override
        public Issue getIssueObject()
        {
            return issue;
        }
    }

    @Test
    public void testZipCreation() throws IOException
    {
        IssueWithAttachments issue = new IssueWithAttachments("TST-123");
        Collection<AttachmentImpl> attachments = CollectionBuilder.newBuilder(
                new AttachmentImpl(issue, "file1.txt"),
                new AttachmentImpl(issue, "file2.txt")
        ).asArrayList();

        issue.setAttachments(attachments);

        AttachmentZipFileCreator zipFileCreator = newAttachmentZipFileCreator(issue);

        File zipFile = null;
        try
        {
            zipFile = zipFileCreator.toZipFile();
            assertZipFileContent(zipFile, Arrays.asList("file1.txt", "file2.txt"));
        }
        finally
        {
            deleteZipFile(zipFile);
        }
    }

    @Test
    public void testZipCreationWithNonUniqueNames() throws IOException
    {
        IssueWithAttachments issue = new IssueWithAttachments("TST-123");
        Collection<AttachmentImpl> attachments = CollectionBuilder.newBuilder(
                new AttachmentImpl(issue, "file1.txt"),
                new AttachmentImpl(issue, "file2.txt"),
                new AttachmentImpl(issue, "file2.txt"),
                new AttachmentImpl(issue, "file2.txt"),
                new AttachmentImpl(issue, "file3.txt")
        ).asArrayList();

        issue.setAttachments(attachments);

        AttachmentZipFileCreator zipFileCreator = newAttachmentZipFileCreator(issue);

        File zipFile = null;
        try
        {
            zipFile = zipFileCreator.toZipFile();
            assertZipFileContent(zipFile, Arrays.asList("file1.txt", "file2.txt", "file2.txt.1", "file2.txt.2", "file3.txt"));
        }
        finally
        {
            deleteZipFile(zipFile);
        }

    }

    private void assertZipFileContent(final File zipFile, final List<String> expectedFilesNames) throws IOException
    {
        assertNotNull(zipFile);
        assertTrue(zipFile.exists());
        assertTrue(zipFile.getName().endsWith(".zip"));
        // Open the ZIP file
        ZipFile zf = new ZipFile(zipFile);

        // Enumerate each entry
        int actualCount = 0;
        for (Enumeration entries = zf.entries(); entries.hasMoreElements();)
        {
            // Get the entry name
            String zipEntryName = ((ZipEntry) entries.nextElement()).getName();
            assertTrue("Expecting " + zipEntryName, expectedFilesNames.contains(zipEntryName));
            actualCount++;
        }
        assertEquals("They arent the same size", expectedFilesNames.size(), actualCount);
    }

    private AttachmentZipFileCreator newAttachmentZipFileCreator(final IssueWithAttachments issue)
    {
        return new AttachmentZipFileCreator(issue)
        {
            @Override
            File getAttachmentFile(final Attachment attachment)
            {
                try
                {
                    File f = File.createTempFile("abc", "xyz");
                    f.deleteOnExit();
                    return f;
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private void deleteZipFile(final File zipFile)
    {
        if (zipFile != null)
        {
            zipFile.delete();
        }
    }

}
