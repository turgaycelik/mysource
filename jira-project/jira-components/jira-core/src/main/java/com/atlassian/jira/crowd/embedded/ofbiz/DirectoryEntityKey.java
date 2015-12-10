package com.atlassian.jira.crowd.embedded.ofbiz;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.atlassian.crowd.model.DirectoryEntity;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * A composite Key of directoryId and name that is used to cache Users and Groups.
*/
final class DirectoryEntityKey implements Serializable
{
    public static DirectoryEntityKey getKeyPreserveCase(long directoryId, String name)
    {
        return new DirectoryEntityKey(directoryId, name);
    }

    public static DirectoryEntityKey getKeyLowerCase(long directoryId, String name)
    {
        return new DirectoryEntityKey(directoryId, toLowerCase(name));
    }

    public static DirectoryEntityKey getKeyLowerCase(DirectoryEntity entity)
    {
        if (entity instanceof UserOrGroupStub)
        {
            return getKeyFor((UserOrGroupStub)entity);
        }
        return new DirectoryEntityKey(entity.getDirectoryId(), toLowerCase(entity.getName()));
    }

    public static DirectoryEntityKey getKeyFor(UserOrGroupStub stub)
    {
        return new DirectoryEntityKey(stub.getDirectoryId(), stub.getLowerName());
    }

    private final long directoryId;
    private final String name;

    private DirectoryEntityKey(long directoryId, String name)
    {
        this.directoryId = directoryId;
        this.name = name;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(final Object o)
    {
        return this == o || (o instanceof DirectoryEntityKey && equals((DirectoryEntityKey)o));
    }

    private boolean equals(@Nonnull final DirectoryEntityKey other)
    {
        return directoryId == other.directoryId && name.equals(other.name);
    }

    @Override
    public int hashCode()
    {
        int hash = (int)(directoryId ^ (directoryId >>> 32));
        return 31 * hash + name.hashCode();
    }

    @Override
    public String toString()
    {
        return "{" + directoryId + ',' + name + '}';
    }
}
