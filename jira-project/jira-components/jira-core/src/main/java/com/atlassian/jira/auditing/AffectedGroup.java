package com.atlassian.jira.auditing;

import com.atlassian.crowd.embedded.api.Directory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @since v6.2
 */
public class AffectedGroup implements AssociatedItem
{
    final private String groupName;
    private final String directoryName;
    private final String directoryId;

    public AffectedGroup(final String groupName, final Directory directory) {
        this.groupName = groupName;
        this.directoryName = directory.getName();
        this.directoryId = directory.getId().toString();
    }

    @Nonnull
    @Override
    public String getObjectName()
    {
        return groupName;
    }

    @Nullable
    @Override
    public String getObjectId()
    {
        return null; //todo change this to something reasonable
    }

    @Nullable
    @Override
    public String getParentName()
    {
        return directoryName;
    }

    @Nullable
    @Override
    public String getParentId()
    {
        return directoryId;
    }

    @Nonnull
    @Override
    public Type getObjectType()
    {
        return Type.GROUP;
    }
}
