package com.atlassian.jira.issue.attachment;

import java.io.File;

import com.atlassian.jira.exception.DataAccessException;

/**
 * Represents a read-only attachment store on a file system.
 *
 * @since v6.3
 */
public interface ReadOnlyFileBasedAttachmentStore
{
    /**
     * Returns a reference to a physical File representing the data for the given Attachment.
     * If you are calling this on multiple attachments for the same issue, consider using the overriden method that
     * passes in the issue.  Else, this goes to the database for each call.
     *
     * @param key Reference to the attachment
     * @return the file.
     * @throws com.atlassian.jira.exception.DataAccessException on failure getting required attachment info.
     */
    File getAttachmentFile(AttachmentKey key) throws DataAccessException;
}
