package com.atlassian.jira.event.user;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;

/**
 * Published when a user logs out.
 */
@PublicApi
public final class LogoutEvent extends UserEvent
{
    public LogoutEvent(User user)
    {
        super(user, UserEventType.USER_LOGOUT);
    }
}
