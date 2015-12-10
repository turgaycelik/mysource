package com.atlassian.jira.avatar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.TemporaryAvatar;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Emmanation of existing implicit link between REST plugin and core. Temporary avatar is created and stored in REST and
 * displayed by servlet in core. This may be changed in future versions.
 *
 * @since v6.3
 */
public interface TemporaryAvatars
{
    /**
     * Store temporary avatar - make it available to displaying servlet. There is only one temporary avatar stored for
     * user session.
     *
     * @param remoteUser user that access avatar - null can be used for anonymous access.
     * @param avatar avatar to store
     * @param type type for which this avatar is
     * @param owningObjectId id of object (project/user/issuetype) to which this avatar is connected to
     */
    void storeTemporaryAvatar(
            @Nonnull ApplicationUser remoteUser, @Nonnull TemporaryAvatar avatar,
            @Nonnull Avatar.Type type, @Nonnull String owningObjectId)
            throws IllegalAccessException;

    /**
     * Get current temporary avatar.
     */
    @Nullable
    TemporaryAvatar getCurrentTemporaryAvatar();

    /**
     * Dispose temporary avatar. Unsets if this is current temporary avatar.
     */
    void dispose(@Nonnull TemporaryAvatar avatar);
}
