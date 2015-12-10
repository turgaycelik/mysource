package com.atlassian.jira.event.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Event indicating that a user's profile has been updated.
 *
 * @since v5.0
 */
public class UserProfileUpdatedEvent
{
    private String username;
    private String editedByUsername;

    public UserProfileUpdatedEvent(ApplicationUser user, ApplicationUser editedBy)
    {
        if (user != null)
        {
            this.username = user.getName();
        }

        if (editedBy != null)
        {
            this.editedByUsername = editedBy.getName();
        }
    }

    /**
     * This method has been deprecated
     * @deprecated since 6.1 use {@link #UserProfileUpdatedEvent(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.user.ApplicationUser)} instead
     */
    @Deprecated
    public UserProfileUpdatedEvent(User user, User editedBy)
    {
        if (user != null)
        {
            this.username = user.getName();
        }

        if (editedBy != null)
        {
            this.editedByUsername = editedBy.getName();
        }
    }

    public String getUsername()
    {
        return username;
    }

    public String getEditedByUsername()
    {
        return editedByUsername;
    }
}
