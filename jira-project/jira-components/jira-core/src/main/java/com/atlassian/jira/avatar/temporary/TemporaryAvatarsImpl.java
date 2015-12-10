package com.atlassian.jira.avatar.temporary;

import javax.servlet.http.HttpSession;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.TypeAvatarService;
import com.atlassian.jira.avatar.TemporaryAvatar;
import com.atlassian.jira.avatar.TemporaryAvatars;
import com.atlassian.jira.avatar.UniversalAvatarsService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.SessionKeys;

public class TemporaryAvatarsImpl implements TemporaryAvatars
{
    private final UniversalAvatarsService avatars;

    public TemporaryAvatarsImpl(final UniversalAvatarsService avatars)
    {
        this.avatars = avatars;
    }

    @Override
    public void storeTemporaryAvatar(ApplicationUser remoteUser, TemporaryAvatar avatar, Avatar.Type type, String ownerId)
            throws IllegalAccessException
    {
        final TypeAvatarService typeAvatars = avatars.getAvatars(type);

        if (typeAvatars==null || !typeAvatars.canUserCreateAvatar(remoteUser, ownerId))
        {
            throw new IllegalAccessException("User cannot store temporary avatars");
        }

        ExecutingHttpRequest.get().getSession().setAttribute(SessionKeys.TEMP_AVATAR, avatar);
    }

    @Override
    public TemporaryAvatar getCurrentTemporaryAvatar()
    {
        final HttpSession session = ExecutingHttpRequest.get().getSession();

        return (TemporaryAvatar) session.getAttribute(SessionKeys.TEMP_AVATAR);
    }

    @Override
    public void dispose(TemporaryAvatar avatar)
    {
        avatar.getFile().delete();

        final HttpSession session = ExecutingHttpRequest.get().getSession();
        session.removeAttribute(SessionKeys.TEMP_AVATAR);
    }
}
