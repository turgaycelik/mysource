package com.atlassian.jira.crowd.embedded.ofbiz;

import java.io.Serializable;

import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.membership.MembershipType;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * A composite Key of directoryId and name that is used to cache Users and Groups.
*/
final class MembershipKey implements Serializable
{
    private static final long serialVersionUID = -1089403757434661088L;

    private final long directoryId;
    private final String name;
    private final MembershipType type;

    private MembershipKey(long directoryId, String name, MembershipType type)
    {
        this.directoryId = directoryId;
        this.name = name;
        this.type = type;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public String getName()
    {
        return name;
    }

    public MembershipType getType()
    {
        return type;
    }

    @Override
    public final boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof MembershipKey))
        {
            return false;
        }

        final MembershipKey other = (MembershipKey)o;
        return directoryId == other.directoryId && type == other.type && name.equals(other.name);
    }

    @Override
    public int hashCode()
    {
        int result = (int)(directoryId ^ (directoryId >>> 32));
        result = 31 * result + type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return reflectionToString(this, SHORT_PREFIX_STYLE);
    }

    public static MembershipKey getKey(long directoryId, String name, MembershipType type)
    {
        return new MembershipKey(directoryId, toLowerCase(name), type);
    }
}
