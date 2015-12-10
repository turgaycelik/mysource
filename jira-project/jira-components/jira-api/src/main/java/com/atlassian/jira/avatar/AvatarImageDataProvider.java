package com.atlassian.jira.avatar;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides avatar image data for given size.
 *
 * @see TypeAvatarService#createAvatar(com.atlassian.jira.user.ApplicationUser, String, AvatarImageDataProvider)
 * @see com.atlassian.jira.avatar.AvatarManager#create(com.atlassian.jira.avatar.Avatar.Type, String,
 * AvatarImageDataProvider)
 * @since 6.3
 */
public interface AvatarImageDataProvider
{
    /**
     * Stores avatar image data for requestedSize to given output stream.
     *
     * @param requestSize size in which avatar image should be stored
     * @param output stream where write image data
     * @throws java.io.IOException when there are problems processing image to stream
     */
    void storeImage(Avatar.Size requestSize, OutputStream output) throws IOException;
}
