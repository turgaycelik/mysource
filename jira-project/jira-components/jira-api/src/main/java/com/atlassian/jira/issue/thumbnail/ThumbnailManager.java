package com.atlassian.jira.issue.thumbnail;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.util.io.InputStreamConsumer;

import org.ofbiz.core.entity.GenericEntityException;

/**
 * Provides key services for small-sized images representing image attachments on issues.
 */
@PublicApi
public interface ThumbnailManager
{
    /**
     * The JIRA global thumbnail MIME type.
     */
    Thumbnail.MimeType MIME_TYPE = Thumbnail.MimeType.PNG;

    /**
     * Retrieves {@link com.atlassian.core.util.thumbnail.Thumbnail Thumbnails} for an {@link com.atlassian.jira.issue.Issue}
     * @param issue the issue to get the thumnails for.
     * @param user the user on whose behalf the request is made.
     * @return the thumbnails.
     * @see #toThumbnailedImage(com.atlassian.core.util.thumbnail.Thumbnail)
     */
    Collection<Thumbnail> getThumbnails(Issue issue, User user) throws Exception;

    /**
     * Indicates whether JIRA can generate a thumbnail for the given attachment.
     *
     * @param attachment the attachment (required)
     * @return see above
     * @throws GenericEntityException
     * @see #isThumbnailable(com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.attachment.Attachment)
     */
    boolean isThumbnailable(Attachment attachment) throws GenericEntityException;

    /**
     * Indicates whether JIRA can generate a thumbnail for the given attachment. This method may
     * perform better than {@link #isThumbnailable(com.atlassian.jira.issue.attachment.Attachment)}
     * because it doesn't need to look up the issue.
     *
     * @param issue the issue to which the attachment relates (if null, this method will look it up)
     * @param attachment the attachment (required)
     * @return see above
     * @throws GenericEntityException
     * @see #isThumbnailable(com.atlassian.jira.issue.attachment.Attachment)
     */
    boolean isThumbnailable(Issue issue, Attachment attachment) throws GenericEntityException;

    /**
     * Checks whether there is an "Abstract Window Toolkit" (AWT Toolkit) available.
     *
     * @return true if one is available
     */
    boolean checkToolkit();

    /**
     * Returns the Thumbnail that corresponds to an Attachment, or null if the given attachment is not an image.
     *
     * @param attachment an Attachment
     * @return returns a Thumbnail, or null
     * @see #toThumbnailedImage(com.atlassian.core.util.thumbnail.Thumbnail)
     */
    @Nullable
    Thumbnail getThumbnail(Attachment attachment);

    /**
     * Returns the Thumbnail that corresponds to an Attachment, or null if the given attachment is not an image.
     *
     * @param issue the issue for the attachment (passed in for performance reasons)
     * @param attachment an Attachment
     * @return returns a Thumbnail, or null
     * @see #toThumbnailedImage(com.atlassian.core.util.thumbnail.Thumbnail)
     */
    @Nullable
    Thumbnail getThumbnail(Issue issue, Attachment attachment);

    /**
     * Converts a Thumbnail into an ThumbnailedImage. The ThumbnailedImage is an analog of the atlassian-core Thumbnail
     * but it contains JIRA-specific methods for getting the image URL, etc.
     *
     * @param thumbnail a Thumbnail, or null
     * @return an ImageThumbnail, or null
     * @since v5.2
     */
    @Nullable
    ThumbnailedImage toThumbnailedImage(@Nullable Thumbnail thumbnail);

    /**
     * Get binary content of the thumbnail
     *
     * @param attachment
     * @param consumer
     * @return
     */
    <T> T streamThumbnailContent(Attachment attachment, InputStreamConsumer<T> consumer) throws IOException;
}
