package com.atlassian.jira.issue.attachment;

import java.io.File;

import javax.annotation.Nonnull;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.util.concurrent.Promise;

import io.atlassian.blobstore.client.api.Unit;

/**
 * Interface for an attachment store that presents a File-based interface for accessing attachments.
 *
 * TODO - We should be able to remove this once we provide sufficient methods in SimpleAttachmentStore to eliminate
 * access to attachments via files.
 *
 * @since v6.3
 */
public interface FileBasedAttachmentStore extends SimpleAttachmentStore
{
    /**
     * Returns the physical File for the given Attachment.
     * This method performs better as it does not need to look up the issue object.
     *
     * @param issue the issue the attachment belongs to.
     * @param attachment the attachment.
     * @return the file.
     * @throws com.atlassian.jira.exception.DataAccessException on failure getting required attachment info.
     */
    File getAttachmentFile(Issue issue, Attachment attachment) throws DataAccessException;

    /**
     * Returns the physical File for the given Attachment.
     * If you are calling this on multiple attachments for the same issue, consider using the overriden method that
     * passes in the issue.  Else, this goes to the database for each call.
     *
     * @param attachment the attachment.
     * @return the file.
     * @throws com.atlassian.jira.exception.DataAccessException on failure getting required attachment info.
     */
    File getAttachmentFile(Attachment attachment) throws DataAccessException;

    /**
     * This is intended for cases where you want more control over where the attachment actually lives and you just want
     * something to handle the look up logic for the various possible filenames an attachment can have.
     * <p/>
     * In practice, this is just used during Project Import
     *
     * @param attachment it's not an attachment but it acts like one for our purposes.
     * @param attachmentDir the directory the attachments live in. This is different that the system-wide attachment
     * directory. i.e. this would "attachments/MKY/MKY-1" and not just "attachments"
     * @return the actual attachment
     */
    File getAttachmentFile(final AttachmentStore.AttachmentAdapter attachment, final File attachmentDir);

    /**
     * Delete the container for attachments for a given issue. For file systems, this means the attachment directory
     * for that issue.
     * @param issue The issue to delete attachments for.
     * @return A promise that will contain a AttachmentCleanupException if there is a problem deleting the attachment directory.
     */
    Promise<Unit> deleteAttachmentContainerForIssue(@Nonnull Issue issue);
}
