package com.atlassian.jira.rest.v2.avatar;

import java.io.File;

public class UploadedAvatar
{
    private final File imageFile;
    private final String contentType;
    private final int width;
    private final int height;

    public File getImageFile()
    {
        return imageFile;
    }

    public String getContentType()
    {
        return contentType;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public UploadedAvatar(final File imageFile, final String contentType, final int width, final int height)
    {
        this.imageFile = imageFile;
        this.contentType = contentType;
        this.width = width;
        this.height = height;
    }
}
