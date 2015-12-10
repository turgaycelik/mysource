package com.atlassian.jira.issue.attachment;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.util.dbc.Assertions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An attachment items holds an attachment and an optional thumbnail.
 */
public class AttachmentItem
{
    @Nonnull
    final Attachment attachment;

    @Nullable
    final Thumbnail thumbnail;

    AttachmentItem(@Nonnull Attachment attachment, @Nullable Thumbnail thumbnail)
    {
        this.attachment = Assertions.notNull(attachment);
        this.thumbnail = thumbnail;
    }

    /**
     * @return the attachment
     */
    public Attachment attachment()
    {
        return attachment;
    }

    /**
     * @return the thumbnail, or null
     * @see #isThumbnailAvailable()
     */
    public Thumbnail thumbnail()
    {
        return thumbnail;
    }

    /**
     * @return a boolean indicating whether this attachment item has a thumbnail
     */
    public boolean isThumbnailAvailable()
    {
        return thumbnail != null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        AttachmentItem that = (AttachmentItem) o;

        if (!attachment.equals(that.attachment)) { return false; }
        if (thumbnail != null ? !thumbnail.equals(that.thumbnail) : that.thumbnail != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = attachment.hashCode();
        result = 31 * result + (thumbnail != null ? thumbnail.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "AttachmentItem{attachment=" + attachment + ", thumbnail=" + thumbnail + '}';
    }
}
