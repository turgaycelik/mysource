package com.atlassian.jira.avatar.types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Contains informations about user permissions to avatars of specific type.
 */
public interface AvatarAccessPolicy
{
    /**
     * Return information if user can add avatar to given subject.
     *
     * @param remoteUser user whose permissions should be used. Null means anonymous access.
     * @param avatar avatar that the user wishes to access
     */
    public boolean userCanViewAvatar(@Nullable ApplicationUser remoteUser, @Nonnull Avatar avatar);

    /**
     * Return information if user can add avatar to given subject.
     *
     * @param remoteUser user whose permissions should be used. Null means anonymous access.
     * @param owningObjectId id of object (project/user/issuetype) to which this avatar is connected to
     */
    public boolean userCanCreateAvatarFor(@Nullable ApplicationUser remoteUser, @Nonnull final String owningObjectId);
}
