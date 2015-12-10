package com.atlassian.jira.issue.attachment;

import java.io.File;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.Internal;
import com.atlassian.fugue.Option;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.Promise;

/**
 * @since v6.1
 * @deprecated since 6.3. Please access attachments via {@link com.atlassian.jira.issue.AttachmentManager}, and
 * thumbnails via {@link com.atlassian.jira.issue.thumbnail.ThumbnailManager}.
 */
@Internal //It belongs to jira-core but has to be left in jira-api as long as its used by AttachmentUtils
public interface AttachmentStore
{
    /**
     * Returns the physical directory of the thumbnails for the given issue, creating if necessary.
     *
     * @param issue the issue whose thumbnail directory you want
     * @return the issue's thumbnail directory
     */
    @Nonnull
    File getThumbnailDirectory(@Nonnull Issue issue);

    @Nullable
    File getAttachmentDirectory(@Nonnull String issueKey);

    /**
     * Returns the physical directory of the attachments for the given issue. This will create it if necessary.
     *
     * @param issue the issue whose attachment directory you want
     * @param createDirectory If true, and the directory does not currently exist, then the directory is created.
     * @return the issue's attachment directory
     */
    File getAttachmentDirectory(@Nonnull Issue issue, final boolean createDirectory);

    File getTemporaryAttachmentDirectory();

    /**
     * Returns the physical directory of the attachments for the given issue. This will create it if necessary.
     *
     * @param issue the issue whose attachment directory you want
     * @return The issue's attachment directory.
     */
    File getAttachmentDirectory(@Nonnull Issue issue);

    /**
     * Get the attachment directory for the given attachment base directory, project key, and issue key.
     * <p/>
     * The idea is to encapsulate all of the path-joinery magic to make future refactoring easier if we ever decide to
     * move away from attachment-base/project-key/issue-ket
     *
     * @param attachmentDirectory base of attachments
     * @param projectKey the project key the issue belongs to
     * @param issueKey the issue key for the issue
     * @return the directory attachments for this issue live in
     */
    File getAttachmentDirectory(final String attachmentDirectory, final String projectKey, final String issueKey);

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
    File getAttachmentFile(final AttachmentAdapter attachment, final File attachmentDir);

    /**
     * Store attachment data for a given attachment.
     *
     * @param metadata attachment metadata, used to determine the logical key under which to store the attachment data
     * @param data source data. The attachment store will close this stream when it has completed.
     * The stream will be closed once the operation is complete.
     * @return A promise of an attachment that performs the 'put' operation once the promise is claimed. The promise will
     * contain an {@link com.atlassian.jira.issue.attachment.AttachmentRuntimeException} in case of error.
     */
    Promise<Attachment> putAttachment(Attachment metadata, InputStream data);

    /**
     * Store attachment data for a given attachment.
     *
     * @param metadata attachment metadata, used to determine the logical key under which to store the attachment data
     * @param data source data. It is assumed that the file will exist during the attachment process (i.e. relatively
     * long lived).
     * @return A promise of an attachment that performs the 'put' operation once the promise is claimed.
     */
    Promise<Attachment> putAttachment(Attachment metadata, File data);

    /**
     * Retrieve data for a given attachment.
     * @param metaData attachment metadata, used to determine the logical key under which to store the attachment data
     * @param inputStreamProcessor Function that processes the attachment data.
     * @param <A> The class that the inputStreamProcessor returns when run.
     * @return A promise of an object that represented the processed attachment data (i.e. from running the inputStreamProcessor over the
     * attachment data).  The promise will contain an {@link com.atlassian.jira.issue.attachment.AttachmentRuntimeException} in case of error.
     */
    <A> Promise<A> getAttachment(final Attachment metaData, final Function<InputStream, A> inputStreamProcessor);

    /**
     * Moves an attachment from its current issue under a new one
     * @param metaData attachment metadata, used to determine the logical key of the attachment to be moved.
     * @param newIssueKey the key of the new issue under which the attachment will reside.
     * @return a promise that will be completed when the operation is complete. It will
     * contain an {@link com.atlassian.jira.issue.attachment.AttachmentRuntimeException} in case of error.
     */
    Promise<Void> move(Attachment metaData, String newIssueKey);


    /**
     * Just like the attachments themselves, thumbnails can succumb to file system encoding problems.
     * However we are going to regenerate thumbnails by only using the new naming scheme and not the legacy one.
     * We can't do this for attachments, but we can for thumbnails since they are ephemeral objects anyway.
     *
     * http://jira.atlassian.com/browse/JRA-23311
     *
     * @param  attachment the attachment for which to get the thumbnail file
     * @return a non-null file handle (the file itself might not exist)
     * @see #getThumbnailFile(com.atlassian.jira.issue.Issue, Attachment)
     */
    @Nonnull
    File getThumbnailFile(Attachment attachment);

    /**
     * Returns the file handle for the given attachment's thumbnail. This method performs
     * better than {@link #getThumbnailFile(Attachment)} if you already have the issue.
     *
     * @param issue the issue to which the attachment belongs
     * @param attachment the attachment for which to get the thumbnail file
     * @return a non-null file handle (the file itself might not exist)
     * @see #getThumbnailFile(Attachment)
     */
    @Nonnull
    File getThumbnailFile(@Nonnull Issue issue, Attachment attachment);

    /**
     * Returns the old legacy file name for thumbnails.
     *
     * http://jira.atlassian.com/browse/JRA-23311
     *
     * @param  attachment the attachment in play
     * @return the full legacy thumbnail file name
     */
    File getLegacyThumbnailFile(Attachment attachment);

    /**
     * Checks that the Attachment directory of the given issue is right to go - writable, accessible etc. Will create it
     * if necessary.
     *
     * @param issue the issue whose attachment directory to check.
     * @throws com.atlassian.jira.web.util.AttachmentException if the directory is not writable or missing and cannot be created.
     * @deprecated Use {@link #errors()} instead.
     */
    void checkValidAttachmentDirectory(Issue issue) throws AttachmentException;

    /**
     * Checks that the temporary directory for attachment uploads is right to go - writable, accessible etc.
     *
     * @throws com.atlassian.jira.web.util.AttachmentException if the directory is not writable or missing and cannot be created.
     * @deprecated Use {@link #errors()} instead.
     */
    void checkValidTemporaryAttachmentDirectory() throws AttachmentException;

    interface AttachmentAdapter
    {
        Long getId();

        String getFilename();
    }

    /**
     * Health status for this component. Specifically the errors that cause the attachment subsystem to fail.
     *
     * @return An option of an error collection that contains error messages if there are any issues. The
     * option will be none if there are no errors.
     */
    Option<ErrorCollection> errors();

    /**
     * Delete the specified attachment.
     * @return a promise that will contain an AttachmentCleanupException in case of error.
     */
    Promise<Void> deleteAttachment(@Nonnull Attachment attachment);

    /**
     * Delete the container for attachments for a given issue. For file systems, this means the attachment directory
     * for that issue.
     * @param issue The issue to delete attachments for.
     * @return A promise that will contain a AttachmentCleanupException if there is a problem deleting the attachment directory.
     */
    Promise<Void> deleteAttachmentContainerForIssue(@Nonnull Issue issue);
}
