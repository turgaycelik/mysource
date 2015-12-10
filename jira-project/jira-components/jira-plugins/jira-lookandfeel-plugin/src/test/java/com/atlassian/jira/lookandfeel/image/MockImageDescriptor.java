package com.atlassian.jira.lookandfeel.image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MockImageDescriptor extends ImageDescriptor
{

    private final String imageName;
    private final String contentType;
    private InputStream inputStream;

    public MockImageDescriptor(String imageName, String contentType)
    {
        this(imageName, contentType, new ByteArrayInputStream(new byte[] { }));
    }

    public MockImageDescriptor(String imageName, String contentType, InputStream inputStream)
    {
        this.imageName = imageName;
        this.contentType = contentType;
        this.inputStream = inputStream;
    }

    public String getImageName()
    {
        return imageName;
    }

    @Override
    public String getImageDescriptorType()
    {
        return null;
    }

    public String getContentType()
    {
        return contentType;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }
}
