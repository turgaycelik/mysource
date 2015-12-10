package com.atlassian.jira.lookandfeel.image;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple Class to hold image details
 *
 * @since v4.4
 */
public abstract class ImageDescriptor
{
    protected InputStream imageData;
    protected String contentType;
    protected String fileName;

    private String imageUrl;

    public String getContentType()
    {
        return contentType;
    }

    public InputStream getInputStream()
    {
        return imageData;
    }

    public String getImageName()
    {
        return fileName;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public abstract String getImageDescriptorType();

    public void closeImageStreamQuietly()
    {
        try
        {
            if (imageData != null)
            {
                this.imageData.close();
            }
        }
        catch (IOException ignore)
        {

        }
    }
}

