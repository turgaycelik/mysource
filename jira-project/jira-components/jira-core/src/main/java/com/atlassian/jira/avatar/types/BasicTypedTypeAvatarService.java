package com.atlassian.jira.avatar.types;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImageDataProvider;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.TypeAvatarService;
import com.atlassian.jira.avatar.SystemAndCustomAvatars;
import com.atlassian.jira.user.ApplicationUser;

public class BasicTypedTypeAvatarService implements TypeAvatarService
{
    private final Avatar.Type type;
    private final AvatarManager avatarManager;
    private final AvatarAccessPolicy policy;

    public BasicTypedTypeAvatarService(final Avatar.Type type, final AvatarManager avatarManager, final AvatarAccessPolicy policy)
    {
        this.type = type;
        this.avatarManager = avatarManager;
        this.policy = policy;
    }

    @Override
    final public SystemAndCustomAvatars getAvatars(final ApplicationUser remoteUser, final String owningObjectId)
    {
        final List<Avatar> systemAvatars = avatarManager.getAllSystemAvatars(type);
        final List<Avatar> avatarsForOwner = avatarManager.getCustomAvatarsForOwner(type, owningObjectId);

        return new SystemAndCustomAvatars(systemAvatars, avatarsForOwner);
    }

    @Override
    public Avatar getAvatar(final ApplicationUser remoteUser, final long avatarId)
    {
        final Avatar currentAvatar = avatarManager.getById(avatarId);
        final boolean userCanViewAvatar =
                currentAvatar != null ?
                        policy.userCanViewAvatar(remoteUser, currentAvatar) :
                        false;

        return userCanViewAvatar ? currentAvatar : null;
    }

    @Override
    public boolean canUserCreateAvatar(final ApplicationUser remoteUser, final String owningObjectId)
    {
        return policy.userCanCreateAvatarFor(remoteUser, owningObjectId);
    }

    @Override
    public Avatar createAvatar(final ApplicationUser remoteUser, final String owningObjectId, final AvatarImageDataProvider imageDataProvider)
            throws IllegalAccessException, IOException
    {
        if (!policy.userCanCreateAvatarFor(remoteUser, owningObjectId))
        {
            throw new IllegalAccessException();
        }
        return avatarManager.create(this.type, owningObjectId, imageDataProvider);
    }

    @Nonnull
    @Override
    public Avatar getDefaultAvatar()
    {
        final Long defaultAvatarId = avatarManager.getDefaultAvatarId(this.type);
        if (null == defaultAvatarId)
        {
            throw new NoSuchElementException("No default avatar id for " + this.type);
        }
        final Avatar avatar = avatarManager.getById(defaultAvatarId);
        if (null == avatar)
        {
            throw new NoSuchElementException("No default avatar for " + this.type);
        }
        return avatar;
    }


}
