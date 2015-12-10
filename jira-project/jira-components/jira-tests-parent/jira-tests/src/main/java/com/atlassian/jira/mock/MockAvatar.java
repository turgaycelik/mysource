package com.atlassian.jira.mock;

import com.atlassian.jira.avatar.Avatar;

/**
 * Non production bean implementation of {@link com.atlassian.jira.avatar.Avatar}.
 *
 * @since v4.0
 */
public class MockAvatar implements Avatar
{
    private long id;
    private Type type;
    private String contentType;
    private String fileName;
    private String owner;
    private boolean system;

    public MockAvatar(final long id, final String fileName, final String contentType, final Type type, final String owner, final boolean system)
    {
        this.id = id;
        this.contentType = contentType;
        this.fileName = fileName;
        this.owner = owner;
        this.system = system;
        this.type = type;
    }

    public Type getAvatarType()
    {
        return type;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getFileName()
    {
        return fileName;
    }

    public Long getId()
    {
        return id;
    }

    public String getOwner()
    {
        return owner;
    }

    public boolean isSystemAvatar()
    {
        return system;
    }
}
