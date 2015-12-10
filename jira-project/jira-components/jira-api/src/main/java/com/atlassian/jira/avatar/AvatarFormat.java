package com.atlassian.jira.avatar;

public class AvatarFormat
{
    private String name;
    private String contentType;

    public AvatarFormat(String name, String contentType)
    {
        this.name = name;
        this.contentType = contentType;
    }

    public String getName()
    {
        return name;
    }

    public String getContentType()
    {
        return contentType;
    }
}
