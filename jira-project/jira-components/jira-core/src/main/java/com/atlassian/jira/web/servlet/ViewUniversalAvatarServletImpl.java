package com.atlassian.jira.web.servlet;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.TypeAvatarService;
import com.atlassian.jira.avatar.UniversalAvatarsService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

public class ViewUniversalAvatarServletImpl
{
    public static final String AVATAR_ID_PARAM = "avatarId";
    public static final String AVATAR_TYPE_PARAM = "avatarType";
    public static final String AVATAR_SIZE_PARAM = "size";

    private final JiraAuthenticationContext authenticationContext;
    private final UniversalAvatarsService avatars;
    private final AvatarToStream avatarToStream;

    public ViewUniversalAvatarServletImpl(
            final JiraAuthenticationContext authenticationContext,
            final UniversalAvatarsService avatars,
            final AvatarToStream avatarToStream)
    {
        this.authenticationContext = authenticationContext;
        this.avatars = avatars;
        this.avatarToStream = avatarToStream;
    }

    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        try {
            final Avatar avatar = getAvatar(request);
            final Avatar.Size size = getValidAvatarSize(request);

            avatarToStream.sendAvatar(avatar, size, response);
        } catch (IllegalArgumentException iae) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, iae.getMessage());
        }
    }

    /**
     * @throws java.lang.IllegalArgumentException
     */
    @Nonnull
    private Avatar getAvatar(final HttpServletRequest request)
    {
        final ApplicationUser user = authenticationContext.getUser();

        final long avatarId = getValidAvatarId(request);
        final Avatar.Type avatarType = getValidAvatarType(request);

        final TypeAvatarService avatarsForType = avatars.getAvatars(avatarType);
        if ( null==avatarsForType) {
            throw new IllegalArgumentException("Avatar type: "+avatarType);
        }
        Avatar avatar = avatarsForType.getAvatar(user, avatarId);
        if (null==avatar) {
            avatar = avatarsForType.getDefaultAvatar();
        }

        return avatar;
    }

    private long getValidAvatarId(final HttpServletRequest request)
    {
        String avatarIdSpec = request.getParameter(AVATAR_ID_PARAM);
        if (null == avatarIdSpec)
        {
            throw new IllegalArgumentException(AVATAR_ID_PARAM);
        }

        try
        {
            return Long.valueOf(avatarIdSpec);
        }
        catch (NumberFormatException x)
        {
            throw new IllegalArgumentException(AVATAR_ID_PARAM, x);
        }
    }

    private Avatar.Type getValidAvatarType(final HttpServletRequest request)
    {
        String avatarTypeSpec = request.getParameter(AVATAR_TYPE_PARAM);
        final Avatar.Type type = Avatar.Type.getByName(avatarTypeSpec);
        if (null == type)
        {
            throw new IllegalArgumentException(AVATAR_TYPE_PARAM);
        }

        return type;
    }

    private Avatar.Size getValidAvatarSize(final HttpServletRequest request)
    {
        String avatarSizeSpec = request.getParameter(AVATAR_SIZE_PARAM);
        if (null == avatarSizeSpec)
        {
            return Avatar.Size.defaultSize();
        }

        try {
            return Avatar.Size.getSizeFromParam(avatarSizeSpec);
        } catch( NoSuchElementException x ) {
            throw new IllegalArgumentException(AVATAR_SIZE_PARAM, x);
        }
    }
}
