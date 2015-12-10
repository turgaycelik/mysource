package com.atlassian.jira.avatar;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Interface to retrieve and create avatars of some type respecting the current JIRA permissions setup.
 *
 * @see UniversalAvatarsService
 * @since v6.3
 */
@ExperimentalApi
public interface TypeAvatarService
{
    /**
     * Get avatars assigned to given subject - with permissions of remote user.
     *
     * @param remoteUser user whose permissions should be used
     * @param owningObjectId id of object (project/user/issuetype) to which this avatar is connected to
     *
     * @return object with all system and accessible by user custom avatars
     */
    @Nonnull
    SystemAndCustomAvatars getAvatars(@Nullable ApplicationUser remoteUser, @Nonnull String owningObjectId);

    /**
     * Return avatar using remote user permissions. May return null if user cannot access this avatar.
     *
     * @param remoteUser user whose permissions should be used
     * @param avatarId avatar id
     */
    @Nullable
    Avatar getAvatar(@Nullable ApplicationUser remoteUser, long avatarId);

    /**
     * Return information if user can add avatar to given subject.
     *
     * @param remoteUser user whose permissions should be used
     * @param owningObjectId id of object (project/user/issuetype) to which this avatar is connected to
     */
    boolean canUserCreateAvatar(@Nonnull ApplicationUser remoteUser, String owningObjectId);

    /**
     * Adds new avatar to given subject with provided image data - with permissions of given remote user. Returns
     * created avatar.
     *
     * @param remoteUser user whose permissions should be used
     * @param owningObjectId id of object (project/user/issuetype) to which this avatar will be connected to
     * @param imageDataProvider provider of image data for new avatar
     *
     * @throws java.lang.IllegalAccessException if user cannot add avatar to given subject
     * @throws java.io.IOException if there is error with image data
     */
    @Nonnull
    Avatar createAvatar(@Nonnull ApplicationUser remoteUser, @Nonnull String owningObjectId, @Nonnull AvatarImageDataProvider imageDataProvider)
            throws IllegalAccessException, IOException;

    /**
     * Get default avatar for current type. This avatar should be available to anyone
     * @return
     */
    @Nonnull
    Avatar getDefaultAvatar();
}
