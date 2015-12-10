package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;

public class SimpleUserOrGroupStub implements UserOrGroupStub
{
    private final long id;
    private final long directoryId;
    private final String name;
    private final String lowerName;

    public SimpleUserOrGroupStub(final long id, final long directoryId, final String name)
    {
        this(id, directoryId, name, IdentifierUtils.toLowerCase(name));
    }

    public SimpleUserOrGroupStub(final long id, final long directoryId, final String name, final String lowerName)
    {
        this.id = id;
        this.directoryId = directoryId;
        this.name = name;
        this.lowerName = lowerName;
    }

    @Override
    public long getId()
    {
        return id;
    }

    @Override
    public long getDirectoryId()
    {
        return directoryId;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getLowerName()
    {
        return lowerName;
    }

    @Override
    public String toString()
    {
        return "SimpleUserOrGroupStub[id=" + id +
                ",directoryId=" + directoryId +
                ",name=" + name +
                ",lowerName=" + lowerName +
                ']';
    }
}
