package com.atlassian.jira.user;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;

/**
 * Service that can decide whether a user can be deleted or not.
 * 
 * For example if a user is assigned to issues, then this account should not be deleted. 
 * If a user delete is vetoed, then the system should disable the account instead, but remember the details of the old 
 * account (display name and email address).
 *
 * @since v6.1
 */
@Internal
public interface UserDeleteVeto
{
    boolean allowDeleteUser(User user);

    long getCommentCountByAuthor(ApplicationUser user);
}
