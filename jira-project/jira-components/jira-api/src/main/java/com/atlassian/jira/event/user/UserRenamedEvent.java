package com.atlassian.jira.event.user;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

/**
 * This event is sent when a user is renamed.
 *
 * @since v6.0
 */
public class UserRenamedEvent extends UserProfileUpdatedEvent
{
    private String oldUserName;

    public UserRenamedEvent(ApplicationUser user, ApplicationUser editedBy, String oldUserName)
    {
        super(user.getDirectoryUser(), ApplicationUsers.toDirectoryUser(editedBy));
        this.oldUserName = oldUserName;
    }

    /**
     * Returns the old username (before the user was renamed).
     *
     * @return the old username
     */
    public String getOldUserName()
    {
        return oldUserName;
    }
}
