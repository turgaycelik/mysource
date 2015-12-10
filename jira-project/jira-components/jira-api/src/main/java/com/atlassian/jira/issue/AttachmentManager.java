/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.io.InputStreamConsumer;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.jira.web.util.AttachmentException;

import org.ofbiz.core.entity.GenericValue;

/**
 * Manages all attachment related tasks in JIRA, which involves retrieving an attachment,
 * creating an attachment and deleting an attachment.
 */
@PublicApi
public interface AttachmentManager
{
    /**
     * The name of the issue-specific sub-directory in which its attachment thumbnails are stored.
     *
     * @since 6.3 moved from AttachmentUtils
     */
    String THUMBS_SUBDIR = "thumbs";

    /**
     * Get a single attachment by its ID.
     *
     * @param id the Attachment ID
     * @return the Attachment can never be null as an exception is thrown if an attachment with the passed id does not
     *  exist.
     * @throws DataAccessException if there is a problem accessing the database.
     * @throws AttachmentNotFoundException thrown if an attachment with the passed id does not exist.
     */
    Attachment getAttachment(Long id) throws DataAccessException, AttachmentNotFoundException;

    /**
     * Get a list of all attachments for a certain issue.
     *
     * @param issue the Issue
     * @return a list of {@link Attachment} objects
     * @throws DataAccessException if there is a problem accessing the database.
     */
    List<Attachment> getAttachments(Issue issue) throws DataAccessException;

    /**
     * Get a list of all attachments for a certain issue, sorted according to the specified comparator.
     *
     * @param issue the Issue
     * @param comparator used for sorting
     * @return a list of {@link Attachment} objects
     * @throws DataAccessException if there is a problem accessing the database.
     */
    List<Attachment> getAttachments(Issue issue, Comparator<? super Attachment> comparator) throws DataAccessException;

    /**
     * Create an attachment both on disk, and in the database by copying the provided file instead of moving it.
     *
     * @param file                 A file on a locally accessible filesystem, this will be copied, not moved.
     * @param filename             The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType          The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param attachmentAuthor     The username of the user who created this attachment, this is not validated so it must be a valid username
     * @param issue                The id of the issue that this attachment is attached to
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).  These are optional,
     *                             and are used to populate a PropertySet on the Attachment ({@link com.atlassian.jira.issue.attachment.Attachment#getProperties()}.  Pass null to set no properties
     * @param createdTime          when the attachment was created
     *
     * @return the Attachment
     * @throws com.atlassian.jira.web.util.AttachmentException if any errors occur.
     * @deprecated Use {@link #createAttachment(com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean)} instead. Since v6.0.
     */
    Attachment createAttachmentCopySourceFile(File file, String filename, String contentType, String attachmentAuthor, Issue issue, Map<String, Object> attachmentProperties, Date createdTime) throws AttachmentException;

    /**
     * Create an attachment both on disk, and in the database.
     *
     * @param file                 A file on a locally accessible filesystem
     * @param filename             The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType          The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param author               The user who created this attachment
     * @param issue                The issue that this file is to be attached to
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).  These are optional,
     *                             and are used to populate a PropertySet on the Attachment ({@link com.atlassian.jira.issue.attachment.Attachment#getProperties()}.  Pass null to set no properties
     * @param createdTime the created time
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     *
     * @throws AttachmentException if an IO error occurs while attempting to copy the file
     *
     * @see #createAttachment(java.io.File, String, String, com.atlassian.crowd.embedded.api.User, Issue)
     * @deprecated Use {@link #createAttachment(com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean)} instead. Since v6.0.
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue, Map<String, Object> attachmentProperties, Date createdTime) throws AttachmentException;

    /**
     * Create an attachment both on disk, and in the database.
     *
     * @param file                 A file on a locally accessible filesystem
     * @param filename             The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType          The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param author               The user who created this attachment
     * @param issue                The issue that this file is to be attached to
     * @param zip                  This file is a zip file.  Null indicates that it is not know if this attachment is a zip file or not
     * @param thumbnailable        This file is thumbnailable (e.g. a png image).  Null indicates that it is not know if this attachment is thumbnailable or not
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).  These are optional,
     *                             and are used to populate a PropertySet on the Attachment ({@link com.atlassian.jira.issue.attachment.Attachment#getProperties()}.  Pass null to set no properties
     * @param createdTime the created time
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     *
     * @throws AttachmentException if an IO error occurs while attempting to copy the file
     *
     * @see #createAttachment(java.io.File, String, String, com.atlassian.crowd.embedded.api.User, Issue)
     * @deprecated Use {@link #createAttachment(com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean)} instead. Since v6.0.
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue, @Nullable Boolean zip, @Nullable Boolean thumbnailable, Map<String, Object> attachmentProperties, Date createdTime) throws AttachmentException;

    /**
     * Create an attachment both on disk, and in the database.
     *
     * @param file                 A file on a locally accessible filesystem
     * @param filename             The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType          The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param author               The user who created this attachment
     * @param issue                The issue that this attachment is attached to
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).  These are optional,
     *                             and are used to populate a PropertySet on the Attachment ({@link com.atlassian.jira.issue.attachment.Attachment#getProperties()}.  Pass null to set no properties
     * @param createdTime the created time
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     *
     * @throws com.atlassian.jira.web.util.AttachmentException if an error occurs while attempting to copy the file
     *
     * @deprecated Use {@link #createAttachment(File, String, String, User, Issue, Map, Date)} instead. Since v5.0.
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, User author, GenericValue issue, Map<String, Object> attachmentProperties, Date createdTime) throws AttachmentException;

    /**
     * Create an attachment both on disk, and in the database.
     *
     * @param file                 A file on a locally accessible filesystem
     * @param filename             The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType          The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param author               The user who created this attachment
     * @param issue                The issue that this attachment is attached to
     * @param zip                  This file is a zip file.  Null indicates that it is not know if this attachment is a zip file or not
     * @param thumbnailable        This file is thumbnailable (e.g. a png image).  Null indicates that it is not know if this attachment is thumbnailable or not
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).  These are optional,
     *                             and are used to populate a PropertySet on the Attachment ({@link com.atlassian.jira.issue.attachment.Attachment#getProperties()}.  Pass null to set no properties
     * @param createdTime the created time
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     *
     * @throws com.atlassian.jira.web.util.AttachmentException if an error occurs while attempting to copy the file
     *
     * @deprecated Use {@link #createAttachment(File, String, String, User, Issue, Map, Date)} instead. Since v5.0.
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, User author, GenericValue issue, Boolean zip, Boolean thumbnailable, Map<String, Object> attachmentProperties, Date createdTime) throws AttachmentException;

    /**
     * Same as the {@link #createAttachment(File, String, String, User, Issue, Map, Date)} method, except it submits no
     * attachmentProperties and uses now() for the created time.
     *
     * @param file        A file on a locally accessible filesystem
     * @param filename    The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param author      The user who created this attachment
     * @param issue       The issue that this attachment is attached to
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     *
     * @throws AttachmentException if an IO error occurs while attempting to copy the file
     *
     * @see #createAttachment(java.io.File, String, String, com.atlassian.crowd.embedded.api.User, Issue, java.util.Map, java.util.Date)
     * @deprecated Use {@link #createAttachment(com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean)} instead. Since v6.0.
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue) throws AttachmentException;

    /**
     * Same as the {@link #createAttachment(java.io.File, String, String, User, org.ofbiz.core.entity.GenericValue, java.util.Map, java.util.Date)} method, except it
     * submits no attachmentProperties and uses now() for the created time.
     *
     * @param file        A file on a locally accessible filesystem
     * @param filename    The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param author      The user who created this attachment
     * @param issue       The issue that this attachment is attached to
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     *
     * @throws com.atlassian.jira.web.util.AttachmentException if an error occurs while attempting to copy the file
     *
     * @deprecated Use {@link #createAttachment(File, String, String, User, Issue)} instead. Since v5.0.
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, User author, GenericValue issue) throws AttachmentException;

    /**
     * Create an attachment in the database.  Note that this does not create it on disk, nor does it create a change item.
     *
     * @param issue                the issue that this attachment is attached to
     * @param author               The user who created this attachment
     * @param mimetype             mimetype
     * @param filename             The desired filename for this attachment.
     * @param filesize             filesize
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).
     * @param createdTime          when the attachment was created
     *
     * @return the Attachment
     *
     * @deprecated Use {@link #createAttachment(File, String, String, User, Issue, Map, Date)} instead. Since v5.0.
     */
    Attachment createAttachment(GenericValue issue, User author, String mimetype, String filename, Long filesize, Map<String, Object> attachmentProperties, Date createdTime);

    /**
     * Create an attachment both on disk, and in the database.
     *
     * @param createAttachmentParamsBean Parameters which describe created attachment
     *
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     * @see com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean.Builder for creating beans
     * @throws AttachmentException if an IO error occurs while attempting to copy the file
     **/
    ChangeItemBean createAttachment(CreateAttachmentParamsBean createAttachmentParamsBean) throws AttachmentException;


    /**
     * Delete an attachment from the database and from the attachment store.
     *
     * @param attachment the Attachment
     * @throws RemoveException if the attachment cannot be removed from the attachment store
     */
    void deleteAttachment(Attachment attachment) throws RemoveException;


    /**
     * Delete the attachment directory from disk if the directory is empty.
     *
     * @param issue the issue whose attachment directory we wish to delete.
     * @throws RemoveException if the directory can not be removed or is not empty.
     * @deprecated This will no longer be exposed by AttachmentManager.
     */
    @Internal
    void deleteAttachmentDirectory(Issue issue) throws RemoveException;

    /**
     * Determine if attachments have been enabled in JIRA and if the attachments directory exists.
     * @return true if enabled, false otherwise
     */
    boolean attachmentsEnabled();

    /**
     * Determine if screenshot applet has been enabled in JIRA.
     * @return true if enabled, false otherwise
     */
    boolean isScreenshotAppletEnabled();

    /**
     * Determine if the screenshot applet is supported by the user's operating system.
     *
     * Note. This always returns true now as we support screenshots on all our supported platforms
     *
     * @return true if applet is supported by the user's OS, false otherwise
     */
    boolean isScreenshotAppletSupportedByOS();

    /**
     * Converts a set of provided temporary attachments to real attachments attached to an issue.  This method will
     * also clean up any temporary attachments still linked to the issue via the TemporaryAttachmentsMonitor.
     *
     * @param user The user performing the action
     * @param issue The issue attachments should be linked to
     * @param selectedAttachments The temporary attachment ids to convert as selected by the user
     * @param temporaryAttachmentsMonitor TemporaryAttachmentsMonitor containing information about all temporary attachments
     * @return A list of ChangeItemBeans for any attachments that got created
     * @throws AttachmentException If there were problems with the Attachment itself
     * @deprecated Use {@link #convertTemporaryAttachments(com.atlassian.jira.user.ApplicationUser, Issue, java.util.List, com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor)} instead. Since v6.0.
     */
    List<ChangeItemBean> convertTemporaryAttachments(final User user, final Issue issue, final List<Long> selectedAttachments,
            final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor) throws AttachmentException;

    /**
     * Converts a set of provided temporary attachments to real attachments attached to an issue.  This method will
     * also clean up any temporary attachments still linked to the issue via the TemporaryAttachmentsMonitor.
     *
     * @param user The user performing the action
     * @param issue The issue attachments should be linked to
     * @param selectedAttachments The temporary attachment ids to convert as selected by the user
     * @param temporaryAttachmentsMonitor TemporaryAttachmentsMonitor containing information about all temporary attachments
     * @return A list of ChangeItemBeans for any attachments that got created
     * @throws AttachmentException If there were problems with the Attachment itself
     */
    List<ChangeItemBean> convertTemporaryAttachments(final ApplicationUser user, final Issue issue, final List<Long> selectedAttachments,
            final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor) throws AttachmentException;

    /**
     * Stores the thumbnailble flag for this attachment
     * @param attachment The attachment
     * @param thumbnailable True if this attachment is thumnailable
     */
    Attachment setThumbnailable(Attachment attachment, boolean thumbnailable);

    /**
     * Stores the zip flag for this attachment
     * @param attachment The attachment
     * @param zip True if this attachment is a zip file
     */
    Attachment setZip(Attachment attachment, boolean zip);

    /**
     * Get binary content of the attachment
     *
     * @param attachment the attachment whose content to stream (required)
     * @param consumer the consumer of the stream (required)
     */
    <T> T streamAttachmentContent(@Nonnull Attachment attachment, InputStreamConsumer<T> consumer)
            throws IOException;

    /**
     * Move Issue attachments to a new directory. This method is intended for Move/Bulk Move only.
     * @param oldIssue the issue attachments will be moved from
     * @param newIssueKey the new issue key
     */
    void moveAttachments(Issue oldIssue, String newIssueKey);
}
