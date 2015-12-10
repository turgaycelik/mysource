package com.atlassian.jira.issue.attachment;

import java.io.File;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.Issue;
import com.atlassian.util.concurrent.Promise;

public interface ThumbnailAccessor
{
    /**
     * Just like the attachments themselves, thumbnails can succumb to file system encoding problems. However we are
     * going to regenerate thumbnails by only using the new naming scheme and not the legacy one.  We cant do this for
     * attachments but we can for thumbnails since they are epheral objects anyway.
     *
     * http://jira.atlassian.com/browse/JRA-23311
     *
     * @param  attachment the attachment for which to get the thumbnail file
     * @return a non-null file handle (the file itself might not exist)
     * @see #getThumbnailFile(com.atlassian.jira.issue.Issue, Attachment)
     */
    @Nonnull
    File getThumbnailFile(@Nonnull Attachment attachment);

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
    File getThumbnailFile(@Nonnull Issue issue, @Nonnull Attachment attachment);

    /**
     * Returns the old legacy file name for thumbnails.
     *
     * http://jira.atlassian.com/browse/JRA-23311
     *
     * @param  attachment the attachment in play
     * @return the full legacy thumbnail file name
     */
    File getLegacyThumbnailFile(@Nonnull Attachment attachment);

    /**
     * Returns the physical directory of the thumbnails for the given issue, creating if necessary.
     *
     * @param issue the issue whose thumbnail directory you want
     * @return The issue's thumbnail directory.
     */
    @Nonnull
    File getThumbnailDirectory(@Nonnull Issue issue);

    /**
     * Deletes the thumbnail directory for a given issue, deleting its contents if necessary.
     * @param issue The issue whose thumbnail directory you want to delete.
     * @return A promise that contains a AttachmentCleanupException if there was an error deleting the directory.
     */
    Promise<Void> deleteThumbnailDirectory(@Nonnull Issue issue);
}
