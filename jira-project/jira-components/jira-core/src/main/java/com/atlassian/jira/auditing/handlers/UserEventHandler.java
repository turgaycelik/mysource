package com.atlassian.jira.auditing.handlers;

import com.atlassian.crowd.event.user.AutoUserCreatedEvent;
import com.atlassian.crowd.event.user.UserCreatedEvent;
import com.atlassian.crowd.event.user.UserCredentialUpdatedEvent;
import com.atlassian.crowd.event.user.UserDeletedEvent;
import com.atlassian.crowd.event.user.UserUpdatedEvent;
import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.RecordRequest;

/**
 * @since v6.2
 */
public interface UserEventHandler
{
    Option<RecordRequest> handleUserUpdatedEvent(UserUpdatedEvent event);

    RecordRequest handleUserCreatedEvent(UserCreatedEvent event);

    RecordRequest handleUserDeletedEvent(UserDeletedEvent event);

    RecordRequest handleUserCredentialUpdatedEvent(UserCredentialUpdatedEvent event);
}
