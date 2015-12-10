package com.atlassian.jira.issue.thumbnail;

import com.atlassian.core.util.thumbnail.Thumbnail;

/**
 * Represents a Thumbnail whose scaling/rendering has failed despite being an apparently thumbnailable type.
 *
 * @since v4.2
 */
public class BrokenThumbnail extends Thumbnail
{

    private static final int WIDTH = 48;
    private static final int HEIGHT = 48;

    public BrokenThumbnail(long attachmentId)
    {
        super(HEIGHT, WIDTH, null, attachmentId, MimeType.PNG);
    }

}
