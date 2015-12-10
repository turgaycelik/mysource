package com.atlassian.jira.issue.attachment;

import java.io.File;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.issue.MockIssue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.3
 */
public class TestFileSystemThumbnailAccessor
{
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void deleteThumbnailDirectoryShouldWork() throws Exception
    {
        File tmp = tmpFolder.newFolder();
        AttachmentDirectoryAccessor ada = mock(AttachmentDirectoryAccessor.class);
        when(ada.getThumbnailDirectory(any(Issue.class))).thenReturn(tmp);

        assertTrue(tmp.exists());

        FileSystemThumbnailAccessor accessor = new FileSystemThumbnailAccessor(ada);
        accessor.deleteThumbnailDirectory(new MockIssue()).claim();
        assertFalse(tmp.exists());
    }
}
