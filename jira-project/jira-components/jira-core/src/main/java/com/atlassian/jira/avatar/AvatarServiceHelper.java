package com.atlassian.jira.avatar;

import com.atlassian.crowd.embedded.api.User;

/**
 * Renders avatars for an arbitrary user. This whole class really only exists because Velocity gets confused when
 * calling into the real {@link com.atlassian.jira.avatar.AvatarService} on account of the fact that the AvatarService
 * has a bunch of overloaded methods and Velocity can't decide which one to run when it gets a null user
 * (JRADEV-20734).
 *
 * @since v6.0
 */
@SuppressWarnings ("deprecation")
public final class AvatarServiceHelper
{
    private final AvatarService avatarService;

    public AvatarServiceHelper(AvatarService avatarService)
    {
        this.avatarService = avatarService;
    }

    /**
     * @see AvatarService#getAvatarURL(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.avatar.Avatar.Size)
     */
    public String renderURL(User loggedInUser, User avatarUser, Avatar.Size size)
    {
        return avatarService.getAvatarURL(loggedInUser, avatarUser != null ? avatarUser.getName() : null, size).toString();
    }
}
