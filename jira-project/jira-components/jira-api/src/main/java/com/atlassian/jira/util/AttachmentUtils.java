package com.atlassian.jira.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.web.util.AttachmentException;

import java.io.File;

/**
 * Static utilities for working with the attachment files and their directories.
 *
 * @deprecated Use {@link com.atlassian.jira.issue.AttachmentManager} only. Since v6.1
 */
@Deprecated
public class AttachmentUtils
{
    /**
     * Infix for generated thumbnail images.
     */
    public static final String THUMBS_SUBDIR = AttachmentManager.THUMBS_SUBDIR;

    /**
     * Returns the physical directory of the thumbnails for the given issue, creating if necessary.
     *
     * @param issue the issue whose thumbnail directory you want.
     * @return The issue's thumbnail directory.
     */
    public static File getThumbnailDirectory(Issue issue)
    {
        return getAttachmentStore().getThumbnailDirectory(issue);
    }

    /**
     * Returns the physical directory of the attachments for the given issue. This will create it if necessary.
     *
     * @param issue the issue whose attachment directory you want.
     * @return The issue's attachment directory.
     */
    public static File getAttachmentDirectory(Issue issue)
    {
        return getAttachmentDirectory(issue, true);
    }

    /**
     * Returns the physical directory of the attachments for the given issue. This will create it if necessary.
     *
     * @param issue the issue whose attachment directory you want.
     * @param createDirectory If true, and the directory does not currently exist, then the directory is created.
     * @return The issue's attachment directory.
     */
    public static File getAttachmentDirectory(Issue issue, final boolean createDirectory)
    {
        return getAttachmentStore().getAttachmentDirectory(issue, createDirectory);
    }

    protected static AttachmentStore getAttachmentStore()
    {
        return ComponentAccessor.getComponent(AttachmentStore.class);
    }

    public static File getTemporaryAttachmentDirectory()
    {
        return getAttachmentStore().getTemporaryAttachmentDirectory();
    }

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
    public static File getAttachmentDirectory(final String attachmentDirectory, final String projectKey, final String issueKey)
    {
        return getAttachmentStore().getAttachmentDirectory(attachmentDirectory, projectKey, issueKey);
    }

    /**
     * Returns the physical File for the given Attachment.
     * This method performs better as it does not need to look up the issue object.
     *
     * @param issue the issue the attachment belongs to.
     * @param attachment the attachment.
     * @return the file.
     * @throws DataAccessException on failure getting required attachment info.
     */
    public static File getAttachmentFile(Issue issue, Attachment attachment) throws DataAccessException
    {
        return getAttachmentStore().getAttachmentFile(issue, attachment);
    }

    /**
     * Returns the physical File for the given Attachment.
     * If you are calling this on multiple attachments for the same issue, consider using the overriden method that
     * passes in the issue.  Else, this goes to the database for each call.
     *
     * @param attachment the attachment.
     * @return the file.
     * @throws DataAccessException on failure getting required attachment info.
     */
    public static File getAttachmentFile(Attachment attachment) throws DataAccessException
    {
        return getAttachmentFile(attachment.getIssueObject(), attachment);
    }

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
    public static File getAttachmentFile(final AttachmentAdapter attachment, final File attachmentDir)
    {
        return getAttachmentStore().getAttachmentFile(attachment, attachmentDir);
    }

    /**
     * Just like the attachments themselves, thumbnails can succumb to file system encoding problems.
     * However we are going to regenerate thumbnails by only using the new naming scheme and not the legacy one.
     * We can't do this for attachments, but we can for thumbnails since they are ephemeral objects anyway.
     *
     * http://jira.atlassian.com/browse/JRA-23311
     *
     * @param  attachment the attacment in play
     * @return the full thumbnail file name
     */
    public static File getThumbnailFile(Attachment attachment)
    {
        return getAttachmentStore().getThumbnailFile(attachment);
    }

    public static File getThumbnailFile(Issue issue, Attachment attachment)
    {
        return getAttachmentStore().getThumbnailFile(issue, attachment);
    }

    /**
     * Returns the old legacy file name for thumbnails
     *
     * http://jira.atlassian.com/browse/JRA-23311
     *
     * @param  attachment the attacment in play
     * @return the full legacy thumbnail file name
     */
    public static File getLegacyThumbnailFile(Attachment attachment)
    {
        return getAttachmentStore().getLegacyThumbnailFile(attachment);
    }

    /**
     * Checks that the Attachment directory of the given issue is right to go - writable, accessible etc. Will create it
     * if necessary.
     *
     * @param issue the issue whose attachment directory to check.
     * @throws AttachmentException if the directory is not writable or missing and cannot be created.
     */
    public static void checkValidAttachmentDirectory(Issue issue) throws AttachmentException
    {
        getAttachmentStore().checkValidAttachmentDirectory(issue);
    }

    public static void checkValidTemporaryAttachmentDirectory() throws AttachmentException
    {
        getAttachmentStore().checkValidTemporaryAttachmentDirectory();
    }

    /**
     * We need some of this utility to code to work for both Attachments and ExternalAttachments (from Project Import).
     * All we really need to work is the id and the filename so this provides an adapter so that we can reuse code
     * here.
     * @deprecated Use {@link AttachmentStore} with {@link AttachmentStore.AttachmentAdapter}. Since v6.1
     */
    @Deprecated
    public static class AttachmentAdapter implements AttachmentStore.AttachmentAdapter
    {
        final private Long id;
        final private String name;

        public AttachmentAdapter(final Long id, final String name)
        {
            this.id = id;
            this.name = name;
        }

        public Long getId()
        {
            return id;
        }

        public String getFilename()
        {
            return name;
        }

        static AttachmentAdapter fromAttachment(final Attachment attachment)
        {
            return new AttachmentAdapter(attachment.getId(), attachment.getFilename());
        }
    }
}
