package com.atlassian.jira.crowd.embedded.ofbiz;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * A composite Key of directoryId and UserID that is used to cache Users Attributes.
*/
final class AttributeKey implements Serializable
{
    private final long directoryId;
    private final long userId;

    AttributeKey(final long directoryId, final long userId)
    {
        this.directoryId = directoryId;
        this.userId = userId;
    }

    long getDirectoryId()
    {
        return directoryId;
    }

    long getUserId()
    {
        return userId;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (!(o instanceof AttributeKey)) { return false; }

        final AttributeKey that = (AttributeKey) o;

        return directoryId == that.directoryId && userId == that.userId;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (directoryId ^ (directoryId >>> 32));
        result = 31 * result + (int) (userId ^ (userId >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return reflectionToString(this, SHORT_PREFIX_STYLE);
    }
}
