package com.atlassian.jira.issue.thumbnail;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Wrapper around the atlassian-core thumbnail.
 *
 * @since v5.2
 */
class AtlassianCoreThumbnail implements ThumbnailedImage
{
    /**
     * The URL for the broken thumbnail image.
     */
    private static final String BROKEN_THUMBNAIL_URL = "/images/broken_thumbnail.png";

    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContext velocityRequestContext;
    private final Thumbnail thumbnail;

    public AtlassianCoreThumbnail(ApplicationProperties applicationProperties, VelocityRequestContext velocityRequestContext, Thumbnail thumbnail)
    {
        this.applicationProperties = applicationProperties;
        this.velocityRequestContext = velocityRequestContext;
        this.thumbnail = thumbnail;
    }

    @Override
    public String getFilename()
    {
        return thumbnail.getFilename();
    }

    @Override
    public int getHeight()
    {
        return thumbnail.getHeight();
    }

    @Override
    public int getWidth()
    {
        return thumbnail.getWidth();
    }

    @Override
    public long getAttachmentId()
    {
        return thumbnail.getAttachmentId();
    }

    @Override
    public String getMimeType()
    {
        return thumbnail.getMimeType().name();
    }

    @Override
    @HtmlSafe
    public String getImageURL()
    {
        if (thumbnail == null)
        {
            return null;
        }

        if (thumbnail instanceof BrokenThumbnail)
        {
            return velocityRequestContext.getCanonicalBaseUrl() + BROKEN_THUMBNAIL_URL;
        }

        return String.format("%s/secure/thumbnail/%s/%s", velocityRequestContext.getCanonicalBaseUrl(), thumbnail.getAttachmentId(), urlEncode(thumbnail.getFilename()));
    }

    /**
     * Encodes the given string into {@code application/x-www-form-urlencoded} format, using the JIRA encoding scheme to
     * obtain the bytes for unsafe characters.
     *
     * @param encode the String to encode
     * @return a URL-encoded String
     * @see java.net.URLEncoder#encode(String, String)
     */
    private String urlEncode(String encode)
    {
        try
        {
            return URLEncoder.encode(encode, applicationProperties.getEncoding());
        }
        catch (UnsupportedEncodingException e)
        {
            // shouldn't happen. hopefully.
            return URLEncoder.encode(encode);
        }
    }
}
