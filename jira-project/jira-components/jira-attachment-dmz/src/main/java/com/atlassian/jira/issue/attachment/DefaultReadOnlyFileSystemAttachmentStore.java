package com.atlassian.jira.issue.attachment;

import java.io.File;

import com.atlassian.jira.exception.DataAccessException;

/**
 * Basic implementation of a ReadOnlyFileBasedAttachmentStore rooted at the specified root directory.
 *
 * @since v6.3
 */
public class DefaultReadOnlyFileSystemAttachmentStore implements ReadOnlyFileBasedAttachmentStore
{
    private final File rootDirectory;

    public DefaultReadOnlyFileSystemAttachmentStore(final File rootDirectory)
    {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public File getAttachmentFile(final AttachmentKey attachment) throws DataAccessException
    {
        return FileAttachments.getAttachmentFileHolder(attachment, rootDirectory);
    }
}
