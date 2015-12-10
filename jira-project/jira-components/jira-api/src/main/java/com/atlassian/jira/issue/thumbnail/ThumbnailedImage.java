package com.atlassian.jira.issue.thumbnail;

import com.atlassian.annotations.PublicApi;

/**
 * JIRA-specific thunbnailed image interface. A {@code ThumbnailedImage} contains methods for getting its URL, etc.
 *
 * @see ThumbnailManager#toThumbnailedImage(com.atlassian.core.util.thumbnail.Thumbnail)
 * @since v5.2
 */
@PublicApi
public interface ThumbnailedImage
{
    /**
     * @return the name of the image file
     * @since v5.2
     */
    String getFilename();

    /**
     * @return the height of the image, in pixels
     * @since v5.2
     */
    int getHeight();

    /**
     * @return the width of the image, in pixels
     * @since v5.2
     */
    int getWidth();

    /**
     * @return the id of the attachment
     * @since v5.2
     */
    long getAttachmentId();

    /**
     * @return the MIME type of the image
     * @since v5.2
     */
    String getMimeType();

    /**
     * @return the URL for this thumbnail image. If running in the context of a web request, this will return a url
     *         relative to the server root (ie "/jira/"). If running via email, it will return an absolute URL (eg.
     *         "http://example.com/jira/").
     */
    String getImageURL();
}
