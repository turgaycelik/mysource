package com.atlassian.jira.issue.attachment;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.util.AttachmentException;

/**
 * Interface for a component that provides access to attachment directories. This is typically only useful for file-based
 * attachment stores.
 *
 * @since v6.3
 */
public interface AttachmentDirectoryAccessor extends AttachmentHealth
{
    /**
     * Returns the physical directory of the thumbnails for the given issue, creating if necessary.
     *
     * @param issue the issue whose thumbnail directory you want
     * @return The issue's thumbnail directory.
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
     * Checks that the Attachment directory of the given issue is right to go - writable, accessible etc. Will create it
     * if necessary.
     *
     * @param issue the issue whose attachment directory to check.
     * @throws com.atlassian.jira.web.util.AttachmentException if the directory is not writable or missing and cannot be created.
     * @deprecated Use {@link #errors()} instead. This method is here while {@link com.atlassian.jira.issue.attachment.AttachmentStore}
     * is still in jira-api.
     */
    void checkValidAttachmentDirectory(Issue issue) throws AttachmentException;

    /**
     * Checks that the temporary directory where attachments can be uploaded is writeable.
     * @throws com.atlassian.jira.web.util.AttachmentException if the directory is not writable or missing and cannot be created.
     * @deprecated Use {@link #errors()} instead. This method is here while {@link com.atlassian.jira.issue.attachment.AttachmentStore}
     * is still in jira-api.
     */
    void checkValidTemporaryAttachmentDirectory() throws AttachmentException;

    /**
     * Returns the path used to store all attachments across the system.
     * @return the path used to store all attachments across the system.
     */
    File getAttachmentRootPath();
}
