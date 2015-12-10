package com.atlassian.jira.avatar;

import java.io.InputStream;

/**
 * A helper interface for uploading and creating custom avatars
 *
 * The user will upload an image that is then stored in the session. A url and cropping instructions are sent back to
 * the user so they can select part of the image to be the avatar. Those coordinates are sent back to the server and
 * used to convert the temporary image into an avatar.
 *
 * @since v5.0
 */
public interface AvatarPickerHelper
{
    /**
     * Handles upload of temporary avatar
     *
     * @param stream upload io stream
     * @param fileName name of upload file
     * @param contentType image type
     * @param size size of avatar
     * @param ownerId ownerId the id of the owner, matches the type (project id or user key)
     * @param type the desired type of the avatar.
     * @return upload result and any errors
     */
    AvatarPickerHelperImpl.Result<AvatarPickerHelperImpl.TemporaryAvatarBean> upload(InputStream stream, String fileName, String contentType, long size, String ownerId, Avatar.Type type);

    /**
     * Converts temporary avatar (uploaded) to real avatar
     *
     * @param ownerId the id of the owner, matches the type (project id or user key)
     * @param type the desired type of the avatar
     * @param selection the area of the temporary avatar to crop real avatar to
     * @return conversion result and any errors
     */
    AvatarPickerHelperImpl.Result<Avatar> convertTemporaryToReal(String ownerId, Avatar.Type type, Selection selection);

    /**
     * Crops the temporary avatar (uploaded) using the given selection.
     *
     * @param ownerId the id of the owner, matches the type (project id or user key)
     * @param type the desired type of the avatar.
     * @param selection the area of the temporary avatar to crop real avatar to
     * @return crop result and any errors
     */
    AvatarPickerHelperImpl.Result<AvatarPickerHelperImpl.TemporaryAvatarBean> cropTemporary(String ownerId, Avatar.Type type, Selection selection);

    /**
     *
     * Gets temporary avatar url
     *
     * @return temporary avatar url
     */
    String getTemporaryAvatarUrl();
}
