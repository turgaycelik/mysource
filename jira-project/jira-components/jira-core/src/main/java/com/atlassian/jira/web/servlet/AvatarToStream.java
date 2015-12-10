package com.atlassian.jira.web.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.util.StreamCopyingConsumer;

public class AvatarToStream
{
    private static final int AVATAR_BUFFER_SIZE = 4096;

    private final AvatarManager avatarManager;

    public AvatarToStream(final AvatarManager avatarManager) {
        this.avatarManager = avatarManager;
    }

    public void sendAvatar(final Avatar avatar, final Avatar.Size size, final HttpServletResponse response)
            throws IOException
    {
        response.setContentType(AvatarManager.AVATAR_IMAGE_FORMAT_FULL.getContentType());
        HttpResponseHeaders.cachePrivatelyForAboutOneYear(response);
        final OutputStream out = response.getOutputStream();
        StreamCopyingConsumer streamCopier = new StreamCopyingConsumer(out, AVATAR_BUFFER_SIZE);


        final AvatarManager.ImageSize avatarSize = AvatarManager.ImageSize.fromSize(size);
        avatarManager.readAvatarData(avatar, avatarSize, streamCopier);
    }
}
