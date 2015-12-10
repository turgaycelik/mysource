package com.atlassian.jira.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

/**
 *
 * @since v6.2
 */
public class AffectedUser implements AssociatedItem
{
    private final String username;
    private final String userKey;
    private final String directoryName;
    private final String directoryId;

    public AffectedUser(final User user)
    {
        this(user.getName(), ApplicationUsers.getKeyFor(user), getDirectoryById((user.getDirectoryId())));
    }

    public AffectedUser(final ApplicationUser user)
    {
        this(user.getName(), user.getKey(), getDirectoryById((user.getDirectoryId())));
    }

    public AffectedUser(@Nonnull final String username, @Nullable final String userKey, @Nonnull final Directory directory)
    {
        this.username = username;
        this.userKey = userKey;
        this.directoryName = directory.getName();
        this.directoryId = directory.getId().toString();
    }

    @Override
    @Nonnull
    public String getObjectName()
    {
        return username;
    }

    @Override
    public String getObjectId()
    {
        return userKey;
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


    @Override
    @Nonnull
    public Type getObjectType()
    {
        return Type.USER;
    }

    private static Directory getDirectoryById(final long directoryId)
    {
        return ComponentAccessor.getUserManager().getDirectory(directoryId);
    }
}
