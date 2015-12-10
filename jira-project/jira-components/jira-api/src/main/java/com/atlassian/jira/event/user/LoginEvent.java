package com.atlassian.jira.event.user;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;

/**
 * Published when a user successfully logs in.
 */
@PublicApi
public final class LoginEvent extends UserEvent
{
    public LoginEvent(User user)
    {
        super(user, UserEventType.USER_LOGIN);
    }
}
