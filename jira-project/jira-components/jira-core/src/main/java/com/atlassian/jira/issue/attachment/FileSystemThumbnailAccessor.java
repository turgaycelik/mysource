package com.atlassian.jira.issue.attachment;

import java.io.File;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.Issue;

public class FileSystemThumbnailAccessor extends WithDirectoryAccessorThumbnailAccessor
{
    /**
     * Infix for generated thumbnail images.
     */
    private static final String THUMBNAIL_DESIGNATION = "_thumb_";

    public FileSystemThumbnailAccessor(final AttachmentDirectoryAccessor ada)
    {
        super(ada);
    }

    /*
     Prosecution : Your honour, this is hard coding the format name.  Hard coding is always bad!
     Defense     : There are benefits in knowing that a file on disk is in fact a png.
     Prosecution  : What if we change this thumbnail format.
     Defense     : We will never change from png any time soon
     Judge       : I will allow it!
     */
    private static File getThumbnailFile(final AttachmentStore.AttachmentAdapter attachment, final File attachmentDirectory)
    {
        return new File(attachmentDirectory, THUMBNAIL_DESIGNATION + attachment.getId() + ".png");
    }

    private static File getLegacyThumbnailFile(final AttachmentStore.AttachmentAdapter attachment, final File attachmentDirectory)
    {
        return new File(attachmentDirectory, attachment.getId() + THUMBNAIL_DESIGNATION + attachment.getFilename());
    }

    @Nonnull
    @Override
    public File getThumbnailFile(@Nonnull final Attachment attachment)
    {
        final AttachmentStore.AttachmentAdapter attachmentAdapter = AttachmentAdapterImpl.fromAttachment(attachment);
        final File thumbDir = getAttachmentDirectoryAccessor().getThumbnailDirectory(attachment.getIssueObject());
        return getThumbnailFile(attachmentAdapter, thumbDir);
    }

    @Nonnull
    @Override
    public File getThumbnailFile(@Nonnull final Issue issue, @Nonnull final Attachment attachment)
    {
        final AttachmentStore.AttachmentAdapter attachmentAdapter = AttachmentAdapterImpl.fromAttachment(attachment);
        final File thumbDir = getAttachmentDirectoryAccessor().getThumbnailDirectory(issue);
        return getThumbnailFile(attachmentAdapter, thumbDir);
    }

    /**
     * Returns the old legacy file name for thumbnails
     *
     * http://jira.atlassian.com/browse/JRA-23311
     *
     * @param  attachment the attacment in play
     * @return the full legacy thumbnail file name
     */
    @Override
    public File getLegacyThumbnailFile(@Nonnull final Attachment attachment)
    {
        final AttachmentStore.AttachmentAdapter attachmentAdapter = AttachmentAdapterImpl.fromAttachment(attachment);
        final File thumbDir = getAttachmentDirectoryAccessor().getThumbnailDirectory(attachment.getIssueObject());
        return getLegacyThumbnailFile(attachmentAdapter, thumbDir);
    }

}
