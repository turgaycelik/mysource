package com.atlassian.jira.issue.attachment;

/**
 * Used by Project Import.
 *
 * @since v6.1
 */
public class AttachmentAdapterImpl implements AttachmentStore.AttachmentAdapter
{
    final private Long id;
    final private String name;

    public AttachmentAdapterImpl(final Long id, final String name)
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

    public static AttachmentAdapterImpl fromAttachment(final Attachment attachment)
    {
        return new AttachmentAdapterImpl(attachment.getId(), attachment.getFilename());
    }
}
