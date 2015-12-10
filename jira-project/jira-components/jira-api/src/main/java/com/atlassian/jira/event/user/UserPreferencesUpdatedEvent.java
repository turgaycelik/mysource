package com.atlassian.jira.event.user;

import com.atlassian.crowd.embedded.api.User;

/**
 * Event indicating that a user's preferences have been updated.
 *
 * @since v5.0
 */
public class UserPreferencesUpdatedEvent
{
    private String username;

    public UserPreferencesUpdatedEvent(User user)
    {
        if (user != null)
        {
            this.username = user.getName();
        }
    }

    public String getUsername()
    {
        return username;
    }
}
