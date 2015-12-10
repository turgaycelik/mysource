package com.atlassian.jira.event.user;

import com.atlassian.crowd.embedded.api.User;

/**
 * Event indicating that a user's avatar has been updated.
 *
 * @since v5.0
 */
public class UserAvatarUpdatedEvent
{
    private String username;
    private Long avatarId;

    public UserAvatarUpdatedEvent(User user, Long avatarId)
    {
        if (user != null)
        {
            this.username = user.getName();
        }
        
        this.avatarId = avatarId;
    }

    public String getUsername()
    {
        return username;
    }

    public Long getAvatarId()
    {
        return avatarId;
    }
}
